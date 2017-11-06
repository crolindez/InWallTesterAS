package es.carlosrolindez.btcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.Set;

import es.carlosrolindez.rfcomm.RfListenerManager;


public class BtListenerManager extends RfListenerManager<BluetoothDevice,BtListenerManager.BtEvent> {
    private static final String TAG = "BtListenerManager";

    public enum BtEvent { DISCOVERY_FINISHED, BONDED, CONNECTED, DISCONNECTED , CHANGING, NOT_BONDED, PAIRING_REQUEST}

    private final Context mContextBt;

    private boolean mBtReceiverRegistered = false;


    public BtListenerManager(Context context, RfListener<BluetoothDevice,BtEvent> listener) {
        super(listener);
        mContextBt = context;
    }

    public void knownBtDevices() {
        BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();

        if (mBTA != null) {

            Set<BluetoothDevice> pairedDevices = mBTA.getBondedDevices();
            if (mRfListener != null) {
                if (pairedDevices.size() > 0) {

                    for (BluetoothDevice device : pairedDevices) {
                        mRfListener.addRfDevice(device.getName(), device);
                    }

                }
            }
        }
    }

    public  void setListenerBtDevices() {

        if (!mBtReceiverRegistered) {
            IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED);
            IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
            IntentFilter filter5 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            IntentFilter filter6 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            IntentFilter filter7 = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);

            mContextBt.registerReceiver(mBtReceiver, filter1);
            mContextBt.registerReceiver(mBtReceiver, filter2);
            mContextBt.registerReceiver(mBtReceiver, filter3);
            mContextBt.registerReceiver(mBtReceiver, filter4);
            mContextBt.registerReceiver(mBtReceiver, filter5);
            mContextBt.registerReceiver(mBtReceiver, filter6);
            mContextBt.registerReceiver(mBtReceiver, filter7);
        }

        mBtReceiverRegistered = true;

  //      sendNames();

    }



    private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mRfListener != null)
                    mRfListener.addRfDevice(device.getName(), device);

                // When discovery is finished, change the Activity title
            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mRfListener != null)
                    mRfListener.addRfDevice(device.getName(), device);

                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mRfListener != null)
                    mRfListener.notifyRfEvent(null, BtEvent.DISCOVERY_FINISHED);

            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mRfListener != null) {
 //                   Log.e(TAG, "ACTION_ACL_CONNECTED");
                    mRfListener.notifyRfEvent(device, BtEvent.CONNECTED);
                }

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mRfListener != null) {
 //                   Log.e(TAG, "ACTION_ACL_DISCONNECTED");
                    mRfListener.notifyRfEvent(device, BtEvent.DISCONNECTED);
                }
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mRfListener != null) {
 //                   Log.e(TAG, "ACTION_PAIRING_REQUEST");
                    mRfListener.notifyRfEvent(device, BtEvent.PAIRING_REQUEST);
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDED:
                        if (mRfListener != null) {
 //                           Log.e(TAG,"BOND_BONDED");
                            mRfListener.notifyRfEvent(device, BtEvent.BONDED);
                        }
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        if (mRfListener != null) {
 //                           Log.e(TAG,"BOND_BONDING");
                            mRfListener.notifyRfEvent(device, BtEvent.CHANGING);
                        }
                        break;
                    case BluetoothDevice.BOND_NONE:
                        if (mRfListener != null) {
  //                          Log.e(TAG,"BOND_NONE");
                            mRfListener.notifyRfEvent(device, BtEvent.NOT_BONDED);
                        }
                        break;
                    default:
                }
            }
        }
    };


    public void closeService() {
        if (mBtReceiverRegistered) {
            mContextBt.unregisterReceiver(mBtReceiver);
            mBtReceiverRegistered = false;
        }
    }

}


