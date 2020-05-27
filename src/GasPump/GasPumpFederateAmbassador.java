package GasPump;

import util.Ambassador;

public class GasPumpFederateAmbassador extends Ambassador {
    private GasPumpFederate federate;

//    TODO: Implement interactions/reflections

    public GasPumpFederateAmbassador(GasPumpFederate federate) {
        super("GasPumpFederateAmbassador");
        this.federate = federate;
    }
}
