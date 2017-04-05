package es.carlosrolindez.rfcomm;


public class RfListenerManager<TypeRfDevice, TypeRfEvent> {
    private static final String TAG = "RfListenerManager";

    protected final RfListener<TypeRfDevice, TypeRfEvent> mRfListener;


    public interface RfListener<RfDevice,RfEvent> {
        void addRfDevice(String name, RfDevice device);
        void notifyRfEvent(RfDevice device, RfEvent event);
    }

    protected RfListenerManager(RfListener<TypeRfDevice, TypeRfEvent> listener) {
        mRfListener = listener;
    }


}


