package Lanes;

import RtiObjects.*;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.exceptions.RTIexception;
import util.FuelEnum;
import util.Uint32;

import java.util.*;

public class LanesFederate extends Federate {
    protected Map<Integer, FuelEnum> vehicleFuelTypeById = new HashMap<>();
    int lastLaneId = 1;
    // Lane object
    protected List<LaneInfo> lanes = new ArrayList();

    // GetClientL1 interaction
    protected InteractionClassHandle getClientL1InteractHandle;
    protected ParameterHandle getClientL1VehicleIdParamHandle;

    // GetClientL2 interaction
    protected InteractionClassHandle getClientL2InteractHandle;
    protected ParameterHandle getClientL2VehicleIdParamHandle;

    // GasPumpOpen interaction
    protected InteractionClassHandle gasPumpOpenInteractHandle;
    protected ParameterHandle gasPumpOpenGasPumpIdParamHandle;
    protected ParameterHandle gasPumpOpenFuelTypeParamHandle;

    public LanesFederate(String name) {
        super(name);
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);

        // Publish and subscribe Lane object

        rtiObjectFactory.registerLane(true, true);
        rtiObjectFactory.registerEntryQueue(true, true);
        rtiObjectFactory.registerVehicle(false, true);

        // Publish GetClientL1 interaction

        this.getClientL1InteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.GetClientL1");
        this.getClientL1VehicleIdParamHandle = rtiamb.getParameterHandle(this.getClientL1InteractHandle, "vehicleId");
        rtiamb.publishInteractionClass(this.getClientL1InteractHandle);

        // Subscribe GetClientL2 interaction

        this.getClientL2InteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.GetClientL2");
        this.getClientL2VehicleIdParamHandle = rtiamb.getParameterHandle(this.getClientL2InteractHandle, "vehicleId");
        rtiamb.subscribeInteractionClass(this.getClientL2InteractHandle);

        // Subscribe GasPumpOpen interaction

        this.gasPumpOpenInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.GasPumpOpen");
        this.gasPumpOpenGasPumpIdParamHandle = rtiamb.getParameterHandle(this.gasPumpOpenInteractHandle, "gasPumpId");
        this.gasPumpOpenFuelTypeParamHandle = rtiamb.getParameterHandle(this.gasPumpOpenInteractHandle, "fuelType");
        rtiamb.subscribeInteractionClass(this.gasPumpOpenInteractHandle);


        this.log("Published and Subscribed");
    }

    void sendGetClientL1(int vehicleId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);
        Uint32 value = new Uint32(vehicleId);
        parameters.put(this.getClientL1VehicleIdParamHandle, value.getByteArray());
        rtiamb.sendInteraction(this.getClientL1InteractHandle, parameters, generateTag());
    }

    void onGetClientL2(int vehicleId) {
        this.log("GetClientL2(" + vehicleId + ")");
    }

    void onGasPumpOpen(int gasPumpId, FuelEnum fuelType) throws RTIexception {
        this.log("GasPumpOpen(" + gasPumpId + ", " + fuelType.getValue() + ")");
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);
        int earliestVehicleId = 0; // TODO: Get and assign last vehicle ID from Entry EntryQueue

        Lane lane = rtiObjectFactory.createLane();
        lane.setInitialAttributeValues(gasPumpId, 0, 5, earliestVehicleId);
        lanes.add(new LaneInfo(lastLaneId, lane, fuelType));
        lastLaneId += 1;
    }

    void onUpdatedEntryQueue(int currentVehicleCount, int earliestVehicleId) throws RTIexception {
        log("EntryQueue(currentVehicleCount=" + currentVehicleCount + ", earliestVehicleId=" + earliestVehicleId + ") updated");
        if (currentVehicleCount == 0) {
            return;
        }

        int vehicleId = earliestVehicleId;

        for (LaneInfo laneInfo : lanes) {
            FuelEnum fuelType = vehicleFuelTypeById.get(vehicleId);
//            log("onUpdatedEntryQueue: iter vehicleFuelType=" + fuelType.getValue() + ", laneId=" + laneInfo.id + "," +
//                    "currentVehicleCount=" + laneInfo.lane.getCurrentVehicleCount() + ", maxVehicles=" + laneInfo.lane.getMaxVehicles() +
//                    "laneFuelType=" + laneInfo.fuelType.getValue());
            if (laneInfo.lane.getCurrentVehicleCount() < laneInfo.lane.getMaxVehicles()
                    && laneInfo.fuelType.equals(fuelType)) {
                laneInfo.vehicleQueue.add(vehicleId);
                log("Added vehicleId=" + vehicleId + " (fuelType=" + fuelType.getValue() + ") to lane id=" + laneInfo.id);
                this.sendGetClientL1(vehicleId);
                break;
            }
        }
    }

    void onUpdatedVehicle(int vehicleId, FuelEnum fuelType) {
        log("Vehicle(" + vehicleId + ", fuelType=" + fuelType.getValue() + ") updated");
        vehicleFuelTypeById.put(vehicleId, fuelType);
    }

    @Override
    protected void processEvents() throws RTIexception {
        while (!events.isEmpty()) {
            Object event = events.remove();

            if (event instanceof GasPumpOpen) {
                GasPumpOpen gasPumpOpen = (GasPumpOpen) event;
                onGasPumpOpen(gasPumpOpen.getGasPumpId(), gasPumpOpen.getFuelType());
            } else if (event instanceof GetClientL2) {
                GetClientL2 getClientL2 = (GetClientL2) event;
                onGetClientL2(getClientL2.getVehicleId());
            } else if (event instanceof EntryQueue) {
                EntryQueue entryQueue = (EntryQueue) event;
                onUpdatedEntryQueue(entryQueue.getCurrentVehicleCount(), entryQueue.getEarliestVehicleId());
            } else if (event instanceof Vehicle) {
                Vehicle vehicle = (Vehicle) event;
                onUpdatedVehicle(vehicle.getId(), vehicle.getFuelType());
            }
        }
    }

    protected void runSimulation() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);

        while (this.getTimeAsInt() < END_TIME) {

            this.advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }

    }

    public static void main(String[] args) {
        String federateName = "LanesFederate";

        LanesFederate lanesFederate = new LanesFederate(federateName);
        lanesFederate.assignAmbassador(new LanesFederateAmbassador(lanesFederate));

        try {
            lanesFederate.runFederate(federateName);
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }

    private class LaneInfo {
        public int id;
        public Lane lane;
        public FuelEnum fuelType;
        public Queue<Integer> vehicleQueue = new LinkedList<>();

        public LaneInfo(int id, Lane lane, FuelEnum fuelType) {
            this.id = id;
            this.lane = lane;
            this.fuelType = fuelType;
        }
    }
}
