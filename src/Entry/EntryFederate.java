package Entry;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.RTIexception;
import util.Federate;

import java.io.File;
import java.net.URL;

public class EntryFederate extends Federate {
    protected ObjectClassHandle entryQueueClassHandle;
    protected AttributeHandle currentVehicleCountAttrHandle;
    protected AttributeHandle maxVehiclesAttrHandle;
    protected AttributeHandle earliestVehicleIdAttrHandle;
    protected InteractionClassHandle newClientInteractHandle;
    protected InteractionClassHandle getClientL1InteractHandle;

    public EntryFederate(String name) {
        super(name);
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        // Publish and subscribe EntryQueue object

        this.entryQueueClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.EntryQueue");
        this.currentVehicleCountAttrHandle = rtiamb.getAttributeHandle(this.entryQueueClassHandle, "currentVehicleCount");
        this.maxVehiclesAttrHandle = rtiamb.getAttributeHandle(this.entryQueueClassHandle, "maxVehicles");
        this.earliestVehicleIdAttrHandle = rtiamb.getAttributeHandle(this.entryQueueClassHandle, "earliestVehicleId");

        AttributeHandleSet entryQueueAttributes = rtiamb.getAttributeHandleSetFactory().create();
        entryQueueAttributes.add(this.currentVehicleCountAttrHandle);
        entryQueueAttributes.add(this.maxVehiclesAttrHandle);
        entryQueueAttributes.add(this.earliestVehicleIdAttrHandle);

        rtiamb.publishObjectClassAttributes(this.entryQueueClassHandle, entryQueueAttributes);
        rtiamb.subscribeObjectClassAttributes(this.entryQueueClassHandle, entryQueueAttributes);

        // Subscribe NewClient interaction

        this.newClientInteractHandle = rtiamb.getInteractionClassHandle("InteractionRoot.NewClient");
        rtiamb.subscribeInteractionClass(this.newClientInteractHandle);

        // Subscribe GetClientL1 interaction

        this.getClientL1InteractHandle = rtiamb.getInteractionClassHandle("InteractionRoot.GetClientL1");
        rtiamb.subscribeInteractionClass(this.getClientL1InteractHandle);


        this.log("Published and Subscribed");
    }

    protected void runSimulation() throws RTIexception {
        // Register (create) a new instance of EntryQueue
        ObjectInstanceHandle entryQueue = rtiamb.registerObjectInstance(entryQueueClassHandle);
        log("Registered Object, handle=" + entryQueue);

        for (int i = 0; i < ITERATIONS; i++) {
            // 9.1 update the attribute values of the instance //
//            updateAttributeValues( objectHandle );
//
//            // 9.2 send an interaction
//            sendInteraction();

            this.advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }

        rtiamb.deleteObjectInstance(entryQueue, generateTag());
        log("Deleted Object, handle=" + entryQueue);
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
