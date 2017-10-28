package es.carlosrolindez.btcomm.bta2dpcomm;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.List;


public class BtA2dpConnectionManager {
    private static final String TAG = "BtA2dpConnectionManager";

    public enum BtA2dpEvent {CONNECTED, DISCONNECTED, CHANGING}

    private BluetoothA2dp a2dpProxy;
    private BluetoothHeadset headsetProxy;

    private final BtA2dpProxyListener mBtA2dProxyListener;

    private final Context mContext;


    public interface BtA2dpProxyListener {
        void notifyBtA2dpEvent(BluetoothDevice device, BtA2dpEvent event);
    }

    public BtA2dpConnectionManager(Context context, BtA2dpProxyListener listener) {
        mBtA2dProxyListener = listener;
        mContext = context;
    }

    public void openManager() {
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(mContext, mA2dpProfileListener, BluetoothProfile.A2DP);
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(mContext, mHeadsetProfileListener, BluetoothProfile.HEADSET);
        mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED));
        mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED));
        mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED));
        mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
    }

    public void closeManager() {

        if (a2dpProxy!=null) {
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.A2DP, a2dpProxy);
        }
        a2dpProxy = null;

        if (headsetProxy!=null) {
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.HEADSET, headsetProxy);
        }
        headsetProxy = null;
        mContext.unregisterReceiver(mReceiver);
    }

    public void refreshBluetoothA2dp() {
        if (mBtA2dProxyListener != null) {
            if (a2dpProxy!=null) {
                List<BluetoothDevice> a2dpConnectedDevices = a2dpProxy.getConnectedDevices();
                if (a2dpConnectedDevices.size() != 0) {
                    for (BluetoothDevice device : a2dpConnectedDevices) {
                        mBtA2dProxyListener.notifyBtA2dpEvent(device, BtA2dpEvent.CONNECTED);
                    }
                }
            }
        }
    }

    public void toggleBluetoothA2dp(BluetoothDevice device) {
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            device.createBond();
        }
        else {
            if (a2dpProxy!=null) {
                // First: disconnection if necessary
                List<BluetoothDevice> a2dpConnectedDevices = a2dpProxy.getConnectedDevices();
                if (a2dpConnectedDevices.size() != 0) {
                    for (BluetoothDevice localDevice : a2dpConnectedDevices) { // only one device can be connected
                        disconnectBluetoothA2dp(localDevice);
                        if (localDevice.getAddress().equals(device.getAddress()))
                            // current device was connected:  toggle means disconnect
                            return;
                    }
                }
                //second: A2dp connection to the new device
                if (a2dpProxy != null) {
                    try {
                        a2dpProxy.getClass().getMethod("connect", BluetoothDevice.class).invoke(a2dpProxy, device);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void connectBluetoothA2dp(BluetoothDevice device) {
        if (device.getBondState() != BluetoothDevice.BOND_BONDED)
            device.createBond();
        else {
            if (a2dpProxy!=null) {
                // First: disconnection from old A2dp Device
                List<BluetoothDevice> a2dpConnectedDevices = a2dpProxy.getConnectedDevices();
                if (a2dpConnectedDevices.size() != 0) { // only one device can be connected
                    for (BluetoothDevice localDevice : a2dpConnectedDevices) {
                        if (localDevice.getAddress().equals(device.getAddress()))
                            // If already connected, return
                            return;
                        else
                            disconnectBluetoothA2dp(localDevice);
                    }
                }
                //second: A2dp connection to new device
                if (a2dpProxy != null) {
                    try {
                        a2dpProxy.getClass().getMethod("connect", BluetoothDevice.class).invoke(a2dpProxy, device);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public void disconnectAnyBluetoothA2dp() {

        if (a2dpProxy != null) {
            List<BluetoothDevice> a2dpConnectedDevices = a2dpProxy.getConnectedDevices();
            if (a2dpConnectedDevices.size() != 0) {
                for (BluetoothDevice device : a2dpConnectedDevices) {
                    disconnectBluetoothA2dp(device);
                }
            }
        }
    }

    public void disconnectBluetoothA2dp(BluetoothDevice device) {
        if (a2dpProxy != null) {
            try {
                a2dpProxy.getClass().getMethod("disconnect", BluetoothDevice.class).invoke(a2dpProxy, device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void unbondBluetoothA2dp(BluetoothDevice device) {
        if (a2dpProxy != null) {
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                try {
                    device.getClass().getMethod("removeBond", (Class[]) null).invoke(device, (Object[]) null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private final BluetoothProfile.ServiceListener mA2dpProfileListener = new BluetoothProfile.ServiceListener() {

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProxy = (BluetoothA2dp) proxy;

                //Just to see if some A2dp is still connected
                if (mBtA2dProxyListener != null) {
                    List<BluetoothDevice> a2dpConnectedDevices = a2dpProxy.getConnectedDevices();
                    if (a2dpConnectedDevices.size() != 0) {
                        for (BluetoothDevice device : a2dpConnectedDevices) {
                            mBtA2dProxyListener.notifyBtA2dpEvent(device, BtA2dpEvent.CONNECTED);
                        }
                    }
                }
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
                if (mBtA2dProxyListener != null)
                    mBtA2dProxyListener.notifyBtA2dpEvent(null, BtA2dpEvent.DISCONNECTED);
            }
        }
    };

    private final BluetoothProfile.ServiceListener mHeadsetProfileListener = new BluetoothProfile.ServiceListener() {

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                headsetProxy = (BluetoothHeadset) proxy;
            }
        }

        public void onServiceDisconnected(int profile) {
        }
    };


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device;
            switch (action) {
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED: {
                    int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (state) {
                        case BluetoothA2dp.STATE_CONNECTED:
                            mBtA2dProxyListener.notifyBtA2dpEvent(device, BtA2dpEvent.CONNECTED);
                            // Just in case the device tries to connect also the Headset profile
                            //                  disconnectBluetoothHeadset(device);
                            break;
                        case BluetoothA2dp.STATE_DISCONNECTED:
                            mBtA2dProxyListener.notifyBtA2dpEvent(device, BtA2dpEvent.DISCONNECTED);
                            break;
                        case BluetoothA2dp.STATE_CONNECTING:
                            mBtA2dProxyListener.notifyBtA2dpEvent(device, BtA2dpEvent.CHANGING);
                            break;
                        case BluetoothA2dp.STATE_DISCONNECTING:
                            mBtA2dProxyListener.notifyBtA2dpEvent(device, BtA2dpEvent.CHANGING);
                            break;
                    }

                    break;
                }
                case BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED: {
//                    int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_NOT_PLAYING);
                    break;
                }
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED: {
//                    int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);
                    if (headsetProxy != null) {
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        disconnectBluetoothHeadset(device);
//                        mBtA2dProxyListener.notifyBtA2dpEvent(device, BtA2dpEvent.CONNECTED);
                    }
                    break;
                }
            }
        }

    };

    public void connectBluetoothHeadset(BluetoothDevice device) {
        if (headsetProxy != null) {
            try {
                headsetProxy.getClass()
                        .getMethod("connect", BluetoothDevice.class)
                        .invoke(headsetProxy, device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void disconnectBluetoothHeadset(BluetoothDevice device) {
        if (headsetProxy != null) {

            try {
                headsetProxy.getClass()
                        .getMethod("disconnect", BluetoothDevice.class)
                        .invoke(headsetProxy, device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

