package RtiObjects;

import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.RTIexception;

public class Motorcycle extends Vehicle {
    private static ObjectClassHandle classHandle;

    Motorcycle(RTIambassador rtiamb) throws RTIexception {
        super(rtiamb);

        this.instance = rtiamb.registerObjectInstance(classHandle);
    }

    public static ObjectClassHandle getClassHandle() {
        return classHandle;
    }

    protected static ObjectClassHandle registerClassHandle(RTIambassador rtiamb) throws RTIexception {
        classHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Vehicle.Motorcycle");
        return classHandle;
    }
}
