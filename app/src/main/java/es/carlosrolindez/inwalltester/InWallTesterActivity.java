package es.carlosrolindez.inwalltester;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
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
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class InWallTesterActivity extends AppCompatActivity {
	private static String TAG = "InWall Tester";

    private BluetoothAdapter mBluetoothAdapter = null;
    
	private final InWallHandler  handler = new InWallHandler(this);

	private TextView message;
	private TextView messageAux;
	
	private static ArrayAdapter<String> deviceListAdapter = null;
	private static ArrayList<String> deviceList;

	private AudioManager mAudioManager;
	private ComponentName mRemoteControlResponder;
    private MediaSession mSession;

    private ProgressBar progressBar;

    private A2dpService a2dpService;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_inwall_tester);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progress_spinner);
        setProgressBar(true);

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
        	
    }
	
    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        } else {
            a2dpService = new A2dpService(this,handler);
            Log.e("FTPServicePing","Started");
        }
        
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    a2dpService = new A2dpService(this,handler);
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
	protected void onDestroy() {
        a2dpService.closeService();
		super.onDestroy();
	}

    public void setProgressBar(boolean visible) {
        if (visible)    progressBar.setVisibility(View.VISIBLE);
        else            progressBar.setVisibility(View.INVISIBLE);
    }

	public static class InWallHandler extends Handler {

	    public static final int MESSAGE_CONNECTED = 1; 
	    public static final int MESSAGE_DISCONNECTED = 2; 
	    public static final int MESSAGE_FOUND = 3; 
	    private String deviceMessage;
	    private String deviceMAC;
	    private String deviceName;
	    
	    private Context mLocalContext = null;

	    InWallHandler(Context context) {
	    	mLocalContext = context;
	    }
	    
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case MESSAGE_CONNECTED:
	            	deviceMessage = (String) msg.obj;
	            	if (deviceMessage.length()>13) {
	            		deviceName = deviceMessage.substring(17);
	            		deviceMAC = deviceMessage.substring(0, 17);
	            	}
                    ((InWallTesterActivity)mLocalContext).message.setText(deviceName);
                    ((InWallTesterActivity)mLocalContext).messageAux.setText(deviceMAC);


            	    if ( (deviceName.length()!=11) || (!deviceName.substring(0,7).equals("KINGBT-")) ) {
                        ((InWallTesterActivity)mLocalContext).message.setTextColor(Color.parseColor("#FF0000"));
	            	} else {
                        ((InWallTesterActivity)mLocalContext).message.setTextColor(Color.parseColor("#00FF00"));
						if (!deviceList.contains(((InWallTesterActivity)mLocalContext).message.getText().toString()))
						{
							deviceList.add(0,((InWallTesterActivity)mLocalContext).message.getText().toString());
							deviceListAdapter.notifyDataSetChanged(); 
						}
	            	}


	                break;
	            case MESSAGE_DISCONNECTED:
                    ((InWallTesterActivity)mLocalContext).message.setText(mLocalContext.getResources().getString(R.string.searching));
                    ((InWallTesterActivity)mLocalContext).messageAux.setText("");
                    ((InWallTesterActivity)mLocalContext).message.setTextColor(Color.parseColor("#dddddd"));

	                break;
	            case MESSAGE_FOUND:
	            	deviceName = (String) msg.obj;
                    ((InWallTesterActivity)mLocalContext).messageAux.setText(mLocalContext.getResources().getString(R.string.found) + " " + deviceName);

	                break;
   	

	        }
	    }
	}

}
