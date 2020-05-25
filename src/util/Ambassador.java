package util;

import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.SynchronizationPointFailureReason;

public abstract class Ambassador extends NullFederateAmbassador {
    private Logger logger;
    private String name;

    protected boolean isAnnounced = false;
    protected boolean isReadyToRun = false;

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

    protected void log(String message) {
        this.log(message, LoggerLevel.INFO);
    }

    protected void log(String message, LoggerLevel level) {
        this.logger.log(message, level);
    }
}
