package Vehicle;

import RtiObjects.Car;
import RtiObjects.Federate;
import RtiObjects.RtiObjectFactory;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.exceptions.RTIexception;
import util.FuelEnum;
import util.Uint32;

import java.util.LinkedList;
import java.util.List;

public class VehicleFederate extends Federate {
    // NewClient interaction
    protected InteractionClassHandle newClientInteractHandle;
    protected ParameterHandle newClientVehicleIdParamHandle;

    // FuelPaid interaction
    protected InteractionClassHandle fuelPaidInteractHandle;
    protected ParameterHandle fuelPaidVehicleIdParamHandle;
    protected ParameterHandle fuelPaidGasPumpIdParamHandle;

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

        // Subscribe FuelPaid interaction

        this.fuelPaidInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.FuelPaid");
        this.fuelPaidVehicleIdParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "vehicleId");
        this.fuelPaidGasPumpIdParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "gasPumpId");
        rtiamb.subscribeInteractionClass(this.fuelPaidInteractHandle);


        this.log("Published and Subscribed");
    }

    void sendNewClient(int vehicleId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);
        Uint32 value = new Uint32(vehicleId);
        parameters.put(this.newClientVehicleIdParamHandle, value.getByteArray());
        rtiamb.sendInteraction(this.newClientInteractHandle, parameters, generateTag());
    }

    void onFuelPaid(int vehicleId, int gasPumpId) {
        // TODO: Handle that
    }

    protected void runSimulation() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);
        int lastVehicleId = 1;
        List<Car> cars = new LinkedList<>();

        for (int i = 0; i < ITERATIONS; i++) {
            if (this.random.nextInt(4) == 0) {
                Car c = rtiObjectFactory.createCar();
                FuelEnum fuelEnum;
                if (this.random.nextInt(2) == 0) {
                    fuelEnum = new FuelEnum("petrol");
                } else {
                    fuelEnum = new FuelEnum("diesel");
                }
                c.setInitialAttributeValues(lastVehicleId, false, (int) this.fedamb.getFederateTime(), fuelEnum);
                log("Registered Car, handle=" + c);
                cars.add(c);
                this.sendNewClient(lastVehicleId);
                lastVehicleId += 1;
            }

            advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }

//        c.destroy();
//        log("Deleted Car, handle=" + c1);
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