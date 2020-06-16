package CarWash;

import Checkout.CheckoutFederate;
import RtiObjects.Ambassador;
import RtiObjects.CarWashQueue;
import RtiObjects.EntryQueue;
import RtiObjects.Vehicle;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import util.FuelEnum;
import util.Uint32;

public class CarWashFederateAmbassador extends Ambassador {

    private CarWashFederate federate;

    public CarWashFederateAmbassador(CarWashFederate federate) {
        super("CarWashFederateAmbassador");
        this.federate = federate;
    }

    @Override
    public void discoverObjectInstance(ObjectInstanceHandle theObject,
                                       ObjectClassHandle theObjectClass,
                                       String objectName)
            throws FederateInternalError {
        super.discoverObjectInstance(theObject, theObjectClass, objectName);

        instanceToClassMap.put(theObject, theObjectClass);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass,
                                   ParameterHandleValueMap theParameters,
                                   byte[] tag,
                                   OrderType sentOrdering,
                                   TransportationTypeHandle theTransport,
                                   LogicalTime time,
                                   OrderType receivedOrdering,
                                   FederateAmbassador.SupplementalReceiveInfo receiveInfo)
            throws FederateInternalError {
        super.receiveInteraction(interactionClass, theParameters, tag, sentOrdering, theTransport, time, receivedOrdering, receiveInfo);

        if (interactionClass.equals(this.federate.washPaidInteractHandle)) {
            byte[] vehicleIdRaw = theParameters.get(this.federate.washPaidVehicleIdParamHandle);
            if (vehicleIdRaw == null) {
                throw new RuntimeException("Required parameter not supplied: vehicleId");
            }

            int vehicleId = new Uint32(vehicleIdRaw).getValue();
            this.federate.onWashPaid(vehicleId);
        } else {
            throw new RuntimeException("A non-subscribed interaction was received: " + interactionClass);
        }
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject,
                                       AttributeHandleValueMap theAttributes,
                                       byte[] tag,
                                       OrderType sentOrdering,
                                       TransportationTypeHandle theTransport,
                                       LogicalTime time,
                                       OrderType receivedOrdering,
                                       SupplementalReflectInfo reflectInfo)
            throws FederateInternalError {
        super.reflectAttributeValues(theObject, theAttributes, tag, sentOrdering, theTransport, time, receivedOrdering, reflectInfo);

        if (instanceToClassMap.get(theObject).equals(CarWashQueue.getClassHandle())) {
            Uint32 maxVehicles = null;
            Uint32 currentVehicleCount = null;
            Uint32 earliestVehicleId = null;

            byte[] maxVehiclesRaw = theAttributes.get(CarWashQueue.getMaxVehiclesAttrHandle());
            if (maxVehiclesRaw != null) {
                maxVehicles = new Uint32(maxVehiclesRaw);
            }

            byte[] currentVehicleCountRaw = theAttributes.get(CarWashQueue.getCurrentVehicleCountAttrHandle());
            if (currentVehicleCountRaw != null) {
                currentVehicleCount = new Uint32(currentVehicleCountRaw);
            }

            byte[] earliestVehicleIdRaw = theAttributes.get(CarWashQueue.getEarliestVehicleIdAttrHandle());
            if (earliestVehicleIdRaw != null) {
                earliestVehicleId = new Uint32(earliestVehicleIdRaw);
            }

            this.federate.onUpdatedCarWashQueue(currentVehicleCount.getValue(), earliestVehicleId.getValue());
        }
    }
}
