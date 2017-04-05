package es.carlosrolindez.btcomm.btsppcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;

import es.carlosrolindez.btcomm.BtConstants;
import es.carlosrolindez.rfcomm.RfServerManager;


public class BtSppServerManager extends RfServerManager<BluetoothSocket,BluetoothServerSocket> {
    private static final String TAG = "BtSppServerManager";

    private static final String CRService = "es.carlosrolindez.btcomm";

    private BluetoothAdapter mBluetoothAdapter;

    public BtSppServerManager(Context context, BtSppCommManager commManager) {
        super(context,commManager);
    }

    @Override
    public void initializeServerManager(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public BluetoothServerSocket openServerSocket(){
        BluetoothServerSocket server;

        try {
            server = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(CRService, BtConstants.SPP_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            server = null;
        }
        return server;
    }

    public BluetoothSocket serviceAccept(BluetoothServerSocket server) {
        BluetoothSocket socket;
        try {
            socket = server.accept();
        } catch (IOException e) {
            e.printStackTrace();
            socket = null;
        }
        return socket;
    }

    public void closeServerSocket(BluetoothServerSocket server){
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
