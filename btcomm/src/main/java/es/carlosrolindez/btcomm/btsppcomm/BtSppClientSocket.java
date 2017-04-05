package es.carlosrolindez.btcomm.btsppcomm;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

import es.carlosrolindez.btcomm.BtConstants;
import es.carlosrolindez.rfcomm.RfClientSocket;


public class BtSppClientSocket extends RfClientSocket<BluetoothSocket,BluetoothDevice> {

    public BtSppClientSocket(BtSppCommManager service, BluetoothDevice device) {
        super(service, device);
    }


    protected BluetoothSocket createSocket(BluetoothDevice device){
        BluetoothSocket mSocket;
        try {
            mSocket = device.createRfcommSocketToServiceRecord(BtConstants.SPP_UUID);

        } catch (IOException e) {
            e.printStackTrace();
            mSocket = null;
        }
        return mSocket;
    }

    protected void connectSocket() {
        try {
            mSocket.connect();

        } catch (IOException e) {
            // Close the socket
            try {
                mSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return;
        }
        mCommManager.setSocket(mSocket,false);
    }
}
