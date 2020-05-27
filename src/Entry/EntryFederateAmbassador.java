package Entry;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;
import util.Ambassador;
import util.Hex;
import util.Uint32;

public class EntryFederateAmbassador extends Ambassador {
    private EntryFederate federate;

    public EntryFederateAmbassador(EntryFederate federate) {
        super("EntryFederateAmbassador");
        this.federate = federate;
    }

//    @Override
//    public void receiveInteraction( InteractionClassHandle interactionClass,
//                                    ParameterHandleValueMap theParameters,
//                                    byte[] tag,
//                                    OrderType sentOrdering,
//                                    TransportationTypeHandle theTransport,
//                                    LogicalTime time,
//                                    OrderType receivedOrdering,
//                                    SupplementalReceiveInfo receiveInfo )
//            throws FederateInternalError
//    {
//        StringBuilder builder = new StringBuilder( "Interaction Received:" );
//
//        builder.append( " handle=" + interactionClass );
//        if( interactionClass.equals(this.federate.getClientL1InteractHandle) )
//        {
//            builder.append( " (GetClientL1: " );
//            Uint32 value = new Uint32(theParameters.getValueReference(this.federate.getClientL1VehicleIdParamHandle).array());
//
//            builder.append(value.getValue() + ")");
//        }
//
//        builder.append( ", tag=" + new String(tag) );
//        if( time != null )
//        {
//            builder.append( ", time=" + ((HLAfloat64Time)time).getValue() );
//        }
//
//        log( builder.toString() );
//    }

    @Override
    public void receiveInteraction( InteractionClassHandle interactionClass,
                                    ParameterHandleValueMap theParameters,
                                    byte[] tag,
                                    OrderType sentOrdering,
                                    TransportationTypeHandle theTransport,
                                    LogicalTime time,
                                    OrderType receivedOrdering,
                                    SupplementalReceiveInfo receiveInfo )
            throws FederateInternalError
    {
        super.receiveInteraction(interactionClass, theParameters, tag, sentOrdering, theTransport, time, receivedOrdering, receiveInfo);

        StringBuilder builder = new StringBuilder( "Interaction Received:" );

        // TODO: Currently this only interprets one interaction's value. Need to handle all and pass to the federate instead of printing here

        builder.append( " handle=" + interactionClass );
        if( interactionClass.equals(this.federate.newClientInteractHandle) )
        {
            builder.append( " (NewClient: " );

            for (ParameterHandle parameterHandle : theParameters.keySet()) {
                builder.append("<" + parameterHandle + ", ");
                Uint32 value = new Uint32(theParameters.get(parameterHandle));
                builder.append(value.getValue() + "> ");
            }

            builder.append(")");
        }

        builder.append( ", tag=" + new String(tag) );
        if( time != null )
        {
            builder.append( ", time=" + ((HLAfloat64Time)time).getValue() );
        }

        log( builder.toString() );
    }
}
