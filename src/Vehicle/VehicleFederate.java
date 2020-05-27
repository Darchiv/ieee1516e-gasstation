package Vehicle;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.RTIexception;
import util.Federate;
import util.Uint32;

public class VehicleFederate extends Federate {
    // Vehicle object
    protected ObjectClassHandle vehicleClassHandle;
    protected AttributeHandle vehicleIdAttrHandle;
    protected AttributeHandle vehicleIsFilledAttrHandle;
    protected AttributeHandle vehicleTimeEnteredAttrHandle;
    protected AttributeHandle vehicleFuelTypeAttrHandle;

    // NewClient interaction
    protected InteractionClassHandle newClientInteractHandle;
    protected ParameterHandle newClientVehicleIdParamHandle;

    public VehicleFederate(String name) {
        super(name);
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        // Publish and subscribe Vehicle object

        this.vehicleClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Vehicle");
        this.vehicleIdAttrHandle = rtiamb.getAttributeHandle(this.vehicleClassHandle, "id");
        this.vehicleIsFilledAttrHandle = rtiamb.getAttributeHandle(this.vehicleClassHandle, "isFilled");
        this.vehicleTimeEnteredAttrHandle = rtiamb.getAttributeHandle(this.vehicleClassHandle, "timeEntered");
        this.vehicleFuelTypeAttrHandle = rtiamb.getAttributeHandle(this.vehicleClassHandle, "fuelType");

        AttributeHandleSet vehicleAttributes = rtiamb.getAttributeHandleSetFactory().create();
        vehicleAttributes.add(this.vehicleIdAttrHandle);
        vehicleAttributes.add(this.vehicleIsFilledAttrHandle);
        vehicleAttributes.add(this.vehicleTimeEnteredAttrHandle);
        vehicleAttributes.add(this.vehicleFuelTypeAttrHandle);

        rtiamb.publishObjectClassAttributes(this.vehicleClassHandle, vehicleAttributes);
        rtiamb.subscribeObjectClassAttributes(this.vehicleClassHandle, vehicleAttributes);

        // Publish NewClient interaction

        this.newClientInteractHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.NewClient");
        this.newClientVehicleIdParamHandle = rtiamb.getParameterHandle(this.newClientInteractHandle, "vehicleId");
        rtiamb.publishInteractionClass(this.newClientInteractHandle);


        this.log("Published and Subscribed");
    }

    void sendNewClient(int vehicleId) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);
        Uint32 value = new Uint32(vehicleId);
        parameters.put(this.newClientVehicleIdParamHandle, value.getByteArray());
        rtiamb.sendInteraction(this.newClientInteractHandle, parameters, generateTag());
    }

    protected void runSimulation() throws RTIexception {
        // Register (create) a new instance of Vehicle
        // TODO: This object must be created on request (randomly), not once
        // TODO: Create only a subclass of Vehicle, i.e. Car or Motorcycle
        ObjectInstanceHandle vehicle = rtiamb.registerObjectInstance(this.vehicleClassHandle);
        log("Registered Object, handle=" + vehicle);

        for (int i = 0; i < ITERATIONS; i++) {
//			updateAttributeValues( objectHandle );

            if (i % 3 == 1) {
                this.sendNewClient(i);
            }

            advanceTime(1.0);
            log("Time Advanced to " + this.fedamb.getFederateTime());
        }

        // TODO: Vehicles must be deleted when requested by the Vehicle federate
        rtiamb.deleteObjectInstance(vehicle, generateTag());
        log("Deleted Object, handle=" + vehicle);
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