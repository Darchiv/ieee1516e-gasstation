package RtiObjects;

import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.*;

public class Car extends Vehicle {
    private static ObjectClassHandle classHandle;

    Car(RTIambassador rtiamb) throws RTIexception {
        super(rtiamb);

        this.instance = rtiamb.registerObjectInstance(classHandle);
    }

    protected static ObjectClassHandle registerClassHandle(RTIambassador rtiamb) throws RTIexception {
        classHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Vehicle.Car");
        return classHandle;
    }
}
