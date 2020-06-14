package Lanes;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import RtiObjects.Ambassador;
import util.FuelEnum;
import util.Uint32;

public class LanesFederateAmbassador extends Ambassador {
    private LanesFederate federate;

//    TODO: Implement reflections

    public LanesFederateAmbassador(LanesFederate federate) {
        super("LanesFederateAmbassador");
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

        if (interactionClass.equals(this.federate.getClientL2InteractHandle)) {
            byte[] vehicleIdRaw = theParameters.get(this.federate.getClientL2VehicleIdParamHandle);
            if (vehicleIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: vehicleId");
            }

            int vehicleId = new Uint32(vehicleIdRaw).getValue();
            this.federate.onGetClientL2(vehicleId);
        } else if (interactionClass.equals(this.federate.gasPumpOpenInteractHandle)) {
            byte[] gasPumpIdRaw = theParameters.get(this.federate.gasPumpOpenGasPumpIdParamHandle);
            if (gasPumpIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: gasPumpId");
            }

            byte[] fuelTypeRaw = theParameters.get(this.federate.gasPumpOpenFuelTypeParamHandle);
            if (fuelTypeRaw == null) {
                throw new RuntimeException("Required parameter not supplied: fuelTypeRaw");
            }

            int gasPumpId = new Uint32(gasPumpIdRaw).getValue();
            FuelEnum fuelType = new FuelEnum(fuelTypeRaw);
            this.federate.onGasPumpOpen(gasPumpId, fuelType);
        } else {
            throw new RuntimeException("A non-subscribed interaction was received: " + interactionClass);
        }
    }
}
