package Entry;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.RTIexception;
import util.Federate;

public class EntryFederate extends Federate {
    // EntryQueue object
    protected ObjectClassHandle entryQueueClassHandle;
    protected AttributeHandle entryQueueCurrentVehicleCountAttrHandle;
    protected AttributeHandle entryQueueMaxVehiclesAttrHandle;
    protected AttributeHandle entryQueueEarliestVehicleIdAttrHandle;

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
        // Publish and subscribe EntryQueue object

        this.entryQueueClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.EntryQueue");
        this.entryQueueCurrentVehicleCountAttrHandle = rtiamb.getAttributeHandle(this.entryQueueClassHandle, "currentVehicleCount");
        this.entryQueueMaxVehiclesAttrHandle = rtiamb.getAttributeHandle(this.entryQueueClassHandle, "maxVehicles");
        this.entryQueueEarliestVehicleIdAttrHandle = rtiamb.getAttributeHandle(this.entryQueueClassHandle, "earliestVehicleId");

        AttributeHandleSet entryQueueAttributes = rtiamb.getAttributeHandleSetFactory().create();
        entryQueueAttributes.add(this.entryQueueCurrentVehicleCountAttrHandle);
        entryQueueAttributes.add(this.entryQueueMaxVehiclesAttrHandle);
        entryQueueAttributes.add(this.entryQueueEarliestVehicleIdAttrHandle);

        rtiamb.publishObjectClassAttributes(this.entryQueueClassHandle, entryQueueAttributes);
        rtiamb.subscribeObjectClassAttributes(this.entryQueueClassHandle, entryQueueAttributes);

        // Subscribe NewClient interaction

        this.newClientInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.NewClient");
        this.newClientVehicleIdParamHandle = rtiamb.getParameterHandle(this.newClientInteractHandle, "vehicleId");
        rtiamb.subscribeInteractionClass(this.newClientInteractHandle);

        // Subscribe GetClientL1 interaction

        this.getClientL1InteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.GetClientL1");
        this.getClientL1VehicleIdParamHandle = rtiamb.getParameterHandle(this.newClientInteractHandle, "vehicleId");
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
