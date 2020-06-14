package RtiObjects;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.RTIexception;
import util.FuelEnum;
import util.Uint32;

public class GasPump extends RtiObject {
    private static ObjectClassHandle classHandle;

    private int id;
    private boolean isBusy;
    private int currentVehicleId;
    private FuelEnum fuelType;

    protected static AttributeHandle idAttrHandle;
    protected static AttributeHandle isBusyAttrHandle;
    protected static AttributeHandle currentVehicleIdAttrHandle;
    protected static AttributeHandle fuelTypeAttrHandle;

    GasPump(RTIambassador rtiamb) throws RTIexception {
        super(rtiamb);

        this.instance = rtiamb.registerObjectInstance(classHandle);
    }

    public void setInitialAttributeValues(int id, boolean isBusy, int currentVehicleId, FuelEnum fuelType) throws RTIexception {
        this.id = id;
        this.isBusy = isBusy;
        this.currentVehicleId = currentVehicleId;
        this.fuelType = fuelType;

        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(4);
        attributes.put(idAttrHandle, new Uint32(id).getByteArray());
        attributes.put(isBusyAttrHandle, encoderFactory.createHLAboolean(isBusy).toByteArray());
        attributes.put(currentVehicleIdAttrHandle, new Uint32(currentVehicleId).getByteArray());
        attributes.put(fuelTypeAttrHandle, fuelType.getByteArray());
        this.sendUpdate(attributes);
    }

    public void updateIsBusy(boolean isBusy) throws RTIexception {
        if (this.isBusy != isBusy) {
            this.isBusy = isBusy;

            AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
            attributes.put(isBusyAttrHandle, encoderFactory.createHLAboolean(isBusy).toByteArray());
            this.sendUpdate(attributes);
        }
    }

    void setId(int id) {
        this.id = id;
    }

    void setIsBusy(boolean isBusy) {
        this.isBusy = isBusy;
    }

    void setCurrentVehicleId(int currentVehicleId) {
        this.currentVehicleId = currentVehicleId;
    }

    void setFuelType(FuelEnum fuelType) {
        this.fuelType = fuelType;
    }

    protected static void registerHandles(RTIambassador rtiamb, ObjectClassHandle classHandle, boolean publish, boolean subscribe) throws RTIexception {
        idAttrHandle = rtiamb.getAttributeHandle(classHandle, "id");
        isBusyAttrHandle = rtiamb.getAttributeHandle(classHandle, "isBusy");
        currentVehicleIdAttrHandle = rtiamb.getAttributeHandle(classHandle, "currentVehicleId");
        fuelTypeAttrHandle = rtiamb.getAttributeHandle(classHandle, "fuelType");

        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(idAttrHandle);
        attributes.add(isBusyAttrHandle);
        attributes.add(currentVehicleIdAttrHandle);
        attributes.add(fuelTypeAttrHandle);

        if (publish) {
            rtiamb.publishObjectClassAttributes(classHandle, attributes);
        }

        if (subscribe) {
            rtiamb.subscribeObjectClassAttributes(classHandle, attributes);
        }
    }

    protected static ObjectClassHandle registerClassHandle(RTIambassador rtiamb) throws RTIexception {
        classHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.GasPump");
        return classHandle;
    }
}
