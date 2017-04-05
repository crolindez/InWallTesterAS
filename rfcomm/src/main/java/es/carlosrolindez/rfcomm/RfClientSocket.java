package es.carlosrolindez.rfcomm;


public abstract class RfClientSocket<TypeRfSocket, TypeRfDevice> extends Thread {

    private static final String TAG = "RfClientSocket";
    protected final RfCommManager<TypeRfSocket> mCommManager;
    private final TypeRfDevice mRfDevice;
    protected TypeRfSocket mSocket;

    abstract protected TypeRfSocket createSocket(TypeRfDevice device);
    abstract protected void connectSocket();


    protected RfClientSocket(RfCommManager<TypeRfSocket> commManager, TypeRfDevice device) {

        this.mCommManager = commManager;
        this.mRfDevice = device;

    }

    @Override
    public void run() {
        this.mSocket = createSocket(mRfDevice);
        if (this.mSocket!=null)
            connectSocket();
    }

}
