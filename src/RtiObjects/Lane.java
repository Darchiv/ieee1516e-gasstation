package RtiObjects;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.RTIexception;
import util.Uint32;

public class Lane extends RtiObject {
    private static ObjectClassHandle classHandle;

    public int gasPumpId;
    public int currentVehicleCount;
    public int maxVehicles;
    public int earliestVehicleId;

    protected static AttributeHandle gasPumpIdAttrHandle;
    protected static AttributeHandle currentVehicleCountAttrHandle;
    protected static AttributeHandle maxVehiclesAttrHandle;
    protected static AttributeHandle earliestVehicleIdAttrHandle;

    public Lane(int gasPumpId, int currentVehicleCount, int maxVehicles, int earliestVehicleId) {
        super();
        this.gasPumpId = gasPumpId;
        this.currentVehicleCount = currentVehicleCount;
        this.maxVehicles = maxVehicles;
        this.earliestVehicleId = earliestVehicleId;
    }

    Lane(RTIambassador rtiamb) throws RTIexception {
        super(rtiamb);

        this.instance = rtiamb.registerObjectInstance(classHandle);
    }

    public void setInitialAttributeValues(int gasPumpId, int currentVehicleCount, int maxVehicles, int earliestVehicleId) throws RTIexception {
        this.gasPumpId = gasPumpId;
        this.currentVehicleCount = currentVehicleCount;
        this.maxVehicles = maxVehicles;
        this.earliestVehicleId = earliestVehicleId;

        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(4);
        attributes.put(gasPumpIdAttrHandle, new Uint32(gasPumpId).getByteArray());
        attributes.put(currentVehicleCountAttrHandle, new Uint32(currentVehicleCount).getByteArray());
        attributes.put(maxVehiclesAttrHandle, new Uint32(maxVehicles).getByteArray());
        attributes.put(earliestVehicleIdAttrHandle, new Uint32(earliestVehicleId).getByteArray());
        this.sendUpdate(attributes);
    }

    public void updateQueue(int currentVehicleCount, int earliestVehicleId) throws RTIexception {
        this.currentVehicleCount = currentVehicleCount;
        this.earliestVehicleId = earliestVehicleId;

        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(3);
        attributes.put(gasPumpIdAttrHandle, new Uint32(gasPumpId).getByteArray());
        attributes.put(currentVehicleCountAttrHandle, new Uint32(currentVehicleCount).getByteArray());
        attributes.put(earliestVehicleIdAttrHandle, new Uint32(earliestVehicleId).getByteArray());
        this.sendUpdate(attributes);
    }

    public int getGasPumpId() {
        return gasPumpId;
    }

    public int getCurrentVehicleCount() {
        return currentVehicleCount;
    }

    public int getMaxVehicles() {
        return maxVehicles;
    }

    public int getEarliestVehicleId() {
        return earliestVehicleId;
    }

    public static AttributeHandle getGasPumpIdAttrHandle() {
        return gasPumpIdAttrHandle;
    }

    public static AttributeHandle getCurrentVehicleCountAttrHandle() {
        return currentVehicleCountAttrHandle;
    }

    public static AttributeHandle getMaxVehiclesAttrHandle() {
        return maxVehiclesAttrHandle;
    }

    public static AttributeHandle getEarliestVehicleIdAttrHandle() {
        return earliestVehicleIdAttrHandle;
    }

    protected static void registerHandles(RTIambassador rtiamb, ObjectClassHandle classHandle, boolean publish, boolean subscribe) throws RTIexception {
        gasPumpIdAttrHandle = rtiamb.getAttributeHandle(classHandle, "gasPumpId");
        currentVehicleCountAttrHandle = rtiamb.getAttributeHandle(classHandle, "currentVehicleCount");
        maxVehiclesAttrHandle = rtiamb.getAttributeHandle(classHandle, "maxVehicles");
        earliestVehicleIdAttrHandle = rtiamb.getAttributeHandle(classHandle, "earliestVehicleId");

        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(gasPumpIdAttrHandle);
        attributes.add(currentVehicleCountAttrHandle);
        attributes.add(maxVehiclesAttrHandle);
        attributes.add(earliestVehicleIdAttrHandle);

        if (publish) {
            rtiamb.publishObjectClassAttributes(classHandle, attributes);
        }

        if (subscribe) {
            rtiamb.subscribeObjectClassAttributes(classHandle, attributes);
        }
    }

    public static ObjectClassHandle getClassHandle() {
        return classHandle;
    }

    protected static ObjectClassHandle registerClassHandle(RTIambassador rtiamb) throws RTIexception {
        classHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Lane");
        return classHandle;
    }
}
