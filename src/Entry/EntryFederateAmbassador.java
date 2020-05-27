package Entry;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import util.Ambassador;
import util.Uint32;

public class EntryFederateAmbassador extends Ambassador {
    private EntryFederate federate;

    public EntryFederateAmbassador(EntryFederate federate) {
        super("EntryFederateAmbassador");
        this.federate = federate;
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
        super.receiveInteraction(interactionClass, theParameters, tag, sentOrdering, theTransport, time, receivedOrdering, receiveInfo);

        if (interactionClass.equals(this.federate.newClientInteractHandle)) {
            byte[] vehicleIdRaw = theParameters.get(this.federate.newClientVehicleIdParamHandle);
            if (vehicleIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: vehicleId");
            }

            int vehicleId = new Uint32(vehicleIdRaw).getValue();
            this.federate.onNewClient(vehicleId);
        } else if (interactionClass.equals(this.federate.getClientL1InteractHandle)) {
            byte[] vehicleIdRaw = theParameters.get(this.federate.getClientL1VehicleIdParamHandle);
            if (vehicleIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: vehicleId");
            }

            int vehicleId = new Uint32(vehicleIdRaw).getValue();
            this.federate.onGetClientL1(vehicleId);
        } else {
            throw new RuntimeException("A non-subscribed interaction was received: " + interactionClass);
        }
    }
}
