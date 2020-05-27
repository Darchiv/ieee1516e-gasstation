package Lanes;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.RTIexception;
import util.Federate;
import util.Uint32;

public class LanesFederate extends Federate {
    // Lane object
    protected ObjectClassHandle laneClassHandle;
    protected AttributeHandle laneGasPumpIdAttrHandle;
    protected AttributeHandle laneCurrentVehicleCountAttrHandle;
    protected AttributeHandle laneMaxVehiclesAttrHandle;
    protected AttributeHandle laneEarliestVehicleIdAttrHandle;

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
        // Publish and subscribe Lane object

        this.laneClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Lane");
        this.laneGasPumpIdAttrHandle = rtiamb.getAttributeHandle(this.laneClassHandle, "gasPumpId");
        this.laneCurrentVehicleCountAttrHandle = rtiamb.getAttributeHandle(this.laneClassHandle, "currentVehicleCount");
        this.laneMaxVehiclesAttrHandle = rtiamb.getAttributeHandle(this.laneClassHandle, "maxVehicles");
        this.laneEarliestVehicleIdAttrHandle = rtiamb.getAttributeHandle(this.laneClassHandle, "earliestVehicleId");

        AttributeHandleSet laneAttributes = rtiamb.getAttributeHandleSetFactory().create();
        laneAttributes.add(this.laneGasPumpIdAttrHandle);
        laneAttributes.add(this.laneCurrentVehicleCountAttrHandle);
        laneAttributes.add(this.laneMaxVehiclesAttrHandle);
        laneAttributes.add(this.laneEarliestVehicleIdAttrHandle);

        rtiamb.publishObjectClassAttributes(this.laneClassHandle, laneAttributes);
        rtiamb.subscribeObjectClassAttributes(this.laneClassHandle, laneAttributes);

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

    protected void runSimulation() throws RTIexception {


        for (int i = 0; i < ITERATIONS; i++) {
            if (i % 5 == 0) {
                this.sendGetClientL1(10);
            }

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