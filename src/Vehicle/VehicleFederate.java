package Vehicle;

import RtiObjects.*;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.exceptions.RTIexception;
import util.FuelEnum;
import util.Uint32;

import java.util.LinkedList;
import java.util.List;

public class VehicleFederate extends Federate {
    List<Vehicle> vehicles = new LinkedList<>();

    // NewClient interaction
    protected InteractionClassHandle newClientInteractHandle;
    protected ParameterHandle newClientVehicleIdParamHandle;

    // FuelPaid interaction
    protected InteractionClassHandle fuelPaidInteractHandle;
    protected ParameterHandle fuelPaidVehicleIdParamHandle;
    protected ParameterHandle fuelPaidGasPumpIdParamHandle;

    // WashPaid interaction
    protected InteractionClassHandle washPaidInteractHandle;
    protected ParameterHandle washPaidVehicleIdParamHandle;

    // GoWash interaction
    protected InteractionClassHandle goWashInteractHandle;
    protected ParameterHandle goWashVehicleIdParamHandle;

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

        // Publish GoWash interaction

        this.goWashInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.GoWash");
        this.goWashVehicleIdParamHandle = rtiamb.getParameterHandle(this.goWashInteractHandle, "vehicleId");
        rtiamb.publishInteractionClass(this.goWashInteractHandle);

        // Subscribe FuelPaid interaction

        this.fuelPaidInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.FuelPaid");
        this.fuelPaidVehicleIdParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "vehicleId");
        this.fuelPaidGasPumpIdParamHandle = rtiamb.getParameterHandle(this.fuelPaidInteractHandle, "gasPumpId");
        rtiamb.subscribeInteractionClass(this.fuelPaidInteractHandle);

        // Subscribe WashPaid interaction

        this.washPaidInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.washPaid");
        this.washPaidVehicleIdParamHandle = rtiamb.getParameterHandle(this.washPaidInteractHandle, "vehicleId");
        rtiamb.subscribeInteractionClass(this.washPaidInteractHandle);


        this.log("Published and Subscribed");
    }

    void sendNewClient(int vehicleId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);
        Uint32 value = new Uint32(vehicleId);
        parameters.put(this.newClientVehicleIdParamHandle, value.getByteArray());
        rtiamb.sendInteraction(this.newClientInteractHandle, parameters, generateTag());
    }

    void sendGoWash(int vehicleId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);
        Uint32 value = new Uint32(vehicleId);
        parameters.put(this.goWashVehicleIdParamHandle, value.getByteArray());
        rtiamb.sendInteraction(this.goWashInteractHandle, parameters, generateTag());
    }

    void onFuelPaid(int vehicleId, int gasPumpId) throws RTIexception {
        Vehicle v = null;

        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId() == vehicleId) {
                v = vehicle;
                vehicles.remove(vehicle);
                break;
            }
        }

        int totalTime = totalTime = getTimeAsInt() - v.getTimeEntered();

        log("Vehicle(id=" + vehicleId + ") has paid, total time on the station: " + totalTime);

        if (v instanceof Car) {
            log("Car(id=" + vehicleId + ") goes washing");
            sendGoWash(vehicleId);
        }
    }

    void onWashPaid(int vehicleId) {
        // TODO: Handle that
    }

    @Override
    protected void processEvents() throws RTIexception {
        while (!events.isEmpty()) {
            Object event = events.remove();

            if (event instanceof FuelPaid) {
                FuelPaid fuelPaid = (FuelPaid) event;
                onFuelPaid(fuelPaid.getVehicleId(), fuelPaid.getVehicleId());
            } else if (event instanceof WashPaid) {
                WashPaid washPaid = (WashPaid) event;
                onWashPaid(washPaid.getVehicleId());
            }
        }
    }

    protected void runSimulation() throws RTIexception {
        RtiObjectFactory rtiObjectFactory = RtiObjectFactory.getFactory(rtiamb);
        int lastVehicleId = 1;

        while (this.getTimeAsInt() < END_TIME) {
            if (this.random.nextInt(4) == 0) {
                Vehicle v;
                if (this.random.nextInt(3) == 0) {
                    v = rtiObjectFactory.createMotorcycle();
                    log("Created Motorcycle(id=" + lastVehicleId + ")");
                } else {
                    v = rtiObjectFactory.createCar();
                    log("Created Car(id=" + lastVehicleId + ")");
                }
                FuelEnum fuelEnum;
                if (this.random.nextInt(2) == 0) {
                    fuelEnum = new FuelEnum("petrol");
                } else {
                    fuelEnum = new FuelEnum("diesel");
                }
                v.setInitialAttributeValues(lastVehicleId, false, (int) this.fedamb.getFederateTime(), fuelEnum);
                vehicles.add(v);
                this.sendNewClient(lastVehicleId);
                lastVehicleId += 1;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
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