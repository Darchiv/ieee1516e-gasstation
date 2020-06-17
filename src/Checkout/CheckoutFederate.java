package Checkout;

import RtiObjects.Federate;
import RtiObjects.Refueled;
import RtiObjects.Washed;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.exceptions.RTIexception;
import util.Uint32;

import java.util.LinkedList;
import java.util.Queue;

public class CheckoutFederate extends Federate {
    protected Refueled currentlyServed = null;
    protected Queue<Refueled> waitingQueue = new LinkedList<>();
    protected int finishTime;

    // Refueled interaction
    protected InteractionClassHandle refueledInteractHandle;
    protected ParameterHandle refueledVehicleIdParamHandle;
    protected ParameterHandle refueledGasPumpIdParamHandle;

    // Washed interaction
    protected InteractionClassHandle washedInteractHandle;
    protected ParameterHandle washedVehicleIdParamHandle;

    // FuelPaid interaction
    protected InteractionClassHandle fuelPaidInteractHandle;
    protected ParameterHandle fuelPaidVehicleIdParamHandle;
    protected ParameterHandle fuelPaidGasPumpIdParamHandle;

    // WashPaid interaction
    protected InteractionClassHandle washPaidInteractHandle;
    protected ParameterHandle washPaidVehicleIdParamHandle;

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

        // Subscribe Washed interaction

        this.washedInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.Refueled");
        this.washedVehicleIdParamHandle = rtiamb.getParameterHandle(this.washedInteractHandle, "vehicleId");
        rtiamb.subscribeInteractionClass(this.washedInteractHandle);

        // Publish FuelPaid interaction

        this.fuelPaidInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.FuelPaid");
        this.fuelPaidVehicleIdParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "vehicleId");
        this.fuelPaidGasPumpIdParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "gasPumpId");
        rtiamb.publishInteractionClass(this.fuelPaidInteractHandle);

        // Publish WashPaid interaction

        this.washPaidInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.WashPaid");
        this.washPaidVehicleIdParamHandle = rtiamb.getParameterHandle(this.washPaidInteractHandle, "vehicleId");
        rtiamb.publishInteractionClass(this.washPaidInteractHandle);

        this.log("Published and Subscribed");
    }

    void sendFuelPaid(int vehicleId, int gasPumpId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
        parameters.put(this.fuelPaidVehicleIdParamHandle, new Uint32(vehicleId).getByteArray());
        parameters.put(this.fuelPaidGasPumpIdParamHandle, new Uint32(gasPumpId).getByteArray());
        rtiamb.sendInteraction(this.fuelPaidInteractHandle, parameters, generateTag());
    }

    void handleCurrentVehicle() {

    }

    void onRefueled(int vehicleId, int gasPumpId) {
        if (currentlyServed == null) {
            finishTime = this.getTimeAsInt() + 2 + this.random.nextInt(3);
            currentlyServed = new Refueled(vehicleId, gasPumpId);
            log("Vehicle(id=" + vehicleId + ") is now served, finish at " + finishTime);
        } else {
            waitingQueue.add(new Refueled(vehicleId, gasPumpId));
            log("Vehicle(id=" + vehicleId + ") added to queue");
        }
    }

    void onWashed(int vehicleId) {
        // TODO: Handle that
    }

    @Override
    protected void processEvents() throws RTIexception {
        while (!events.isEmpty()) {
            Object event = events.remove();

            if (event instanceof Refueled) {
                Refueled refueled = (Refueled) event;
                onRefueled(refueled.getVehicleId(), refueled.getGasPumpId());
            } else if (event instanceof Washed) {
                Washed washed = (Washed) event;
                onWashed(washed.getVehicleId());
            }
        }
    }

    protected void runSimulation() throws RTIexception {
        while (this.getTimeAsInt() < END_TIME) {

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
