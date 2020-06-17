package GasPump;

import RtiObjects.*;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.exceptions.RTIexception;
import util.FuelEnum;
import util.Uint32;

import java.util.ArrayList;
import java.util.List;

public class GasPumpFederate extends Federate {
    List<GasPump> gasPumps = new ArrayList<>();

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

    void onUpdatedLane(int gasPumpId, int currentVehicleCount, int earliestVehicleId) {
        // TODO: Add the vehicle to appropriate gas pump and set it busy, schedule a "filled" event
        // TODO: Use sendGetClientL2() to send interacion to Lane
    }

    void onUpdatedVehicle(int vehicleId, FuelEnum fuelType) {
        // TODO: Store info about fueType of this vehicleId for future needs?
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
                onUpdatedVehicle(vehicle.getId(), vehicle.getFuelType());
            } else if (event instanceof Lane) {
                Lane lane = (Lane) event;
                onUpdatedLane(lane.getGasPumpId(), lane.getCurrentVehicleCount(), lane.getEarliestVehicleId());
            }
        }
    }

    protected void runSimulation() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);

        int gi = 0;
        for (; gi < 2; gi++) {
            GasPump gasPump = rtiObjectFactory.createGasPump();
            String fuelType = "diesel";
            gasPump.setInitialAttributeValues(gi, false, 0, new FuelEnum(fuelType));
            gasPumps.add(gasPump);
            sendGasPumpOpen(gi, fuelType);
        }
        for (; gi < 4; gi++) {
            GasPump gasPump = rtiObjectFactory.createGasPump();
            String fuelType = "petrol";
            gasPump.setInitialAttributeValues(gi, false, 0, new FuelEnum(fuelType));
            gasPumps.add(gasPump);
            sendGasPumpOpen(gi, fuelType);
        }

        while (this.getTimeAsInt() < END_TIME) {

            // TODO: Send Refueled interaction when a vehicle has been refuelled

            this.advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }


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
}
