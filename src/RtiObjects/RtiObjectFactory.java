package RtiObjects;

import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.RTIexception;

public final class RtiObjectFactory {
    private static RtiObjectFactory instance;
    private RTIambassador rtiamb;

    public static RtiObjectFactory getFactory(RTIambassador rtiamb) {
        if (instance == null) {
            instance = new RtiObjectFactory(rtiamb);
        }

        return instance;
    }

    private RtiObjectFactory(RTIambassador rtiamb) {
        this.rtiamb = rtiamb;
    }

    public void registerVehicle(boolean publish, boolean subscribe) {
        try {
            ObjectClassHandle classHandle = Vehicle.registerClassHandle(this.rtiamb);
            Vehicle.registerHandles(this.rtiamb, classHandle, publish, subscribe);
        } catch (RTIexception e) {
            throw new RuntimeException(e);
        }
    }

    public void registerCar(boolean publish, boolean subscribe) {
        try {
            ObjectClassHandle classHandle = Car.registerClassHandle(this.rtiamb);
            Vehicle.registerHandles(this.rtiamb, classHandle, publish, subscribe);
        } catch (RTIexception e) {
            throw new RuntimeException(e);
        }
    }

    public void registerMotorcycle(boolean publish, boolean subscribe) {
        try {
            ObjectClassHandle classHandle = Motorcycle.registerClassHandle(this.rtiamb);
            Vehicle.registerHandles(this.rtiamb, classHandle, publish, subscribe);
        } catch (RTIexception e) {
            throw new RuntimeException(e);
        }
    }

    public void registerGasPump(boolean publish, boolean subscribe) {
        try {
            ObjectClassHandle classHandle = GasPump.registerClassHandle(this.rtiamb);
            GasPump.registerHandles(this.rtiamb, classHandle, publish, subscribe);
        } catch (RTIexception e) {
            throw new RuntimeException(e);
        }
    }

    public void registerEntryQueue(boolean publish, boolean subscribe) {
        try {
            ObjectClassHandle classHandle = EntryQueue.registerClassHandle(this.rtiamb);
            EntryQueue.registerHandles(this.rtiamb, classHandle, publish, subscribe);
        } catch (RTIexception e) {
            throw new RuntimeException(e);
        }
    }

    public void registerLane(boolean publish, boolean subscribe) {
        try {
            ObjectClassHandle classHandle = Lane.registerClassHandle(this.rtiamb);
            Lane.registerHandles(this.rtiamb, classHandle, publish, subscribe);
        } catch (RTIexception e) {
            throw new RuntimeException(e);
        }
    }

    public Car createCar() {
        try {
            return new Car(this.rtiamb);
        } catch (RTIexception e) {
            throw new RuntimeException(e);
        }
    }

    public Motorcycle createMotorcycle() {
        try {
            return new Motorcycle(this.rtiamb);
        } catch (RTIexception e) {
            throw new RuntimeException(e);
        }
    }

    public GasPump createGasPump() {
        try {
            return new GasPump(this.rtiamb);
        } catch (RTIexception e) {
            throw new RuntimeException(e);
        }
    }

    public EntryQueue createEntryQueue() {
        try {
            return new EntryQueue(this.rtiamb);
        } catch (RTIexception e) {
            throw new RuntimeException(e);
        }
    }

    public Lane createLane() {
        try {
            return new Lane(this.rtiamb);
        } catch (RTIexception e) {
            throw new RuntimeException(e);
        }
    }
}
