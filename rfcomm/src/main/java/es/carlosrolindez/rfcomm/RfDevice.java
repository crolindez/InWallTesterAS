package es.carlosrolindez.rfcomm;


public abstract class RfDevice<TypeRfDevice> {
    private static String TAG = "TypeRfDevice";

    public String deviceName;

    public final TypeRfDevice mDevice;
    public int mPort;
    public boolean deviceConnected;

    protected RfDevice(String name, TypeRfDevice device, int port) {
        deviceName = name;
        deviceConnected = false;
        mDevice = device;
        mPort = port;
    }

    public abstract String getAddress();

}
