package CarWash;

import RtiObjects.CarWashQueue;
import RtiObjects.Federate;
import RtiObjects.RtiObjectFactory;
import RtiObjects.WashPaid;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.exceptions.RTIexception;

public class CarWashFederate extends Federate {
    // Washed interaction
    protected InteractionClassHandle washedInteractHandle;
    protected ParameterHandle washedVehicleIdParamHandle;

    // GetClientLW interaction
    protected InteractionClassHandle getClientLWInteractHandle;
    protected ParameterHandle getClientLWVehicleIdParamHandle;

    // WashPaid interaction
    protected InteractionClassHandle washPaidInteractHandle;
    protected ParameterHandle washPaidVehicleIdParamHandle;

    public CarWashFederate(String name) {
        super(name);
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);

        rtiObjectFactory.registerCarWashQueue(true, true);

        // Publish Washed interaction
        this.washedInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.Washed");
        this.washedVehicleIdParamHandle = rtiamb.getParameterHandle(this.washedInteractHandle, "vehicleId");
        rtiamb.publishInteractionClass(this.washedInteractHandle);

        // Publish GetClientLW interaction
        this.getClientLWInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.GetClientLW");
        this.getClientLWVehicleIdParamHandle = rtiamb.getParameterHandle(this.getClientLWInteractHandle, "vehicleId");
        rtiamb.publishInteractionClass(this.getClientLWInteractHandle);

        // Subscribe WashPaid interaction
        this.washPaidInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.WashPaid");
        this.washPaidVehicleIdParamHandle = rtiamb.getParameterHandle(this.washPaidInteractHandle, "vehicleId");
        rtiamb.subscribeInteractionClass(this.getClientLWInteractHandle);

        this.log("Published and Subscribed");
    }

    void onWashPaid(int vehicleId) {
    }

    @Override
    protected void processEvents() throws RTIexception {
        while (!events.isEmpty()) {
            Object event = events.remove();

            if (event instanceof WashPaid) {
                WashPaid washPaid = (WashPaid) event;
                onWashPaid(washPaid.getVehicleId());
            } else if (event instanceof CarWashQueue) {
                CarWashQueue carWashQueue = (CarWashQueue) event;
                onUpdatedCarWashQueue(carWashQueue.getCurrentVehicleCount(), carWashQueue.getEarliestVehicleId());
            }
        }
    }

    void onUpdatedCarWashQueue(int currentVehicleCount, int earliestVehicleId) {
    }

    protected void runSimulation() throws RTIexception {
    }

    public static void main(String[] args) {
        String federateName = "CarWashFederate";

        CarWashFederate carWashFederate = new CarWashFederate(federateName);
        carWashFederate.assignAmbassador(new CarWashFederateAmbassador(carWashFederate));

        try {
            carWashFederate.runFederate(federateName);
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
