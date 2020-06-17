package GasPump;

import RtiObjects.Ambassador;
import RtiObjects.FuelPaid;
import RtiObjects.Lane;
import RtiObjects.Vehicle;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import util.FuelEnum;
import util.Uint32;

public class GasPumpFederateAmbassador extends Ambassador {
    private GasPumpFederate federate;

    @Override
    public void discoverObjectInstance(ObjectInstanceHandle theObject,
                                       ObjectClassHandle theObjectClass,
                                       String objectName)
            throws FederateInternalError {
        super.discoverObjectInstance(theObject, theObjectClass, objectName);

        instanceToClassMap.put(theObject, theObjectClass);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject,
                                       AttributeHandleValueMap theAttributes,
                                       byte[] tag,
                                       OrderType sentOrdering,
                                       TransportationTypeHandle theTransport,
                                       LogicalTime time,
                                       OrderType receivedOrdering,
                                       SupplementalReflectInfo reflectInfo)
            throws FederateInternalError {
//        super.reflectAttributeValues(theObject, theAttributes, tag, sentOrdering, theTransport, time, receivedOrdering, reflectInfo);

        if (instanceToClassMap.get(theObject).equals(Lane.getClassHandle())) {
            Uint32 gasPumpId = null;
            Uint32 maxVehicles = null;
            Uint32 currentVehicleCount = null;
            Uint32 earliestVehicleId = null;

            byte[] gasPumpIdRaw = theAttributes.get(Lane.getGasPumpIdAttrHandle());
            if (gasPumpIdRaw != null) {
                gasPumpId = new Uint32(gasPumpIdRaw);
            }

            byte[] maxVehiclesRaw = theAttributes.get(Lane.getMaxVehiclesAttrHandle());
            if (maxVehiclesRaw != null) {
                maxVehicles = new Uint32(maxVehiclesRaw);
            }

            byte[] currentVehicleCountRaw = theAttributes.get(Lane.getCurrentVehicleCountAttrHandle());
            if (currentVehicleCountRaw != null) {
                currentVehicleCount = new Uint32(currentVehicleCountRaw);
            }

            byte[] earliestVehicleIdRaw = theAttributes.get(Lane.getEarliestVehicleIdAttrHandle());
            if (earliestVehicleIdRaw != null) {
                earliestVehicleId = new Uint32(earliestVehicleIdRaw);
            }

            int maxVehiclesVal = maxVehicles != null ? maxVehicles.getValue() : 0;
            this.federate.events.add(new Lane(gasPumpId.getValue(), currentVehicleCount.getValue(), maxVehiclesVal, earliestVehicleId.getValue()));
        } else if (instanceToClassMap.get(theObject).equals(Vehicle.getClassHandle())) {
            Uint32 id = null;
            Uint32 timeEntered = null;
            FuelEnum fuelType = null;

            byte[] idRaw = theAttributes.get(Vehicle.getIdAttrHandle());
            if (idRaw != null) {
                id = new Uint32(idRaw);
            }

            byte[] timeEnteredRaw = theAttributes.get(Vehicle.getTimeEnteredAttrHandle());
            if (timeEnteredRaw != null) {
                timeEntered = new Uint32(timeEnteredRaw);
            }

            byte[] fuelTypeRaw = theAttributes.get(Vehicle.getFuelTypeAttrHandle());
            if (fuelTypeRaw != null) {
                fuelType = new FuelEnum(fuelTypeRaw);
            }

            this.federate.events.add(new Vehicle(id.getValue(), false, timeEntered.getValue(), fuelType));
        }
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
        } else {
            throw new RuntimeException("A non-subscribed interaction was received: " + interactionClass);
        }
    }

    public GasPumpFederateAmbassador(GasPumpFederate federate) {
        super("GasPumpFederateAmbassador");
        this.federate = federate;
    }
}
