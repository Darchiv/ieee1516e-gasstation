package Checkout;

import RtiObjects.Ambassador;
import RtiObjects.Refueled;
import RtiObjects.Washed;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import util.Uint32;

public class CheckoutFederateAmbassador extends Ambassador {
    private CheckoutFederate federate;

    public CheckoutFederateAmbassador(CheckoutFederate federate) {
        super("CheckoutFederateAmbassador");
        this.federate = federate;
    }

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

        if (interactionClass.equals(this.federate.refueledInteractHandle)) {
            byte[] vehicleIdRaw = theParameters.get(this.federate.refueledVehicleIdParamHandle);
            if (vehicleIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: vehicleId");
            }

            byte[] gasPumpIdRaw = theParameters.get(this.federate.refueledGasPumpIdParamHandle);
            if (gasPumpIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: gasPumpId");
            }

            int vehicleId = new Uint32(vehicleIdRaw).getValue();
            int gasPumpId = new Uint32(gasPumpIdRaw).getValue();
            this.federate.events.add(new Refueled(vehicleId, gasPumpId));
        } else if (interactionClass.equals(this.federate.washedInteractHandle)) {
            byte[] vehicleIdRaw = theParameters.get(this.federate.washedVehicleIdParamHandle);
            if (vehicleIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: vehicleId");
            }

            int vehicleId = new Uint32(vehicleIdRaw).getValue();
            this.federate.events.add(new Washed(vehicleId));
        } else {
            throw new RuntimeException("A non-subscribed interaction was received: " + interactionClass);
        }
    }
}
