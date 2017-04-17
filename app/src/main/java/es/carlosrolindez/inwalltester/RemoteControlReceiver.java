package es.carlosrolindez.inwalltester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.KeyEvent;


public class RemoteControlReceiver extends BroadcastReceiver {

    private final static String TAG = "RemoteControlReceiver";
    private static MediaPlayer mediaPlayer = null;
    
    public RemoteControlReceiver() {
    }

	@Override
	public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {

            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if ((KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) || (KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()))  {
                if (KeyEvent.ACTION_UP == event.getAction()) {
                    Log.e(TAG, "PLAY");
                    if (mediaPlayer == null) {
                        AudioManager mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                        if (DeviceFilter.TAG.equals("ISELECT"))
                            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
                        mediaPlayer = MediaPlayer.create(context, DeviceFilter.musicTrack(0));
                        if (DeviceFilter.TAG.equals("INWALL"))
                            mediaPlayer.setLooping(true);
                        mediaPlayer.start();
                    }
                }
        			
            } else if  ( (KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()) ) {
                Log.e(TAG,"PAUSE");
        		if (mediaPlayer!=null) {
        			mediaPlayer.pause();
        		}
            } else if (KeyEvent.KEYCODE_MEDIA_STOP == event.getKeyCode()) {
                Log.e(TAG,"STOP");
        		if(mediaPlayer!=null)
        		{
                    mediaPlayer.stop();
                    mediaPlayer.setLooping(false);
                    mediaPlayer.release();
                    mediaPlayer = null;
        		}	
            } else if (KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()) {
                if (KeyEvent.ACTION_UP == event.getAction()) {
                    Log.e(TAG, "NEXT");
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.setLooping(false);
                        mediaPlayer.release();
                        mediaPlayer = MediaPlayer.create(context, DeviceFilter.musicTrack(1));
                        if (DeviceFilter.TAG.equals("INWALL"))
                            mediaPlayer.setLooping(true);
                        mediaPlayer.start();
                    }
                }
            } else
                Log.e(TAG,event.toString());
        }
	}
}
