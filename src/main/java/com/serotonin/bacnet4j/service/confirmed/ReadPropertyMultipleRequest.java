/*
 * ============================================================================
 * GNU General Public License
 * ============================================================================
 *
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * When signing a commercial license with Infinite Automation Software,
 * the following extension to GPL is made. A special exception to the GPL is
 * included to allow you to distribute a combined work that includes BAcnet4J
 * without being obliged to provide the source code for any proprietary components.
 *
 * See www.infiniteautomation.com for commercial license options.
 *
 * @author Matthew Lohbihler
 */
package com.serotonin.bacnet4j.service.confirmed;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetErrorException;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.ObjectPropertyTypeDefinition;
import com.serotonin.bacnet4j.service.acknowledgement.AcknowledgementService;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyMultipleAck;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyReference;
import com.serotonin.bacnet4j.type.constructed.ReadAccessResult;
import com.serotonin.bacnet4j.type.constructed.ReadAccessResult.Result;
import com.serotonin.bacnet4j.type.constructed.ReadAccessSpecification;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.sero.ByteQueue;

public class ReadPropertyMultipleRequest extends ConfirmedRequestService {
    public static final byte TYPE_ID = 14;

    private final SequenceOf<ReadAccessSpecification> listOfReadAccessSpecs;

    public ReadPropertyMultipleRequest(final SequenceOf<ReadAccessSpecification> listOfReadAccessSpecs) {
        this.listOfReadAccessSpecs = listOfReadAccessSpecs;
    }

    @Override
    public byte getChoiceId() {
        return TYPE_ID;
    }

    @Override
    public void write(final ByteQueue queue) {
        write(queue, listOfReadAccessSpecs);
    }

    ReadPropertyMultipleRequest(final ByteQueue queue) throws BACnetException {
        listOfReadAccessSpecs = readSequenceOf(queue, ReadAccessSpecification.class);
    }

    @Override
    public AcknowledgementService handle(final LocalDevice localDevice, final Address from) throws BACnetException {
        BACnetObject obj;
        ObjectIdentifier oid;
        final List<ReadAccessResult> readAccessResults = new ArrayList<>();
        List<Result> results;

        try {
            for (final ReadAccessSpecification req : listOfReadAccessSpecs) {
                results = new ArrayList<>();
                oid = req.getObjectIdentifier();
                obj = localDevice.getObjectRequired(oid);

                for (final PropertyReference propRef : req.getListOfPropertyReferences())
                    addProperty(obj, results, propRef.getPropertyIdentifier(), propRef.getPropertyArrayIndex());

                readAccessResults.add(new ReadAccessResult(oid, new SequenceOf<>(results)));
            }
        } catch (final BACnetServiceException e) {
            throw new BACnetErrorException(getChoiceId(), e);
        }

        return new ReadPropertyMultipleAck(new SequenceOf<>(readAccessResults));
    }

    public SequenceOf<ReadAccessSpecification> getListOfReadAccessSpecs() {
        return listOfReadAccessSpecs;
    }

    public int getNumberOfProperties() {
        int sum = 0;
        for (final ReadAccessSpecification spec : listOfReadAccessSpecs) {
            sum += spec.getNumberOfProperties();
        }
        return sum;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (listOfReadAccessSpecs == null ? 0 : listOfReadAccessSpecs.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ReadPropertyMultipleRequest other = (ReadPropertyMultipleRequest) obj;
        if (listOfReadAccessSpecs == null) {
            if (other.listOfReadAccessSpecs != null)
                return false;
        } else if (!listOfReadAccessSpecs.equals(other.listOfReadAccessSpecs))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ReadPropertyMultipleRequest [listOfReadAccessSpecs=" + listOfReadAccessSpecs + "]";
    }

    private void addProperty(final BACnetObject obj, final List<Result> results, final PropertyIdentifier pid,
            final UnsignedInteger pin) {
        if (pid.intValue() == PropertyIdentifier.all.intValue()) {
            for (final ObjectPropertyTypeDefinition def : ObjectProperties
                    .getObjectPropertyTypeDefinitions(obj.getId().getObjectType()))
                addProperty(obj, results, def.getPropertyTypeDefinition().getPropertyIdentifier(), pin);
        } else if (pid.intValue() == PropertyIdentifier.required.intValue()) {
            for (final ObjectPropertyTypeDefinition def : ObjectProperties
                    .getRequiredObjectPropertyTypeDefinitions(obj.getId().getObjectType()))
                addProperty(obj, results, def.getPropertyTypeDefinition().getPropertyIdentifier(), pin);
        } else if (pid.intValue() == PropertyIdentifier.optional.intValue()) {
            for (final ObjectPropertyTypeDefinition def : ObjectProperties
                    .getOptionalObjectPropertyTypeDefinitions(obj.getId().getObjectType()))
                addProperty(obj, results, def.getPropertyTypeDefinition().getPropertyIdentifier(), pin);
        } else {
            // Get the specified property.
            try {
                results.add(new Result(pid, pin, obj.readPropertyRequired(pid, pin)));
            } catch (final BACnetServiceException e) {
                results.add(new Result(pid, pin, new ErrorClassAndCode(e.getErrorClass(), e.getErrorCode())));
            }
        }
    }
}
