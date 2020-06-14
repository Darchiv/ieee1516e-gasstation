package Vehicle;

import RtiObjects.*;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.RTIexception;
import util.FuelEnum;
import util.Uint32;

public class VehicleFederate extends Federate {
    // NewClient interaction
    protected InteractionClassHandle newClientInteractHandle;
    protected ParameterHandle newClientVehicleIdParamHandle;

    public VehicleFederate(String name) {
        super(name);
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);

        rtiObjectFactory.registerVehicle(true, true);
        rtiObjectFactory.registerCar(true, true);
        rtiObjectFactory.registerMotorcycle(true, true);

        // Publish NewClient interaction

        this.newClientInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.NewClient");
        this.newClientVehicleIdParamHandle = rtiamb.getParameterHandle(this.newClientInteractHandle, "vehicleId");
        rtiamb.publishInteractionClass(this.newClientInteractHandle);


        this.log("Published and Subscribed");
    }

    void sendNewClient(int vehicleId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);
        Uint32 value = new Uint32(vehicleId);
        parameters.put(this.newClientVehicleIdParamHandle, value.getByteArray());
        rtiamb.sendInteraction(this.newClientInteractHandle, parameters, generateTag());
    }

    protected void runSimulation() throws RTIexception {
        // TODO: This object must be created on request (randomly), not once
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);
        Car c1 = rtiObjectFactory.createCar();
        c1.setInitialAttributeValues(1, false, 0, new FuelEnum("petrol"));
        log("Registered Car, handle=" + c1);

        for (int i = 0; i < ITERATIONS; i++) {
//			updateAttributeValues( objectHandle );

            if (i % 3 == 1) {
                this.sendNewClient(i);
            }

            advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }

        // TODO: Vehicles must be deleted when requested by the Vehicle federate
        c1.destroy();
        log("Deleted Car, handle=" + c1);
    }

    public static void main(String[] args) {
        String federateName = "VehicleFederate";

        VehicleFederate vehicleFederate = new VehicleFederate(federateName);
        vehicleFederate.assignAmbassador(new VehicleFederateAmbassador(vehicleFederate));

        try {
            vehicleFederate.runFederate(federateName);
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}