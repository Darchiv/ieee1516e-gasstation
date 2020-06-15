package Entry;

import RtiObjects.EntryQueue;
import RtiObjects.Federate;
import RtiObjects.RtiObjectFactory;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.exceptions.RTIexception;

public class EntryFederate extends Federate {
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

    protected void onNewClient(int vehicleId) {
        this.log("NewClient(" + vehicleId + ")");

        // TODO: Add this vehicleId to a queue
        // TODO: Update entryQueue accordingly to notify Lanes
    }

    protected void onGetClientL1(int vehicleId) {
        this.log("GetClientL1(" + vehicleId + ")");

        // TODO: Update queue (client was removed)
    }

    protected void runSimulation() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);
        entryQueue = rtiObjectFactory.createEntryQueue();
        entryQueue.setInitialAttributeValues(0, 10, 0);
        log("Registered EntryQueue, handle=" + entryQueue);

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
