package Entry;

import RtiObjects.*;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.exceptions.RTIexception;

import java.util.LinkedList;
import java.util.Queue;

public class EntryFederate extends Federate {
    protected Queue<Integer> vehicleQueue = new LinkedList<>();
    // EntryQueue object
    protected EntryQueue entryQueue;

    // NewClient interaction
    protected InteractionClassHandle newClientInteractHandle;
    protected ParameterHandle newClientVehicleIdParamHandle;

    // GetClientL1 interaction
    protected InteractionClassHandle getClientL1InteractHandle;
    protected ParameterHandle getClientL1VehicleIdParamHandle;

    public EntryFederate(String name) {
        super(name);
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);

        // Publish and subscribe EntryQueue object

        rtiObjectFactory.registerEntryQueue(true, true);

        // Subscribe NewClient interaction

        this.newClientInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.NewClient");
        this.newClientVehicleIdParamHandle = rtiamb.getParameterHandle(this.newClientInteractHandle, "vehicleId");
        rtiamb.subscribeInteractionClass(this.newClientInteractHandle);

        // Subscribe GetClientL1 interaction

        this.getClientL1InteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.GetClientL1");
        this.getClientL1VehicleIdParamHandle = rtiamb.getParameterHandle(this.getClientL1InteractHandle, "vehicleId");
        rtiamb.subscribeInteractionClass(this.getClientL1InteractHandle);


        this.log("Published and Subscribed");
    }

    protected void onNewClient(int vehicleId) throws RTIexception {
        this.log("NewClient(" + vehicleId + ")");
        vehicleQueue.add(vehicleId);

        int currentVehicleCount = vehicleQueue.size();
        int earliestVehicleId = vehicleQueue.peek();
        log("Updating EntryQueue to currentVehicleCount=" + currentVehicleCount + ", earliestVehicleId=" + earliestVehicleId);
        entryQueue.updateQueue(currentVehicleCount, earliestVehicleId);
    }

    protected void onGetClientL1(int vehicleId) {
        this.log("GetClientL1(" + vehicleId + ")");

        int vId = vehicleQueue.remove();

        if (vId != vehicleId) {
            throw new RuntimeException("A vehicle with earliest id must be requested");
        }
    }

    @Override
    protected void processEvents() throws RTIexception {
        while (!events.isEmpty()) {
            Object event = events.remove();

            if (event instanceof NewClient) {
                NewClient newClient = (NewClient) event;
                onNewClient(newClient.getVehicleId());
            } else if (event instanceof GetClientL1) {
                GetClientL1 getClientL1 = (GetClientL1) event;
                onGetClientL1(getClientL1.getVehicleId());
            }
        }
    }

    protected void runSimulation() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);
        int maxVehicles = 10;
        entryQueue = rtiObjectFactory.createEntryQueue();
        entryQueue.setInitialAttributeValues(0, maxVehicles, 0);
        log("Registered EntryQueue(maxVehicles=" + maxVehicles + ")");

        for (int i = 0; i < ITERATIONS; i++) {

            this.advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }

//        rtiamb.deleteObjectInstance(entryQueue, generateTag());
//        log("Deleted Object, handle=" + entryQueue);
    }

    public static void main(String[] args) {
        String federateName = "EntryFederate";

        EntryFederate entryFederate = new EntryFederate(federateName);
        entryFederate.assignAmbassador(new EntryFederateAmbassador(entryFederate));

        try {
            entryFederate.runFederate(federateName);
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
