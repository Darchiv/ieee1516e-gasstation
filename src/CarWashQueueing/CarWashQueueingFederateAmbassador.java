package CarWashQueueing;

import CarWash.CarWashFederate;
import RtiObjects.Ambassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import util.Uint32;

public class CarWashQueueingFederateAmbassador extends Ambassador {

    private CarWashQueueingFederate federate;

    public CarWashQueueingFederateAmbassador(CarWashQueueingFederate federate) {
        super("CarWashQueueingFederateAmbassador");
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
                                   FederateAmbassador.SupplementalReceiveInfo receiveInfo)
            throws FederateInternalError {
        super.receiveInteraction(interactionClass, theParameters, tag, sentOrdering, theTransport, time, receivedOrdering, receiveInfo);

        if (interactionClass.equals(this.federate.goWashInteractHandle)) {
            byte[] vehicleIdRaw = theParameters.get(this.federate.goWashVehicleIdParamHandle);
            if (vehicleIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: vehicleId");
            }

            int vehicleId = new Uint32(vehicleIdRaw).getValue();
            this.federate.onWashPaid(vehicleId);
        } else {
            throw new RuntimeException("A non-subscribed interaction was received: " + interactionClass);
        }
    }
}