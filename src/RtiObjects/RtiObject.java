package RtiObjects;

import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.RTIexception;

public abstract class RtiObject {
    protected EncoderFactory encoderFactory;
    protected RTIambassador rtiamb;
    protected ObjectInstanceHandle instance;

    protected RtiObject(RTIambassador rtiamb) throws RTIexception {
        this.encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
        this.rtiamb = rtiamb;
    }

    public RtiObject() {

    }

    public String toString() {
        return instance.toString();
    }

    protected void sendUpdate(AttributeHandleValueMap attributes) throws RTIexception {
        rtiamb.updateAttributeValues(this.instance, attributes, null);
    }

    public void destroy() throws RTIexception {
        rtiamb.deleteObjectInstance(this.instance, null);
    }
}
