package Entry;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.exceptions.RTIexception;
import util.Federate;

import java.io.File;
import java.net.URL;

public class EntryFederate extends Federate {
    private EntryFederateAmbassador fedamb;

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

    protected void runSimulation() {
//        TODO: Implement

//        ObjectInstanceHandle objectHandle = registerObject();
//        log( "Registered Object, handle=" + objectHandle );
//
//        for( int i = 0; i < ITERATIONS; i++ )
//        {
//            // 9.1 update the attribute values of the instance //
//            updateAttributeValues( objectHandle );
//
//            // 9.2 send an interaction
//            sendInteraction();
//
//            // 9.3 request a time advance and wait until we get it
//            advanceTime( 1.0 );
//            log( "Time Advanced to " + fedamb.federateTime );
//        }
//        deleteObject( objectHandle );
//        log( "Deleted Object, handle=" + objectHandle );
    }

    //    TODO: Move runFederate into Federate
    public void runFederate(String federateName) throws Exception {
        this.createAmbassador();
        // TODO: Move fedamb into Federate (via constructor)
        this.fedamb = new EntryFederateAmbassador(this);
        this.connectAmbassador(fedamb);

        this.createFederation(new URL[]{
                (new File("foms/GasStation.xml")).toURI().toURL()
        });

        this.joinFederation(new URL[]{
                (new File("foms/GasStation.xml")).toURI().toURL()
        });

        // TODO: Time factory

        this.announceReadySyncPoint(this.fedamb);
        // TODO: Remove the need for pause
        this.waitForUser();
        this.achieveReadySyncPoint(this.fedamb);

        // TODO: Enable time policies

        this.publishAndSubscribe();

        this.runSimulation();

        this.resignAndDestroyFederation();
    }

    public static void main(String[] args) {
        String federateName = "EntryFederate";

        EntryFederate entryFederate = new EntryFederate(federateName);

        try {
            entryFederate.runFederate(federateName);
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
