package com.serotonin.bacnet4j.service.confirmed;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.service.acknowledgement.AcknowledgementService;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.Choice;
import com.serotonin.bacnet4j.type.enumerated.MessagePriority;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.util.queue.ByteQueue;

public class ConfirmedTextMessageRequest extends ConfirmedRequestService {
    public static final byte TYPE_ID = 19;
    
    private static List<Class<? extends Encodable>> classes;
    static {
        classes = new ArrayList<Class<? extends Encodable>>();
        classes.add(UnsignedInteger.class);
        classes.add(CharacterString.class);
    }
    
    private ObjectIdentifier textMessageSourceDevice;
    private Choice messageClass;
    private MessagePriority messagePriority;
    private CharacterString message;
    
    public ConfirmedTextMessageRequest(ObjectIdentifier textMessageSourceDevice, UnsignedInteger messageClass, 
            MessagePriority messagePriority, CharacterString message) {
        this.textMessageSourceDevice = textMessageSourceDevice;
        this.messageClass = new Choice(0, messageClass);
        this.messagePriority = messagePriority;
        this.message = message;
    }

    public ConfirmedTextMessageRequest(ObjectIdentifier textMessageSourceDevice, CharacterString messageClass, 
            MessagePriority messagePriority, CharacterString message) {
        this.textMessageSourceDevice = textMessageSourceDevice;
        this.messageClass = new Choice(0, messageClass);
        this.messagePriority = messagePriority;
        this.message = message;
    }

    public ConfirmedTextMessageRequest(ObjectIdentifier textMessageSourceDevice, MessagePriority messagePriority, 
            CharacterString message) {
        this.textMessageSourceDevice = textMessageSourceDevice;
        this.messagePriority = messagePriority;
        this.message = message;
    }

    @Override
    public byte getChoiceId() {
        return TYPE_ID;
    }
    
    @Override
    public AcknowledgementService handle(LocalDevice localDevice, Address from) throws BACnetException {
        localDevice.getEventHandler().fireTextMessage(
                localDevice.getRemoteDeviceCreate(textMessageSourceDevice.getInstanceNumber(), from),
                messageClass, messagePriority, message);
        return null;
    }

    @Override
    public void write(ByteQueue queue) {
        write(queue, textMessageSourceDevice, 0);
        writeOptional(queue, messageClass, 1);
        write(queue, messagePriority, 2);
        write(queue, message, 3);
    }
    
    ConfirmedTextMessageRequest(ByteQueue queue) throws BACnetException {
        textMessageSourceDevice = read(queue, ObjectIdentifier.class, 0);
        if (readStart(queue) == 1)
            messageClass = new Choice(queue, classes);
        messagePriority = read(queue, MessagePriority.class, 2);
        message = read(queue, CharacterString.class, 3);
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((message == null) ? 0 : message.hashCode());
        result = PRIME * result + ((messageClass == null) ? 0 : messageClass.hashCode());
        result = PRIME * result + ((messagePriority == null) ? 0 : messagePriority.hashCode());
        result = PRIME * result + ((textMessageSourceDevice == null) ? 0 : textMessageSourceDevice.hashCode());
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
        final ConfirmedTextMessageRequest other = (ConfirmedTextMessageRequest) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        }
        else if (!message.equals(other.message))
            return false;
        if (messageClass == null) {
            if (other.messageClass != null)
                return false;
        }
        else if (!messageClass.equals(other.messageClass))
            return false;
        if (messagePriority == null) {
            if (other.messagePriority != null)
                return false;
        }
        else if (!messagePriority.equals(other.messagePriority))
            return false;
        if (textMessageSourceDevice == null) {
            if (other.textMessageSourceDevice != null)
                return false;
        }
        else if (!textMessageSourceDevice.equals(other.textMessageSourceDevice))
            return false;
        return true;
    }
}