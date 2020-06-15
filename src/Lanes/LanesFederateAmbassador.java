package Lanes;

import RtiObjects.Ambassador;
import RtiObjects.EntryQueue;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import util.FuelEnum;
import util.Uint32;

import java.util.HashMap;
import java.util.Map;

public class LanesFederateAmbassador extends Ambassador {
    private LanesFederate federate;
    private Map<ObjectInstanceHandle, ObjectClassHandle> instanceToClassMap = new HashMap<>();

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
        super.reflectAttributeValues(theObject, theAttributes, tag, sentOrdering, theTransport, time, receivedOrdering, reflectInfo);

        if (instanceToClassMap.get(theObject).equals(EntryQueue.getClassHandle())) {
            Uint32 maxVehicles = null;
            Uint32 currentVehicleCount = null;
            Uint32 earliestVehicleId = null;

            byte[] maxVehiclesRaw = theAttributes.get(EntryQueue.getMaxVehiclesAttrHandle());
            if (maxVehiclesRaw != null) {
                maxVehicles = new Uint32(maxVehiclesRaw);
            }

            byte[] currentVehicleCountRaw = theAttributes.get(EntryQueue.getCurrentVehicleCountAttrHandle());
            if (currentVehicleCountRaw != null) {
                currentVehicleCount = new Uint32(currentVehicleCountRaw);
            }

            byte[] earliestVehicleIdRaw = theAttributes.get(EntryQueue.getEarliestVehicleIdAttrHandle());
            if (earliestVehicleIdRaw != null) {
                earliestVehicleId = new Uint32(earliestVehicleIdRaw);
            }

            // TODO: Maybe use maxVehicles
            this.federate.onUpdatedEntryQueue(currentVehicleCount.getValue(), earliestVehicleId.getValue());
        }
    }
}
