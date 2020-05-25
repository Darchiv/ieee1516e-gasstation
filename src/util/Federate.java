package util;

import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.RTIexception;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public abstract class Federate {
    private Logger logger;
    private String name;
    protected RTIambassador rtiamb;
    protected EncoderFactory encoderFactory;

    protected static final String READY_TO_RUN = "ReadyToRun";
    protected static final String federationName = "GasStation";

    public Federate(String name) {
        this.name = name;
        this.logger = new Logger(name);
    }

    public void createAmbassador() throws RTIexception {
        this.log("Creating RTIambassador");
        this.rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        this.encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
    }

    public void connectAmbassador(FederateAmbassador fedamb) throws RTIexception {
        this.log("Connecting...");
        this.rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);
    }

    public void createFederation(URL[] modules) throws RTIexception {
        this.log("Creating Federation...");

        try {
            this.rtiamb.createFederationExecution(federationName, modules);
            this.log("Created Federation");
        } catch (FederationExecutionAlreadyExists exists) {
            this.log("Didn't create federation, it already existed");
        }
    }

    public void joinFederation(URL[] modules) throws RTIexception {
        this.rtiamb.joinFederationExecution(this.name, this.name + "Type", federationName, modules);

        this.log("Joined Federation as " + this.name);
    }

    public void announceReadySyncPoint(Ambassador fedamb) throws RTIexception {
        this.rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);

        while (fedamb.isAnnounced == false) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    public void achieveReadySyncPoint(Ambassador fedamb) throws RTIexception {
        this.rtiamb.synchronizationPointAchieved(READY_TO_RUN);
        log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");

        while (fedamb.isReadyToRun == false) {
            this.rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    public void resignAndDestroyFederation() throws RTIexception {
        this.rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
        log("Resigned from Federation");

        try {
            rtiamb.destroyFederationExecution(federationName);
            log("Destroyed Federation");
        } catch (FederationExecutionDoesNotExist dne) {
            log("No need to destroy federation, it doesn't exist");
        } catch (FederatesCurrentlyJoined fcj) {
            log("Didn't destroy federation, federates still joined");
        }
    }

    protected abstract void publishAndSubscribe() throws RTIexception;

    protected abstract void runSimulation() throws RTIexception;

    public String getFederateName() {
        return this.name;
    }

    protected void log(String message) {
        this.log(message, LoggerLevel.INFO);
    }

    protected void log(String message, LoggerLevel level) {
        this.logger.log(message, level);
    }

    protected void waitForUser() {
        log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (Exception e) {
            log("Error while waiting for user input: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
