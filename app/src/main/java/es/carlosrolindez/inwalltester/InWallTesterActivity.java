package es.carlosrolindez.inwalltester;

import android.annotation.TargetApi;
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

    private static final String inWallFootprint = "00:0D:18";
    private static final String inWall2Footprint = "5C:0E:23";

    private enum ActivityState {SCANNING, CONNECTED}
    private final ActivityState activityState = ActivityState.SCANNING;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BtListenerManager mBtListenerManager = null;
    private BtA2dpConnectionManager mBtA2dpConnectionManager = null;

	private TextView message;
	private TextView messageAux;

    private MenuItem mActionProgressItem;

	private static ArrayAdapter<String> deviceListAdapter = null;
	private static ArrayList<String> deviceList;

	private AudioManager mAudioManager;
	private ComponentName mRemoteControlResponder;
    private MediaSession mSession;

    private AudioManager am;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inwall_tester);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

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

        setProgressBar(ActivityState.SCANNING);
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    startRfListening();
                } else {
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
        else
            startRfListening();

        if (Build.VERSION.SDK_INT < 21) {
            mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
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
        if  (mBtListenerManager!=null) mBtListenerManager.closeService();
        if  (mBtA2dpConnectionManager!=null) mBtA2dpConnectionManager.closeManager();

        if (mBluetoothAdapter!=null) mBluetoothAdapter.cancelDiscovery();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mActionProgressItem = menu.findItem(R.id.mActionProgress);
        return true;
    }

/*    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        mActionProgressItem = menu.findItem(R.id.mActionProgress);
        return super.onPrepareOptionsMenu(menu);
    }*/

    private void setProgressBar(ActivityState state) {

        switch (state) {
            case CONNECTED:
                mBluetoothAdapter.cancelDiscovery();
                if (mActionProgressItem!=null)  mActionProgressItem.setVisible(false);
                break;
            case SCANNING:
                mBluetoothAdapter.startDiscovery();
                if (mActionProgressItem!=null)  mActionProgressItem.setVisible(true);
                break;

        }
    }

    private void startRfListening() {
        mBtListenerManager = new BtListenerManager(getApplication(),this);
        mBtA2dpConnectionManager = new BtA2dpConnectionManager(getApplication(),this);
        mBtListenerManager.searchBtDevices();

    }

    @Override
    public void addRfDevice(String name, BluetoothDevice device) {

        messageAux.setText(getResources().getString(R.string.found) + " " + device.getName());
        if (device.getAddress().substring(0,8).equals(inWallFootprint) ||  device.getAddress().substring(0,8).equals(inWall2Footprint)) {
            connect2BtA2dp(device);
        }
    }

    public void notifyRfEvent(BluetoothDevice device,  BtListenerManager.BtEvent event) {
        switch (event) {
            case DISCOVERY_FINISHED:
                if (activityState!=ActivityState.CONNECTED) {
                    mBluetoothAdapter.startDiscovery();
                }
                break;

            case CONNECTED:
 //               if (device.getBondState()==BluetoothDevice.BOND_BONDED) {
                    setProgressBar(ActivityState.CONNECTED);
                    Toast.makeText(this, device.getName() + " Connected", Toast.LENGTH_SHORT).show();
                    playBt();

                    message.setText(device.getName());
                    messageAux.setText(device.getAddress());
                    if ((device.getName().length() != 11) || (!device.getName().substring(0, 7).equals("KINGBT-"))) {
                        message.setTextColor(Color.parseColor("#FF0000"));
                    } else {
                        message.setTextColor(Color.parseColor("#00FF00"));
                        if (!deviceList.contains(device.getName())) {
                            deviceList.add(0, device.getName());
                            deviceListAdapter.notifyDataSetChanged();
                        }
 //                   }

                }
                break;

            case DISCONNECTED:
                setProgressBar(ActivityState.SCANNING);
                message.setText(this.getResources().getString(R.string.searching));
                message.setTextColor(Color.parseColor("#dddddd"));
                messageAux.setText("");

                stopPlayBt();
                Toast.makeText(this, device.getName() + " Disconnected", Toast.LENGTH_SHORT).show();

                removeBond(device);

                mBluetoothAdapter.startDiscovery();

                break;

            case BONDED:

                if (device.getBondState()==BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(this, device.getName() + " Bonded", Toast.LENGTH_SHORT).show();
                    connect2BtA2dp(device);
                    playBt();
                }
                break;

        }
    }

    public void notifyBtA2dpEvent(BluetoothDevice device,  BtA2dpConnectionManager.BtA2dpEvent event) {

        switch (event) {
            case CONNECTED:
                Log.e(TAG,"A2dp connected");
                break;

            case DISCONNECTED:
                Log.e(TAG,"A2dp disconnected");
                break;

        }


    }

    private void connect2BtA2dp(BluetoothDevice device) {
        InWallTesterActivity.this.setProgressBar(ActivityState.CONNECTED);
        if (device.getBondState() != BluetoothDevice.BOND_BONDED)
                createBond(device);
        else {
            if (mBtA2dpConnectionManager!=null)
                mBtA2dpConnectionManager.disconnectBluetoothA2dp();
            if (mBtA2dpConnectionManager!=null)
                mBtA2dpConnectionManager.connectBluetoothA2dp(device);
        }
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

    public void playBt()
    {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT >= 19) {

                    long eventtime = SystemClock.uptimeMillis() - 1;
                    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
                    am.dispatchMediaKeyEvent(downEvent);

                    eventtime++;
                    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
                    am.dispatchMediaKeyEvent(upEvent);
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

    public void stopPlayBt()
    {
        if (Build.VERSION.SDK_INT >= 19) {
            long eventtime = SystemClock.uptimeMillis() - 1;
            KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP, 0);
            am.dispatchMediaKeyEvent(downEvent);

            eventtime++;
            KeyEvent upEvent = new KeyEvent(eventtime,eventtime,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_STOP, 0);
            am.dispatchMediaKeyEvent(upEvent);
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
