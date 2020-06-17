/*
 *   Copyright 2012 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL)
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */
package Vehicle;

import RtiObjects.Ambassador;
import RtiObjects.FuelPaid;
import RtiObjects.WashPaid;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import util.Uint32;

public class VehicleFederateAmbassador extends Ambassador {
    private VehicleFederate federate;

    @Override
    public void discoverObjectInstance(ObjectInstanceHandle theObject,
                                       ObjectClassHandle theObjectClass,
                                       String objectName)
            throws FederateInternalError {
        super.discoverObjectInstance(theObject, theObjectClass, objectName);

        instanceToClassMap.put(theObject, theObjectClass);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass,
                                   ParameterHandleValueMap theParameters,
                                   byte[] tag,
                                   OrderType sentOrdering,
                                   TransportationTypeHandle theTransport,
                                   LogicalTime time,
                                   OrderType receivedOrdering,
                                   SupplementalReceiveInfo receiveInfo)
            throws FederateInternalError {
//        super.receiveInteraction(interactionClass, theParameters, tag, sentOrdering, theTransport, time, receivedOrdering, receiveInfo);

        if (interactionClass.equals(this.federate.fuelPaidInteractHandle)) {
            byte[] vehicleIdRaw = theParameters.get(this.federate.fuelPaidVehicleIdParamHandle);
            if (vehicleIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: vehicleId");
            }

            byte[] gasPumpIdRaw = theParameters.get(this.federate.fuelPaidGasPumpIdParamHandle);
            if (gasPumpIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: gasPumpId");
            }

            int vehicleId = new Uint32(vehicleIdRaw).getValue();
            int gasPumpId = new Uint32(gasPumpIdRaw).getValue();
            this.federate.events.add(new FuelPaid(vehicleId, gasPumpId));
        } else if (interactionClass.equals(this.federate.washPaidInteractHandle)) {
            byte[] vehicleIdRaw = theParameters.get(this.federate.washPaidVehicleIdParamHandle);
            if (vehicleIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: vehicleId");
            }

            int vehicleId = new Uint32(vehicleIdRaw).getValue();
            this.federate.events.add(new WashPaid(vehicleId));
        } else {
            throw new RuntimeException("A non-subscribed interaction was received: " + interactionClass);
        }
    }

    public VehicleFederateAmbassador(VehicleFederate federate) {
        super("VehicleFederateAmbassador");
        this.federate = federate;
    }
}
