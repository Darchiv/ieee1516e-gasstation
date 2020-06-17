package GasPump;

import RtiObjects.*;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.exceptions.RTIexception;
import util.FuelEnum;
import util.Uint32;

import java.util.*;

public class GasPumpFederate extends Federate {
    protected Map<Integer, FuelEnum> vehicleFuelTypeById = new HashMap<>();
    protected Map<Integer, Integer> vehicleTimeEnteredById = new HashMap<>();
    List<GasPumpInfo> gasPumpInfos = new ArrayList<>();
    List<Integer> queueWaitingTimes = new ArrayList<>();

    // GetClientL2 interaction
    protected InteractionClassHandle getClientL2InteractHandle;
    protected ParameterHandle getClientL2VehicleIdParamHandle;

    // Refueled interaction
    protected InteractionClassHandle refueledInteractHandle;
    protected ParameterHandle refueledVehicleIdParamHandle;
    protected ParameterHandle refueledGasPumpIdParamHandle;

    // GasPumpOpen interaction
    protected InteractionClassHandle gasPumpOpenInteractHandle;
    protected ParameterHandle gasPumpOpenGasPumpIdParamHandle;
    protected ParameterHandle gasPumpOpenFuelTypeParamHandle;

    // FuelPaid interaction
    protected InteractionClassHandle fuelPaidInteractHandle;
    protected ParameterHandle fuelPaidVehicleIdParamHandle;
    protected ParameterHandle fuelPaidGasPumpIdParamHandle;

    public GasPumpFederate(String name) {
        super(name);
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);

        rtiObjectFactory.registerVehicle(true, true);
        rtiObjectFactory.registerGasPump(true, true);
        rtiObjectFactory.registerLane(true, true);

        // Publish GetClientL2 interaction

        this.getClientL2InteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.GetClientL2");
        this.getClientL2VehicleIdParamHandle = rtiamb.getParameterHandle(this.getClientL2InteractHandle, "vehicleId");
        rtiamb.publishInteractionClass(this.getClientL2InteractHandle);

        // Publish Refueled interaction

        this.refueledInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.Refueled");
        this.refueledVehicleIdParamHandle = rtiamb.getParameterHandle(this.refueledInteractHandle, "vehicleId");
        this.refueledGasPumpIdParamHandle = rtiamb.getParameterHandle(this.refueledInteractHandle, "gasPumpId");
        rtiamb.publishInteractionClass(this.refueledInteractHandle);

        // Publish GasPumpOpen interaction

        this.gasPumpOpenInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.GasPumpOpen");
        this.gasPumpOpenGasPumpIdParamHandle = rtiamb.getParameterHandle(this.gasPumpOpenInteractHandle, "gasPumpId");
        this.gasPumpOpenFuelTypeParamHandle = rtiamb.getParameterHandle(this.gasPumpOpenInteractHandle, "fuelType");
        rtiamb.publishInteractionClass(this.gasPumpOpenInteractHandle);

        // Subscribe FuelPaid interaction

