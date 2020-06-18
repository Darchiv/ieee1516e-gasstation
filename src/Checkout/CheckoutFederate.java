package Checkout;

import RtiObjects.Federate;
import RtiObjects.Refueled;
import RtiObjects.Washed;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.exceptions.RTIexception;
import util.Uint32;

import java.sql.Ref;
import java.util.LinkedList;
import java.util.Queue;

public class CheckoutFederate extends Federate {
    protected CheckoutInfo currentlyServed = null;
    protected Queue<CheckoutInfo> waitingQueue = new LinkedList<>();
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

        this.washedInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.Washed");
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

    void sendWashPaid(int vehicleId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);
        parameters.put(this.washPaidVehicleIdParamHandle, new Uint32(vehicleId).getByteArray());
        rtiamb.sendInteraction(this.washedInteractHandle, parameters, generateTag());
    }

    void handleNext() {
        if (currentlyServed == null && !waitingQueue.isEmpty()) {
            CheckoutInfo checkoutInfo = waitingQueue.remove();

            finishTime = this.getTimeAsInt() + 2 + this.random.nextInt(3);
            currentlyServed = checkoutInfo;

            if (checkoutInfo.refueled != null) {
                log("Vehicle(id=" + checkoutInfo.refueled.getVehicleId() + ") after refuelling is now served, finish at " + finishTime);
            } else {
                log("Vehicle(id=" + checkoutInfo.washed.getVehicleId() + ") after washing is now served, finish at " + finishTime);
            }
        }
    }

    void onRefueled(int vehicleId, int gasPumpId) {
        waitingQueue.add(new CheckoutInfo(new Refueled(vehicleId, gasPumpId)));
        log("Refuelled Vehicle(id=" + vehicleId + ") added to queue");
        handleNext();
    }

    void onWashed(int vehicleId) {
        waitingQueue.add(new CheckoutInfo(new Washed(vehicleId)));
        log("Washed Vehicle(id=" + vehicleId + ") added to queue");
        handleNext();
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
            if (currentlyServed != null && finishTime <= getTimeAsInt()) {
                if (currentlyServed.refueled != null) {
                    log("Finished handling Vehicle(id=" + currentlyServed.refueled.getVehicleId()
                            + ") refuelled at GasPump(id=" + currentlyServed.refueled.getGasPumpId() + ")");
                    sendFuelPaid(currentlyServed.refueled.getVehicleId(), currentlyServed.refueled.getGasPumpId());
                } else {
                    log("Finished handling washed Vehicle(id=" + currentlyServed.washed.getVehicleId() + ")");
                    sendWashPaid(currentlyServed.washed.getVehicleId());
                }

                currentlyServed = null;
                handleNext();
            }

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

    private class CheckoutInfo {
        public Refueled refueled = null;
        public Washed washed = null;

        public CheckoutInfo(Refueled refueled) {
            this.refueled = refueled;
        }

        public CheckoutInfo(Washed washed) {
            this.washed = washed;
        }
    }
}
