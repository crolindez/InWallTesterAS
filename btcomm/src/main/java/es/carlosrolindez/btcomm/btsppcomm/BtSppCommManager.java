package es.carlosrolindez.btcomm.btsppcomm;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import es.carlosrolindez.rfcomm.RfCommManager;


public class BtSppCommManager extends RfCommManager<BluetoothSocket> {
    private static final String TAG = "BtSppCommManager";

    public BtSppCommManager(Context context) {
        super(context);
    }

    @Override
    protected InputStream getInputStream() {
        InputStream is = null;

        try {
            is = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    @Override
    protected OutputStream getOutputStream() {
        OutputStream os = null;

        try {
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return os;
    }

    @Override
    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BluetoothDevice getConnectedDevice(){
        if (socket!=null) {
            if (socket.isConnected())
                return socket.getRemoteDevice();
        }

        return null;
    }

}
