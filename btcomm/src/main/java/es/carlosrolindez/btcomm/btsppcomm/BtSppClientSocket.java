package es.carlosrolindez.btcomm.btsppcomm;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

import es.carlosrolindez.btcomm.BtConstants;
import es.carlosrolindez.rfcomm.RfClientSocket;


public class BtSppClientSocket extends RfClientSocket<BluetoothSocket,BluetoothDevice> {
    private static final String TAG = "BtSppClientSocket";

    public BtSppClientSocket(BtSppCommManager service, BluetoothDevice device) {
        super(service, device);
    }


    protected BluetoothSocket createSocket(BluetoothDevice device){
        BluetoothSocket mSocket;
        try {
            mSocket = device.createRfcommSocketToServiceRecord(BtConstants.SPP_UUID);

        } catch (IOException e) {
 //           Log.e(TAG,"Spp Connected - Exception Creating socket");
            e.printStackTrace();
            mSocket = null;
        }
        return mSocket;
    }

    protected void connectSocket() {
        try {
            mSocket.connect();

        } catch (IOException e) {
//            Log.e(TAG,"Spp Connected - Exception Connecting socket");
//            Log.e(TAG,e.toString());
            try {
                mSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
//TODO return false if failed
            return;
        }
        mCommManager.setSocket(mSocket,false);
//TODO return true if success
    }
}
