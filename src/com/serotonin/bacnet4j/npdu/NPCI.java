package com.serotonin.bacnet4j.npdu;

import java.math.BigInteger;

import com.serotonin.util.queue.ByteQueue;

/**
 * Network layer protocol control information. This class currently only implements the reading of information.
 * @author mlohbihler
 */
public class NPCI {
    private int version;
    private BigInteger control;
    private int destinationNetwork;
    private int destinationLength;
    private byte[] destinationAddress;
    private int sourceNetwork;
    private int sourceLength;
    private byte[] sourceAddress;
    private int hopCount;
    private int messageType;
    private int vendorId;
    
    public NPCI(ByteQueue queue) {
        version = queue.popU1B();
        control = BigInteger.valueOf(queue.popU1B());
        
        if (control.testBit(5)) {
            destinationNetwork = queue.popU2B();
            destinationLength = queue.popU1B();
            if (destinationLength > 0) {
                destinationAddress = new byte[destinationLength];
                queue.pop(destinationAddress);
            }
        }
        
        if (control.testBit(3)) {
            sourceNetwork = queue.popU2B();
            sourceLength = queue.popU1B();
            sourceAddress = new byte[sourceLength];
            queue.pop(sourceAddress);
        }
        
        if (control.testBit(5))
            hopCount = queue.popU1B();
        
        if (control.testBit(7)) {
            messageType = queue.popU1B();
            if (messageType >= 80)
                vendorId = queue.popU2B();
        }
    }

    public boolean hasDestinationInfo() {
        return control.testBit(5);
    }
    
    public boolean isDestinationBroadcast() {
        return destinationLength == 0;
    }
    
    public boolean hasSourceInfo() {
        return control.testBit(3);
    }
    
    public boolean isExpectingReply() {
        return control.testBit(2);
    }
    
    public boolean isNetworkMessage() {
        return control.testBit(7);
    }
    
    public boolean isVendorSpecificNetworkMessage() {
        return isNetworkMessage() && messageType >= 80;
    }
    
    public int getNetworkPriority() {
        return (control.testBit(1) ? 2 : 0) | (control.testBit(0) ? 1 : 0);
    }
    
    
    public byte[] getDestinationAddress() {
        return destinationAddress;
    }

    public int getDestinationLength() {
        return destinationLength;
    }

    public int getDestinationNetwork() {
        return destinationNetwork;
    }

    public int getHopCount() {
        return hopCount;
    }

    public int getMessageType() {
        return messageType;
    }

    public byte[] getSourceAddress() {
        return sourceAddress;
    }

    public int getSourceLength() {
        return sourceLength;
    }

    public int getSourceNetwork() {
        return sourceNetwork;
    }

    public int getVendorId() {
        return vendorId;
    }

    public int getVersion() {
        return version;
    }
}