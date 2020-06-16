package CarWashQueueing;

import CarWash.CarWashFederate;
import CarWash.CarWashFederateAmbassador;
import RtiObjects.Federate;
import RtiObjects.RtiObjectFactory;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.exceptions.RTIexception;

public class CarWashQueueingFederate extends Federate
{
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

    protected void runSimulation() throws RTIexception {
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