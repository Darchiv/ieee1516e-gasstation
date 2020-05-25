package Entry;

import util.Ambassador;

public class EntryFederateAmbassador extends Ambassador {
    private EntryFederate federate;

//    TODO: Implement interactions/reflections

    public EntryFederateAmbassador(EntryFederate federate) {
        super("EntryFederateAmbassador");
        this.federate = federate;
    }
}
