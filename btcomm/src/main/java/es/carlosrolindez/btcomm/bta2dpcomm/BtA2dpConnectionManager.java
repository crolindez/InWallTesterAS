package es.carlosrolindez.btcomm.bta2dpcomm;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.List;


public class BtA2dpConnectionManager {
    private static final String TAG = "BtA2dpConnectionManager";

    public enum BtA2dpEvent {CONNECTED, DISCONNECTED}

    private BluetoothA2dp a2dpProxy;

    private final BtA2dpProxyListener mBtA2dProxyListener;

    private BluetoothDevice connectedDevice;


    public interface BtA2dpProxyListener {
        void notifyBtA2dpEvent(BluetoothDevice device, BtA2dpEvent event);
    }

    public BtA2dpConnectionManager(Context context, BtA2dpProxyListener listener) {
        mBtA2dProxyListener = listener;
        connectedDevice = null;

        try {
            Class<?> mClass = Class.forName("android.os.ServiceManager");
            mClass.getDeclaredMethod("getService", String.class).invoke(mClass.newInstance(), "bluetooth_a2dp");

            BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, mProfileListener, BluetoothProfile.A2DP);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void connectBluetoothA2dp(BluetoothDevice device) {
        try {
            a2dpProxy.getClass()
                    .getMethod("connect", BluetoothDevice.class)
                    .invoke(a2dpProxy, device);
            connectedDevice = device;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectBluetoothA2dp() {

        if (connectedDevice!=null) {
            try {
                a2dpProxy.getClass()
                        .getMethod("disconnect", BluetoothDevice.class)
                        .invoke(a2dpProxy, connectedDevice);
                connectedDevice = null;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeManager() {

        if (a2dpProxy!=null) {
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.A2DP, a2dpProxy);
        }
        a2dpProxy = null;
    }

    private final BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {

        public void onServiceConnected(int profile, BluetoothProfile proxy) {

            if (profile == BluetoothProfile.A2DP) {

                a2dpProxy = (BluetoothA2dp) proxy;

                if (mBtA2dProxyListener != null) {
                    List<BluetoothDevice> a2dpConnectedDevices = a2dpProxy.getConnectedDevices();
                    if (a2dpConnectedDevices.size() != 0) {
                        for (BluetoothDevice device : a2dpConnectedDevices) {
                            mBtA2dProxyListener.notifyBtA2dpEvent(device, BtA2dpEvent.CONNECTED);
                            connectedDevice = device;

                        }
                    }
                }
            }
        }

        public void onServiceDisconnected(int profile) {

            if (profile == BluetoothProfile.A2DP) {
                if (mBtA2dProxyListener != null)
                    mBtA2dProxyListener.notifyBtA2dpEvent(null,BtA2dpEvent.DISCONNECTED);
            }
        }
    };

}

