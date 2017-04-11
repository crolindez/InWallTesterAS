package es.carlosrolindez.inwalltester;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;

import es.carlosrolindez.btcomm.BtListenerManager;
import es.carlosrolindez.btcomm.bta2dpcomm.BtA2dpConnectionManager;

public class InWallTesterActivity extends AppCompatActivity implements BtListenerManager.RfListener<BluetoothDevice,BtListenerManager.BtEvent>,
        BtA2dpConnectionManager.BtA2dpProxyListener {

	private final static String TAG = "InWall Tester";
    private static final int REQUEST_ENABLE_BT = 1;

    private enum ActivityState {SCANNING, CONNECTED}
    private ActivityState activityState = ActivityState.SCANNING;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BtListenerManager mBtListenerManager = null;
    private BtA2dpConnectionManager mBtA2dpConnectionManager = null;

	private TextView message;
	private TextView messageAux;

    private MenuItem scanButton;
    private MenuItem mActionProgressItem;

	private static ArrayAdapter<String> deviceListAdapter = null;
	private static ArrayList<String> deviceList;

	private AudioManager mAudioManager;
	private ComponentName mRemoteControlResponder;
    private MediaSession mSession;



    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inwall_tester);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
		    Toast.makeText(this, getString(R.string.bt_not_availabe), Toast.LENGTH_LONG).show();
		    finish();
		}	
		
		ListView listView = (ListView) findViewById(R.id.list);
		deviceList = new ArrayList<>();
		deviceListAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, android.R.id.text1,deviceList);
		listView.setAdapter(deviceListAdapter); 

        message =(TextView) findViewById(R.id.DeviceName); 
        messageAux =(TextView) findViewById(R.id.DeviceFound);

        mBtListenerManager = new BtListenerManager(getApplication(),this);
        mBtA2dpConnectionManager = new BtA2dpConnectionManager(getApplication(),this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if (mBtA2dpConnectionManager!=null) mBtA2dpConnectionManager.disconnectBluetoothA2dp();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bt_not_enabled,Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }



    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        setState(activityState);

        if (Build.VERSION.SDK_INT < 21) {
            mRemoteControlResponder = new ComponentName(getPackageName(),RemoteControlReceiver.class.getName());
            mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);
        } else {
            mSession =  new MediaSession(this,getPackageName());
            Intent intent = new Intent(this, RemoteControlReceiver.class);
            PendingIntent pintent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mSession.setMediaButtonReceiver(pintent);
            mSession.setActive(true);
            PlaybackState state = new PlaybackState.Builder()
                    .setActions(PlaybackState.ACTION_FAST_FORWARD | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_STOP)
                    .setState(PlaybackState.STATE_PLAYING, 0, 1, PlaybackState.PLAYBACK_POSITION_UNKNOWN)
                    .build();
            // TODO Next, previous
            mSession.setPlaybackState(state);
        }

    }	

	@SuppressWarnings("deprecation")
    @Override
	protected void onPause() {

		super.onPause();
        if (Build.VERSION.SDK_INT < 21) {
            mAudioManager.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
        } else {
            mSession.release();
        }

	}

    @Override
    protected void onStop() {
        super.onStop();
        if (mBluetoothAdapter!=null) mBluetoothAdapter.cancelDiscovery();
        if  (mBtListenerManager!=null) mBtListenerManager.closeService();
        if  (mBtA2dpConnectionManager!=null) mBtA2dpConnectionManager.closeManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mActionProgressItem = menu.findItem(R.id.mActionProgress);
        scanButton = menu.findItem(R.id.mScan);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mScan) {
            setState(ActivityState.SCANNING);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setState(ActivityState state) {
        activityState = state;
        switch (state) {
            case CONNECTED:
                if (mActionProgressItem!=null)  mActionProgressItem.setVisible(false);
                if (scanButton!=null)           scanButton.setVisible(true);
                break;

            case SCANNING:
                if (mActionProgressItem!=null)  mActionProgressItem.setVisible(true);
                if (scanButton!=null)           scanButton.setVisible(false);
                mBluetoothAdapter.startDiscovery();
                if (mBtA2dpConnectionManager!=null) mBtA2dpConnectionManager.disconnectBluetoothA2dp();
                if (mBtListenerManager!=null) mBtListenerManager.searchBtDevices();
                break;

        }
    }


    @Override
    public void addRfDevice(String name, BluetoothDevice device) {
        if (activityState!=ActivityState.SCANNING) {
            return;
        }

        messageAux.setText(getResources().getString(R.string.found) + " " + name);

        if (DeviceFilter.filterAddress(device.getAddress())) {
            if (activityState==ActivityState.SCANNING) {
                setState(ActivityState.CONNECTED);
                bond2BtA2dp(device);
            }
        }
    }

    public void notifyRfEvent(BluetoothDevice device,  BtListenerManager.BtEvent event) {
        switch (event) {
            case DISCOVERY_FINISHED:
                if (activityState==ActivityState.SCANNING) {
                    mBluetoothAdapter.startDiscovery();
                }
                break;

            case CONNECTED:
                    Toast.makeText(this, device.getName() + " Connected", Toast.LENGTH_SHORT).show();


                    message.setText(device.getName());
                    messageAux.setText(device.getAddress());
                    if (DeviceFilter.filterName(device.getName())) {
                        message.setTextColor(Color.parseColor("#FF0000"));
                    } else {
                        message.setTextColor(Color.parseColor("#00FF00"));
                        if (!deviceList.contains(device.getName())) {
                            deviceList.add(0, device.getName());
                            deviceListAdapter.notifyDataSetChanged();
                        }
                    }
  //              playBt();
                break;

            case DISCONNECTED:
                message.setText(this.getResources().getString(R.string.searching));
                message.setTextColor(Color.parseColor("#dddddd"));
                messageAux.setText("");

                stopPlayBt();
                Toast.makeText(this, device.getName() + " Disconnected", Toast.LENGTH_SHORT).show();

                removeBond(device);

                setState(ActivityState.SCANNING);

                break;

            case BONDED:
                if (device.getBondState()==BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(this, device.getName() + " Bonded", Toast.LENGTH_SHORT).show();
                    connect2BtA2dp(device);

                }
                break;

        }
    }

    public void notifyBtA2dpEvent(BluetoothDevice device,  BtA2dpConnectionManager.BtA2dpEvent event) {

        switch (event) {
            case CONNECTED:
                Log.e(TAG,"A2DP CONNECTED");
                break;

            case DISCONNECTED:
                Log.e(TAG,"A2DP DISCONNECTED");
                break;

        }


    }

    private void bond2BtA2dp(BluetoothDevice device) {
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            Log.e(TAG, "createBond");
            createBond(device);
        } else {
            connect2BtA2dp(device);
        }
    }


    private void connect2BtA2dp(BluetoothDevice device) {
        Log.e(TAG, "connect2BtA2dp");
        if (mBtA2dpConnectionManager!=null)
            mBtA2dpConnectionManager.disconnectBluetoothA2dp();
        if (mBtA2dpConnectionManager!=null)
            mBtA2dpConnectionManager.connectBluetoothA2dp(device);

    }

    public void createBond(BluetoothDevice btDevice)
    {
        if (Build.VERSION.SDK_INT >= 19) {
            btDevice.createBond();
        } else {
            try
            {
                Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
                Method createBondMethod = class1.getMethod("createBond");
                createBondMethod.invoke(btDevice);
            }
            catch (Exception e)
            {
            }
        }
    }

    public boolean removeBond(BluetoothDevice btDevice) {
        try {
            Class<?> btClass = Class.forName("android.bluetooth.BluetoothDevice");
            Method removeBondMethod = btClass.getMethod("removeBond");
            return (Boolean) removeBondMethod.invoke(btDevice);
        }catch (Exception e) {
        }
        return true;

    }

