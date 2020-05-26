package util;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;

public abstract class Ambassador extends NullFederateAmbassador {
    private Logger logger;
    private String name;

    protected boolean isAnnounced = false;
    protected boolean isReadyToRun = false;

    protected boolean isAdvancing = false;

    protected double federateTime = 0.0;
    protected double federateLookahead = 1.0;

    public Ambassador(String name) {
        this.name = name;
        this.logger = new Logger(name);
    }

    @Override
    public void synchronizationPointRegistrationFailed(String label,
                                                       SynchronizationPointFailureReason reason) {
        this.log("Failed to register sync point: " + label + ", reason=" + reason);
    }

    @Override
    public void synchronizationPointRegistrationSucceeded(String label) {
        this.log("Successfully registered sync point: " + label);
    }

    @Override
    public void announceSynchronizationPoint(String label, byte[] tag) {
        this.log("Synchronization point announced: " + label);
        if (label.equals(Federate.READY_TO_RUN))
            this.isAnnounced = true;
    }

    @Override
    public void federationSynchronized(String label, FederateHandleSet failed) {
        this.log("Federation Synchronized: " + label);
        if (label.equals(Federate.READY_TO_RUN))
            this.isReadyToRun = true;
    }

    @Override
    public void timeAdvanceGrant(LogicalTime time) {
        this.federateTime = ((HLAfloat64Time) time).getValue();
        this.isAdvancing = false;
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle theObject,
                                     byte[] tag,
                                     OrderType sentOrdering,
                                     SupplementalRemoveInfo removeInfo)
            throws FederateInternalError {
        log("Object Removed: handle=" + theObject);
    }

    public double getFederateTime() {
        return this.federateTime;
    }

    protected void log(String message) {
        this.log(message, LoggerLevel.INFO);
    }

    protected void log(String message, LoggerLevel level) {
        this.logger.log(message, level);
    }
}
