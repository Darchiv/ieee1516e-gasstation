package CarWash;

import Checkout.CheckoutFederate;
import RtiObjects.Ambassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import util.Uint32;

public class CarWashFederateAmbassador extends Ambassador {

    private CarWashFederate federate;

    public CarWashFederateAmbassador(CarWashFederate federate) {
        super("CarWashFederateAmbassador");
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

        if (interactionClass.equals(this.federate.washPaidInteractHandle)) {
            byte[] vehicleIdRaw = theParameters.get(this.federate.washPaidVehicleIdParamHandle);
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
