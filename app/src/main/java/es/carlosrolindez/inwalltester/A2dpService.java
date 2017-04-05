package es.carlosrolindez.inwalltester;

/*

public class A2dpService {
	private static final String TAG = "A2DP Service";

    private static final String inWallFootprint = "00:0D:18";   
    //private static final String inWall2Footprint = "C1:02:5B";
    private static final String inWall2Footprint = "5C:0E:23";
    
    private BluetoothAdapter mBluetoothAdapter ;

	
	private Context mContextBt;

	private boolean mBtA2dpIsBound = false;
	private IBluetoothA2dp iBtA2dp = null;
	private boolean a2dpReceiverRegistered = false;
	
	private BluetoothDevice connectingDevice;
	
	private boolean connectedA2dp;
	
    private InWallTesterActivity.InWallHandler mHandler;
    
    
    private AudioManager am;
    
	

	public A2dpService(Context context,InWallTesterActivity.InWallHandler handler) {
		
		mContextBt = context;
		connectedA2dp = false;
		mHandler = handler;

		am = (AudioManager)mContextBt.getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= 18) {
            final BluetoothManager bluetoothManager = (BluetoothManager) mContextBt.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter(); //Lint Error..
        } else {
            mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        }

        mBluetoothAdapter.getProfileProxy(context, mProfileListener, BluetoothProfile.A2DP);
        doDiscovery();

        IntentFilter filter2;
        if (Build.VERSION.SDK_INT >= 23) {
            filter2 = new IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED);
        } else {
            filter2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        }
		IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);			
        IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter5 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);	
        IntentFilter filter6 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);	    

        context.registerReceiver(mBtReceiver, filter2);
        context.registerReceiver(mBtReceiver, filter3);	  
        context.registerReceiver(mBtReceiver, filter4);	
        context.registerReceiver(mBtReceiver, filter5);	
        context.registerReceiver(mBtReceiver, filter6);	
  
	}
		
	private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

        	stopPlayBt();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mHandler.obtainMessage(InWallTesterActivity.InWallHandler.MESSAGE_FOUND, -1, -1, device.getName()).sendToTarget();
                if (device.getAddress().substring(0,8).equals(inWallFootprint) ||  device.getAddress().substring(0,8).equals(inWall2Footprint)) {
                    switchA2dp(device);
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mHandler.obtainMessage(InWallTesterActivity.InWallHandler.MESSAGE_FOUND, -1, -1, device.getName()).sendToTarget();
        		if (device.getAddress().substring(0,8).equals(inWallFootprint) ||  device.getAddress().substring(0,8).equals(inWall2Footprint)) {
        			switchA2dp(device);
        		}
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	((InWallTesterActivity)context).setProgressBar(false);
            	if (!connectedA2dp) {
            		doDiscovery();	
            	}
            
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            	((InWallTesterActivity)context).setProgressBar(false);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
                if (device.getBondState()==BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(context, device.getName() + " Connected", Toast.LENGTH_SHORT).show();
                    mHandler.obtainMessage(InWallTesterActivity.InWallHandler.MESSAGE_CONNECTED, -1, -1, device.getAddress() + device.getName()).sendToTarget();
                    playBt();   
                } else if (device.getBondState()==BluetoothDevice.BOND_BONDING) {
                } else {
                }
               
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
  //          	stopPlayBt();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
                Toast.makeText(context, device.getName() + " Disconnected", Toast.LENGTH_SHORT).show();
                connectedA2dp = false;
                mHandler.obtainMessage(InWallTesterActivity.InWallHandler.MESSAGE_DISCONNECTED, -1, -1, device.getName()).sendToTarget();
          		if (device.getAddress().substring(0,8).equals(inWallFootprint) || device.getAddress().substring(0,8).equals(inWall2Footprint)) {
        			removeBond(device);
           		}
                doDiscovery();  
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState()==BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(context, device.getName() + " Connected", Toast.LENGTH_SHORT).show();
                    mHandler.obtainMessage(InWallTesterActivity.InWallHandler.MESSAGE_CONNECTED, -1, -1, device.getAddress() + device.getName()).sendToTarget();
                	switchBluetoothA2dp(device);  
                	playBt();  
                } else if (device.getBondState()==BluetoothDevice.BOND_BONDING) {
                } else if (device.getBondState()==BluetoothDevice.BOND_NONE) {
                }
            }
		}

	};

	
	public void playBt()
	{
        if (Build.VERSION.SDK_INT >= 19) {
       		new Handler().postDelayed(new Runnable() {
    		    @Override
				@TargetApi(19)
    		    public void run() {

    				long eventtime = SystemClock.uptimeMillis() - 1;
    				KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
    				am.dispatchMediaKeyEvent(downEvent);

    				eventtime++;
    				KeyEvent upEvent = new KeyEvent(eventtime,eventtime,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PLAY, 0);         
    				am.dispatchMediaKeyEvent(upEvent);

    		  	}
    		}, 1500);
       		
        } else {
			new Handler().postDelayed(new Runnable() {
	            @Override
	            public void run() {

	                long eventtime = SystemClock.uptimeMillis() - 1;

	                Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
	                Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);

	                KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
	                eventtime++;
	                KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0);


	                downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
	                mContextBt.sendBroadcast(downIntent, null);

	                upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
	                mContextBt.sendBroadcast(upIntent, null);


	            }
	        }, 1500);
        }

	
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
	        mContextBt.sendBroadcast(downIntent, null);

	        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
	        mContextBt.sendBroadcast(upIntent, null);
        }



	}

	public void switchBluetoothA2dp(BluetoothDevice device) {
		connectingDevice = device;
		if (!a2dpReceiverRegistered) {
			IntentFilter filter1 = new IntentFilter(Constants.a2dpFilter);
			mContextBt.registerReceiver(mA2dpReceiver, filter1);
			a2dpReceiverRegistered = true;
		}
		Intent i = new Intent(IBluetoothA2dp.class.getName());
        i.setPackage("com.android.bluetooth");
		mContextBt.bindService(i, mBtA2dpServiceConnection, Context.BIND_AUTO_CREATE);
	}	

	public ServiceConnection mBtA2dpServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBtA2dpIsBound = true;
			iBtA2dp = IBluetoothA2dp.Stub.asInterface(service);

			Intent intent = new Intent();
			intent.setAction(Constants.a2dpFilter);
			mContextBt.sendBroadcast(intent);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBtA2dpIsBound = false;

		}


	};

	private final BroadcastReceiver mA2dpReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			new connectA2dpTask().execute();
		}

	};
	

	private class connectA2dpTask extends AsyncTask<String, Void, Boolean> {


		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			a2dpDone();
		}

		protected void onPreExecute() {
		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			
			BluetoothDevice device = connectingDevice;

			BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
			if (mBTA == null || !mBTA.isEnabled())
				return false;


			try {
				if ( (A2dpService.this.iBtA2dp != null) && (A2dpService.this.iBtA2dp.getConnectionState(device) == 0) ) {
					A2dpService.this.iBtA2dp.connect(device);
				} else {
					A2dpService.this.iBtA2dp.disconnect(device);
				}

			} catch (Exception e) {
			}
			return true;
		}

	}	



	
	public void closeService( ){
		a2dpDone();	
		mContextBt.unregisterReceiver(mBtReceiver);
	}
	
	
	private void a2dpDone() {
		if (a2dpReceiverRegistered) {
			mContextBt.unregisterReceiver(mA2dpReceiver);
			a2dpReceiverRegistered = false;
			doUnbindServiceBtA2dp();
		}


	}
	
	public void doUnbindServiceBtA2dp() {
		if (mBtA2dpIsBound) {
			try {
				mContextBt.unbindService(mBtA2dpServiceConnection);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	


	private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                BluetoothA2dp btA2dp = (BluetoothA2dp) proxy;
                List<BluetoothDevice> a2dpConnectedDevices = btA2dp.getConnectedDevices();

                if (a2dpConnectedDevices.size() == 0) {
                    doDiscovery();              	
                }
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.A2DP, btA2dp);
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
            }
        }
    };

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

	
	public void switchA2dp (BluetoothDevice device) {

		
		connectedA2dp = true;	

		if   (device != null) {

			if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
				createBond(device);
			} else {
				BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
				switchBluetoothA2dp(device);
			}

		}
	}


    private void doDiscovery() {
        // Indicate scanning in the title
    	((InWallTesterActivity)mContextBt).setProgressBar(true);

        if (mBluetoothAdapter.isDiscovering()) return;
        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
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

}

*/