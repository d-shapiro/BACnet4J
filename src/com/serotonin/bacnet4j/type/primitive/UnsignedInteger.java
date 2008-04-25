package com.serotonin.bacnet4j.type.primitive;

import java.math.BigInteger;

import com.serotonin.util.queue.ByteQueue;

public class UnsignedInteger extends Primitive {
    public static final byte TYPE_ID = 2;
    
    private int smallValue;
    private BigInteger bigValue;
    
    public UnsignedInteger(int value) {
        if (value < 0)
            throw new IllegalArgumentException("Value cannot be less than zero");
        smallValue = value;
    }
    
    public UnsignedInteger(BigInteger value) {
        if (value.signum() == -1)
            throw new IllegalArgumentException("Value cannot be less than zero");
        bigValue = value;
    }
    
    public int intValue() {
        if (bigValue == null)
            return smallValue;
        return bigValue.intValue();
    }
    
    public BigInteger bigIntegerValue() {
        if (bigValue == null)
            return BigInteger.valueOf(smallValue);
        return bigValue;
    }
    
    //
    // Reading and writing
    //
    public UnsignedInteger(ByteQueue queue) {
        int length = (int)readTag(queue);
        if (length < 4) {
            while (length > 0)
                smallValue |= (queue.pop() & 0xff) << (--length * 8); 
        }
        else {
            byte[] bytes = new byte[length+1];
            queue.pop(bytes, 1, length);
            bigValue = new BigInteger(bytes);
        }
    }
    
    protected void writeImpl(ByteQueue queue) {
        int length = (int)getLength();
        if (bigValue == null) {
            while (length > 0)
                queue.push(smallValue >> (--length * 8));
        }
        else {
            byte[] bytes = new byte[length];
            
            for (int i=0; i<bigValue.bitLength(); i++) {
                if (bigValue.testBit(i))
                    bytes[length - i/8 - 1] |= 1 << (i%8);
            }
            
            queue.push(bytes);
        }
    }

    protected long getLength() {
        if (bigValue == null) {
            int length;
            if (smallValue < 0x100)
                length = 1;
            else if (smallValue < 0x10000)
                length = 2;
            else if (smallValue < 0x1000000)
                length = 3;
            else
                length = 4;
            
            return length;
        }
        
        if (bigValue.intValue() == 0)
            return 1;
        return (bigValue.bitLength() + 7) / 8;
    }

    protected byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((bigValue == null) ? 0 : bigValue.hashCode());
        result = PRIME * result + smallValue;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final UnsignedInteger other = (UnsignedInteger) obj;
        return this.bigIntegerValue().equals(other.bigIntegerValue());
    }
    
    public String toString() {
        if (bigValue == null)
            return Integer.toString(smallValue);
        return bigValue.toString();
    }
}