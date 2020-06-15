package Checkout;

import Entry.EntryFederate;
import Entry.EntryFederateAmbassador;
import RtiObjects.Federate;
import RtiObjects.RtiObjectFactory;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.exceptions.RTIexception;
import util.Uint32;

public class CheckoutFederate extends Federate {
    // Refueled interaction
    protected InteractionClassHandle refueledInteractHandle;
    protected ParameterHandle refueledVehicleIdParamHandle;
    protected ParameterHandle refueledGasPumpIdParamHandle;

    // FuelPaid interaction
    protected InteractionClassHandle fuelPaidInteractHandle;
    protected ParameterHandle fuelPaidVehicleIdParamHandle;
    protected ParameterHandle fuelPaidGasPumpIdParamHandle;

    public CheckoutFederate(String name) {
        super(name);
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        // Subscribe Refueled interaction

        this.refueledInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.Refueled");
        this.refueledVehicleIdParamHandle = rtiamb.getParameterHandle(this.refueledInteractHandle, "vehicleId");
        this.refueledGasPumpIdParamHandle = rtiamb.getParameterHandle(this.refueledInteractHandle, "gasPumpId");
        rtiamb.subscribeInteractionClass(this.refueledInteractHandle);

        // Publish FuelPaid interaction

        this.fuelPaidInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.FuelPaid");
        this.fuelPaidVehicleIdParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "vehicleId");
        this.fuelPaidGasPumpIdParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "gasPumpId");
        rtiamb.publishInteractionClass(this.fuelPaidInteractHandle);

        this.log("Published and Subscribed");
    }

    void sendFuelPaid(int vehicleId, int gasPumpId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
        parameters.put(this.fuelPaidVehicleIdParamHandle, new Uint32(vehicleId).getByteArray());
        parameters.put(this.fuelPaidGasPumpIdParamHandle, new Uint32(gasPumpId).getByteArray());
        rtiamb.sendInteraction(this.fuelPaidInteractHandle, parameters, generateTag());
    }

    void onRefueled(int vehicleId, int gasPumpId) {
        // TODO: Handle that
    }

    protected void runSimulation() throws RTIexception {
        for (int i = 0; i < ITERATIONS; i++) {

            this.advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }
    }

    public static void main(String[] args) {
        String federateName = "CheckoutFederate";

        CheckoutFederate checkoutFederate = new CheckoutFederate(federateName);
        checkoutFederate.assignAmbassador(new CheckoutFederateAmbassador(checkoutFederate));

        try {
            checkoutFederate.runFederate(federateName);
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