        this.fuelPaidInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.FuelPaid");
        this.fuelPaidVehicleIdParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "vehicleId");
        this.fuelPaidGasPumpIdParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "gasPumpId");
        rtiamb.subscribeInteractionClass(this.fuelPaidInteractHandle);

        this.log("Published and Subscribed");
    }

    void sendGetClientL2(int vehicleId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);
        Uint32 value = new Uint32(vehicleId);
        parameters.put(this.getClientL2VehicleIdParamHandle, value.getByteArray());
        rtiamb.sendInteraction(this.getClientL2InteractHandle, parameters, generateTag());
    }

    void sendGasPumpOpen(int gasPumpId, String fuelType) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
        Uint32 hlaGasPumpId = new Uint32(gasPumpId);
        FuelEnum hlaFuelType = new FuelEnum(fuelType);
        parameters.put(this.gasPumpOpenGasPumpIdParamHandle, hlaGasPumpId.getByteArray());
        parameters.put(this.gasPumpOpenFuelTypeParamHandle, hlaFuelType.getByteArray());
        rtiamb.sendInteraction(this.gasPumpOpenInteractHandle, parameters, generateTag());
    }

    void sendRefueled(int vehicleId, int gasPumpId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
        parameters.put(this.refueledVehicleIdParamHandle, new Uint32(vehicleId).getByteArray());
        parameters.put(this.refueledGasPumpIdParamHandle, new Uint32(gasPumpId).getByteArray());
        rtiamb.sendInteraction(this.refueledInteractHandle, parameters, generateTag());
    }

    void onFuelPaid(int vehicleId, int gasPumpId) {
        // TODO: Handle that
    }

    void onUpdatedLane(int gasPumpId, int currentVehicleCount, int earliestVehicleId) throws RTIexception {
        log("Lane(gasPumpId=" + gasPumpId + ", currentVehicleCount=" + currentVehicleCount + ", earliestVehicleId=" + earliestVehicleId + ") updated");

        if (currentVehicleCount == 0) {
            return;
        }

        for (GasPumpInfo gasPumpInfo : gasPumpInfos) {
            if (gasPumpId == gasPumpInfo.gasPump.getId() && !gasPumpInfo.gasPump.isBusy()) {
                int vehicleId = earliestVehicleId;
                int waitTime = this.getTimeAsInt() - vehicleTimeEnteredById.get(vehicleId);
                int finishTime = this.getTimeAsInt() + 4 + this.random.nextInt(10);

                log("Adding Vehicle(id=" + vehicleId + ") to GasPump(id=" + gasPumpInfo.gasPump.getId() + ") to finish at "
                        + finishTime + ", after waiting in queue for " + waitTime);
                queueWaitingTimes.add(waitTime);
                gasPumpInfo.finishTime = finishTime;
                gasPumpInfo.currentVehicleId = vehicleId;
                gasPumpInfo.gasPump.setIsBusy(true);
                sendGetClientL2(vehicleId);
                break;
            }
        }
    }

    void onUpdatedVehicle(int vehicleId, int timeEntered, FuelEnum fuelType) {
        log("Vehicle(" + vehicleId + ", timeEntered=" + timeEntered + ",fuelType=" + fuelType.getValue() + ") updated");
        vehicleFuelTypeById.put(vehicleId, fuelType);
        vehicleTimeEnteredById.put(vehicleId, timeEntered);
    }

    @Override
    protected void processEvents() throws RTIexception {
        while (!events.isEmpty()) {
            Object event = events.remove();

            if (event instanceof FuelPaid) {
                FuelPaid fuelPaid = (FuelPaid) event;
                onFuelPaid(fuelPaid.getVehicleId(), fuelPaid.getGasPumpId());
            } else if (event instanceof Vehicle) {
                Vehicle vehicle = (Vehicle) event;
                onUpdatedVehicle(vehicle.getId(), vehicle.getTimeEntered(), vehicle.getFuelType());
            } else if (event instanceof Lane) {
                Lane lane = (Lane) event;
                onUpdatedLane(lane.getGasPumpId(), lane.getCurrentVehicleCount(), lane.getEarliestVehicleId());
            }
        }
    }

    protected void runSimulation() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);

        int gi = 1;
        for (; gi < 3; gi++) {
            GasPump gasPump = rtiObjectFactory.createGasPump();
            String fuelType = "diesel";
            gasPump.setInitialAttributeValues(gi, false, 0, new FuelEnum(fuelType));
            gasPumpInfos.add(new GasPumpInfo(gasPump));
            sendGasPumpOpen(gi, fuelType);
        }
        for (; gi < 5; gi++) {
            GasPump gasPump = rtiObjectFactory.createGasPump();
            String fuelType = "petrol";
            gasPump.setInitialAttributeValues(gi, false, 0, new FuelEnum(fuelType));
            gasPumpInfos.add(new GasPumpInfo(gasPump));
            sendGasPumpOpen(gi, fuelType);
        }

        while (this.getTimeAsInt() < END_TIME) {
            for (GasPumpInfo gasPumpInfo : gasPumpInfos) {
                if (gasPumpInfo.gasPump.isBusy() && gasPumpInfo.finishTime <= this.getTimeAsInt()) {
                    int vehicleId = gasPumpInfo.gasPump.getCurrentVehicleId();
                    int gasPumpId = gasPumpInfo.gasPump.getId();

                    log("GasPump(id=" + gasPumpId + ") finished refuelling Vehicle(id="
                            + vehicleId + ")");
//                    gasPumpInfo.gasPump.setIsBusy(false);
                    gasPumpInfo.finishTime = Integer.MAX_VALUE;
                    this.sendRefueled(vehicleId, gasPumpId);
                }
            }

            this.advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }

        log("Waiting times: ");
        for (Integer wt : queueWaitingTimes) {
            System.out.print(wt + ", ");
        }
        System.out.print("\r\n");

    }

    public static void main(String[] args) {
        String federateName = "GasPumpFederate";

        GasPumpFederate gasPumpFederate = new GasPumpFederate(federateName);
        gasPumpFederate.assignAmbassador(new GasPumpFederateAmbassador(gasPumpFederate));

        try {
            gasPumpFederate.runFederate(federateName);
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }

    private class GasPumpInfo {
        public int currentVehicleId;
        public GasPump gasPump;
        public int finishTime = 0;

        public GasPumpInfo(GasPump gasPump) {
            this.gasPump = gasPump;
        }
    }
}
