package Lanes;

import Entry.EntryFederate;
import util.Ambassador;

public class LanesFederateAmbassador extends Ambassador {
    private LanesFederate federate;

//    TODO: Implement interactions/reflections

    public LanesFederateAmbassador(LanesFederate federate) {
        super("LanesFederateAmbassador");
        this.federate = federate;
    }
}
