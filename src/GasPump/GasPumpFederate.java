package GasPump;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.RTIexception;
import util.Federate;
import util.FuelEnum;
import util.Uint32;

public class GasPumpFederate extends Federate {
    // GasPump object
    protected ObjectClassHandle gasPumpClassHandle;
    protected AttributeHandle gasPumpIsBusyAttrHandle;
    protected AttributeHandle gasPumpCurrentVehicleIdAttrHandle;
    protected AttributeHandle gasPumpFuelTypeAttrHandle;

    // Vehicle object
    protected ObjectClassHandle vehicleClassHandle;
    protected AttributeHandle vehicleIdAttrHandle;
    protected AttributeHandle vehicleIsFilled;
    protected AttributeHandle vehicleTimeEntered;
    protected AttributeHandle vehicleFuelType;

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
    protected ParameterHandle fuelPaidGasPumpIdParamHandle;
    protected ParameterHandle fuelPaidFuelTypeParamHandle;

    public GasPumpFederate(String name) {
        super(name);
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        // Publish and subscribe GasPump object

        this.gasPumpClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.GasPump");
        this.gasPumpIsBusyAttrHandle = rtiamb.getAttributeHandle(this.gasPumpClassHandle, "isBusy");
        this.gasPumpCurrentVehicleIdAttrHandle = rtiamb.getAttributeHandle(this.gasPumpClassHandle, "currentVehicleId");
        this.gasPumpFuelTypeAttrHandle = rtiamb.getAttributeHandle(this.gasPumpClassHandle, "fuelType");

        AttributeHandleSet gasPumpAttribute = rtiamb.getAttributeHandleSetFactory().create();
        gasPumpAttribute.add(this.gasPumpIsBusyAttrHandle);
        gasPumpAttribute.add(this.gasPumpCurrentVehicleIdAttrHandle);
        gasPumpAttribute.add(this.gasPumpFuelTypeAttrHandle);

        rtiamb.publishObjectClassAttributes(this.gasPumpClassHandle, gasPumpAttribute);
        rtiamb.subscribeObjectClassAttributes(this.gasPumpClassHandle, gasPumpAttribute);

        // Publish and subscribe Vehicle object

        this.vehicleClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Vehicle");
        this.vehicleIdAttrHandle = rtiamb.getAttributeHandle(this.vehicleClassHandle, "isBusy");
        this.vehicleIsFilled = rtiamb.getAttributeHandle(this.vehicleClassHandle, "isFilled");
        this.vehicleTimeEntered = rtiamb.getAttributeHandle(this.vehicleClassHandle, "timeEntered");
        this.vehicleFuelType = rtiamb.getAttributeHandle(this.vehicleClassHandle, "fuelType");

        AttributeHandleSet vehicleAttribute = rtiamb.getAttributeHandleSetFactory().create();
        gasPumpAttribute.add(this.vehicleIdAttrHandle);
        gasPumpAttribute.add(this.vehicleIsFilled);
        gasPumpAttribute.add(this.vehicleTimeEntered);
        gasPumpAttribute.add(this.vehicleFuelType);

        rtiamb.publishObjectClassAttributes(this.vehicleClassHandle, vehicleAttribute);
        rtiamb.subscribeObjectClassAttributes(this.vehicleClassHandle, vehicleAttribute);

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
        this.fuelPaidGasPumpIdParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "gasPumpId");
        this.fuelPaidFuelTypeParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "fuelType");
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
        rtiamb.sendInteraction(this.getClientL2InteractHandle, parameters, generateTag());
    }

    protected void runSimulation() throws RTIexception {


        for (int i = 0; i < ITERATIONS; i++) {
            if (i % 5 == 0) {
                this.sendGasPumpOpen(1,"diesel");
            }
            if (i % 6 == 0) {
                this.sendGetClientL2(10);
            }
            this.advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }


    }

    public static void main(String[] args) {
        String federateName = "LanesFederate";

        GasPumpFederate gasPumpFederate = new GasPumpFederate(federateName);
        gasPumpFederate.assignAmbassador(new GasPumpFederateAmbassador(gasPumpFederate));

        try {
            gasPumpFederate.runFederate(federateName);
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
