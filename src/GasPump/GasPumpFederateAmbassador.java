package GasPump;

import RtiObjects.Ambassador;
import RtiObjects.Vehicle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class GasPumpFederateAmbassador extends Ambassador {
    private GasPumpFederate federate;

    @Override
    public void discoverObjectInstance(ObjectInstanceHandle theObject,
                                       ObjectClassHandle theObjectClass,
                                       String objectName)
            throws FederateInternalError {
        if (theObjectClass.equals(Vehicle.getClassHandle())) {

        }
        log("Discovered Object: handle=" + theObject + ", classHandle=" +
                theObjectClass + ", name=" + objectName);
    }

    public GasPumpFederateAmbassador(GasPumpFederate federate) {
        super("GasPumpFederateAmbassador");
        this.federate = federate;
    }
}