/*    public void playBt()
    {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT >= 19) {

                    long eventtime = SystemClock.uptimeMillis() - 1;
                    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
                    mAudioManager.dispatchMediaKeyEvent(downEvent);

                    eventtime++;
                    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
                    mAudioManager.dispatchMediaKeyEvent(upEvent);
                } else {
                    long eventtime = SystemClock.uptimeMillis() - 1;

                    Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                    Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);

                    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
                    eventtime++;
                    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0);


                    downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
                    sendBroadcast(downIntent, null);

                    upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
                    sendBroadcast(upIntent, null);
                }
            }
        }, 1500);
    }
*/
    public void stopPlayBt()
    {
        if (Build.VERSION.SDK_INT >= 19) {
            long eventtime = SystemClock.uptimeMillis() - 1;
            KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP, 0);
            mAudioManager.dispatchMediaKeyEvent(downEvent);

            eventtime++;
            KeyEvent upEvent = new KeyEvent(eventtime,eventtime,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_STOP, 0);
            mAudioManager.dispatchMediaKeyEvent(upEvent);
        } else {
            long eventtime = SystemClock.uptimeMillis() - 1;

            Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);

            KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP, 0);
            eventtime++;
            KeyEvent upEvent = 	 new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_STOP, 0);

            downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
            this.sendBroadcast(downIntent, null);

            upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
            this.sendBroadcast(upIntent, null);
        }
    }

}
