package CarWash;

import RtiObjects.CarWashQueue;
import RtiObjects.Federate;
import RtiObjects.RtiObjectFactory;
import RtiObjects.WashPaid;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.exceptions.RTIexception;
import util.Uint32;

public class CarWashFederate extends Federate {
    CarWashQueue carWashQueue;
    boolean isBusy = false;
    int finishTime = 0;
    int currentVehicleId;

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

    void sendGetClientLW(int vehicleId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);
        Uint32 value = new Uint32(vehicleId);
        parameters.put(this.getClientLWVehicleIdParamHandle, value.getByteArray());
        rtiamb.sendInteraction(this.getClientLWInteractHandle, parameters, generateTag());
    }

    void sendWashed(int vehicleId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);
        Uint32 value = new Uint32(vehicleId);
        parameters.put(this.washedVehicleIdParamHandle, value.getByteArray());
        rtiamb.sendInteraction(this.washedInteractHandle, parameters, generateTag());
    }

    void onWashPaid(int vehicleId) throws RTIexception {
        log("Vehicle(id=" + vehicleId + ") has finished checking out");

        isBusy = false;

        if (carWashQueue.currentVehicleCount == 0) {
            log("No Vehicle waiting for CarWash");
        } else {
            handleNextVehicle();
        }
    }

    void handleNextVehicle() throws RTIexception {
        if (carWashQueue.currentVehicleCount > 0 && !isBusy) {
            int vehicleId = carWashQueue.earliestVehicleId;
            finishTime = this.getTimeAsInt() + 10 + this.random.nextInt(20);

            log("Adding Vehicle(id=" + vehicleId + ") to CarWash to finish at " + finishTime);

            carWashQueue.currentVehicleCount -= 1;
            currentVehicleId = vehicleId;
            isBusy = true;
            sendGetClientLW(vehicleId);
        }
    }

    void onUpdatedCarWashQueue(int currentVehicleCount, int earliestVehicleId) throws RTIexception {
        log("CarWashQueue(currentVehicleCount=" + currentVehicleCount + ", earliestVehicleId="
                + earliestVehicleId + ") updated");

        carWashQueue.currentVehicleCount = currentVehicleCount;
        carWashQueue.earliestVehicleId = earliestVehicleId;

        handleNextVehicle();
    }

    protected void runSimulation() throws RTIexception {
        carWashQueue = new CarWashQueue(0, 10, 0);

        while (this.getTimeAsInt() < END_TIME) {
            if (isBusy && finishTime <= this.getTimeAsInt()) {
                int vehicleId = currentVehicleId;

                log("CarWash finished washing Vehicle(id=" + vehicleId + ")");
                finishTime = Integer.MAX_VALUE;
                this.sendWashed(vehicleId);
            }

            this.advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }
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
