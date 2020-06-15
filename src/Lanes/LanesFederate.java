package Lanes;

import RtiObjects.Federate;
import RtiObjects.Lane;
import RtiObjects.RtiObjectFactory;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.exceptions.RTIexception;
import util.FuelEnum;
import util.Uint32;

public class LanesFederate extends Federate {
    // Lane object
    // TODO: Lanes list (uninitialized)

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

    void onGasPumpOpen(int gasPumpId, FuelEnum fuelType) {
        this.log("GasPumpOpen(" + gasPumpId + ", " + fuelType.getValue() + ")");
    }

    void onUpdatedEntryQueue(int currentVehicleCount, int earliestVehicleId) {
        // TODO: Add the vehicle to some lane queue if possible
        // TODO: Use sendGetClientL1() to send interacion to EntryQueue
    }

    protected void runSimulation() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);

        // TODO: Create as many Lane instances (with appropriate IDs, etc.) as needed and put them into some list
        Lane lane = rtiObjectFactory.createLane();
        lane.setInitialAttributeValues(1, 0, 5, 0);

        for (int i = 0; i < ITERATIONS; i++) {

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
}
