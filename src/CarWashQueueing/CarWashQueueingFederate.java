package CarWashQueueing;

import RtiObjects.*;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.exceptions.RTIexception;

import java.util.LinkedList;
import java.util.Queue;

public class CarWashQueueingFederate extends Federate {
    protected Queue<Integer> vehicleQueue = new LinkedList<>();

    // CarWashQueue object
    protected CarWashQueue carWashQueue;

    // GoWash interaction
    protected InteractionClassHandle goWashInteractHandle;
    protected ParameterHandle goWashVehicleIdParamHandle;

    // GetClientLW interaction
    protected InteractionClassHandle getClientLWInteractHandle;
    protected ParameterHandle getClientLWVehicleIdParamHandle;

    public CarWashQueueingFederate(String name) {
        super(name);
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);

        rtiObjectFactory.registerCarWashQueue(true, true);

        // Subscribe GetClientLW interaction
        this.getClientLWInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.GetClientLW");
        this.getClientLWVehicleIdParamHandle = rtiamb.getParameterHandle(this.getClientLWInteractHandle, "vehicleId");
        rtiamb.publishInteractionClass(this.getClientLWInteractHandle);

        // Subscribe GoWash interaction
        this.goWashInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.GoWash");
        this.goWashVehicleIdParamHandle = rtiamb.getParameterHandle(this.goWashInteractHandle, "vehicleId");
        rtiamb.subscribeInteractionClass(this.getClientLWInteractHandle);

        this.log("Published and Subscribed");
    }

    void onWashPaid(int vehicleId) {
    }

    void onGetClientLW(int vehicleId) {
        this.log("GetClientLW(" + vehicleId + ")");

        int vId = vehicleQueue.remove();

        if (vId != vehicleId) {
            throw new RuntimeException("A vehicle with earliest id must be requested");
        }
    }

    @Override
    protected void processEvents() {
        while (!events.isEmpty()) {
            Object event = events.remove();

            if (event instanceof WashPaid) {
                WashPaid washPaid = (WashPaid) event;
                onWashPaid(washPaid.getVehicleId());
            }
        }
    }

    protected void runSimulation() throws RTIexception {
        while (this.getTimeAsInt() < END_TIME) {
            RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);
            int maxVehicles = 10;
            carWashQueue = rtiObjectFactory.createCarWashQueue();
            carWashQueue.setInitialAttributeValues(0, maxVehicles, 0);
            log("Registered CarWashQueue(maxVehicles=" + maxVehicles + ")");

            while (this.getTimeAsInt() < END_TIME) {

                this.advanceTime(1.0);
                log("Time Advanced to " + this.fedamb.getFederateTime());
            }
        }
    }

    public static void main(String[] args) {
        String federateName = "CarWashQueueingFederate";

        CarWashQueueingFederate carWashQueueingFederate = new CarWashQueueingFederate(federateName);
        carWashQueueingFederate.assignAmbassador(new CarWashQueueingFederateAmbassador(carWashQueueingFederate));

        try {
            carWashQueueingFederate.runFederate(federateName);
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}