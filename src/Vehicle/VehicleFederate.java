package Vehicle;

import hla.rti1516e.exceptions.RTIexception;
import util.Federate;

public class VehicleFederate extends Federate {
    public VehicleFederate(String name) {
        super(name);
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        // TODO: Publish and subscribe
    }

    protected void runSimulation() throws RTIexception {
        // TODO: Object registration

        for (int i = 0; i < ITERATIONS; i++) {
            // TODO: Send interactions and update attributes

//			updateAttributeValues( objectHandle );
//			sendInteraction();

            advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }

        // TODO: Object deletion
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