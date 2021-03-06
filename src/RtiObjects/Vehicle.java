package RtiObjects;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.RTIexception;
import util.FuelEnum;
import util.Uint32;

public class Vehicle extends RtiObject {
    private static ObjectClassHandle classHandle;

    private int id;
    private boolean isFilled;
    private int timeEntered;
    private FuelEnum fuelType;

    protected static AttributeHandle idAttrHandle;
    protected static AttributeHandle isFilledAttrHandle;
    protected static AttributeHandle timeEnteredAttrHandle;
    protected static AttributeHandle fuelTypeAttrHandle;

    public Vehicle(int id, boolean isFilled, int timeEntered, FuelEnum fuelType) {
        super();
        this.id = id;
        this.isFilled = isFilled;
        this.timeEntered = timeEntered;
        this.fuelType = fuelType;
    }

    Vehicle(RTIambassador rtiamb) throws RTIexception {
        super(rtiamb);
    }

    public void setInitialAttributeValues(int id, boolean isFilled, int timeEntered, FuelEnum fuelType) throws RTIexception {
        this.id = id;
        this.isFilled = isFilled;
        this.timeEntered = timeEntered;
        this.fuelType = fuelType;

        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(4);
        attributes.put(idAttrHandle, new Uint32(id).getByteArray());
        attributes.put(isFilledAttrHandle, encoderFactory.createHLAboolean(isFilled).toByteArray());
        attributes.put(timeEnteredAttrHandle, new Uint32(timeEntered).getByteArray());
        attributes.put(fuelTypeAttrHandle, fuelType.getByteArray());
        this.sendUpdate(attributes);
    }

    public void updateIsFilled(boolean isFilled) throws RTIexception {
        if (this.isFilled != isFilled) {
            this.isFilled = isFilled;

            AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
            attributes.put(isFilledAttrHandle, encoderFactory.createHLAboolean(isFilled).toByteArray());
            this.sendUpdate(attributes);
        }
    }

    public int getId() {
        return id;
    }

    public boolean isFilled() {
        return isFilled;
    }

    public int getTimeEntered() {
        return timeEntered;
    }

    public FuelEnum getFuelType() {
        return fuelType;
    }

    public static AttributeHandle getIdAttrHandle() {
        return idAttrHandle;
    }

    public static AttributeHandle getIsFilledAttrHandle() {
        return isFilledAttrHandle;
    }

    public static AttributeHandle getTimeEnteredAttrHandle() {
        return timeEnteredAttrHandle;
    }

    public static AttributeHandle getFuelTypeAttrHandle() {
        return fuelTypeAttrHandle;
    }

    void setId(int id) {
        this.id = id;
    }

    void setIsFilled(boolean isFilled) {
        this.isFilled = isFilled;
    }

    void setTimeEntered(int timeEntered) {
        this.timeEntered = timeEntered;
    }

    void setFuelType(FuelEnum fuelType) {
        this.fuelType = fuelType;
    }

    protected static void registerHandles(RTIambassador rtiamb, ObjectClassHandle classHandle, boolean publish, boolean subscribe) throws RTIexception {
        idAttrHandle = rtiamb.getAttributeHandle(classHandle, "id");
        isFilledAttrHandle = rtiamb.getAttributeHandle(classHandle, "isFilled");
        timeEnteredAttrHandle = rtiamb.getAttributeHandle(classHandle, "timeEntered");
        fuelTypeAttrHandle = rtiamb.getAttributeHandle(classHandle, "fuelType");

        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(idAttrHandle);
        attributes.add(isFilledAttrHandle);
        attributes.add(timeEnteredAttrHandle);
        attributes.add(fuelTypeAttrHandle);

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
        classHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Vehicle");
        return classHandle;
    }
}
