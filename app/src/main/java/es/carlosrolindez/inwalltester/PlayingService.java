package es.carlosrolindez.inwalltester;


import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;


public class  PlayingService extends MediaSessionCompat.Callback  {
    private static String TAG = "PlayingService";
    private MediaPlayer mediaPlayer1 = null;
    private MediaPlayer mediaPlayer2 = null;
    private Context mContext;

    public PlayingService(Context context) {
        this.mContext = context;
    }

    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
        if (mediaButtonEvent != null) {
            KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
//            Log.e(TAG, event.toString());
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        Log.e(TAG,"PlayPause");
                        onPause();
                        return true;

                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        onPlay();
                        return true;

                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        onSkipToNext();
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        onSkipToPrevious();
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_STOP:
                        onStop();
                        return true;
                    default:
                        Log.e(TAG,"others");
                }
            }
        }
        return true; //super.onMediaButtonEvent(mediaButtonEvent);
    }

    @Override
    public void onPlay() {
        Log.e(TAG,"Play");
//        super.onPlay();
        if (mediaPlayer1==null) {
            mediaPlayer1 = MediaPlayer.create(mContext, DeviceFilter.musicTrack(0));
            mediaPlayer1.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer2 = MediaPlayer.create(mContext, DeviceFilter.musicTrack(1));
            mediaPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //        AudioManager mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

            if (DeviceFilter.TAG.equals("INWALL"))
                mediaPlayer1.setLooping(true);
            mediaPlayer1.start();
        }


    }


    @Override
    public void onPause() {
        Log.e(TAG,"Pause");
 //       super.onPause();
        mediaPlayer1.pause();
        mediaPlayer2.pause();


    }

    @Override
    public void onSkipToNext() {
        Log.e(TAG,"onSkipToNext");
  //      super.onSkipToNext();
 /*       if (mediaPlayer != null) {
            mediaPlayer.stop();
//            mediaPlayer.setLooping(false);
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(mContext, DeviceFilter.musicTrack(1));
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }*/
        mediaPlayer2.start();
        mediaPlayer1.pause();

    }

    @Override
    public void onSkipToPrevious() {
        Log.e(TAG,"onSkipToPrevious");
        super.onSkipToPrevious();

    }

    @Override
    public void onSkipToQueueItem(long id) {
        Log.e(TAG,"onSkipToQueueItem");
        super.onSkipToQueueItem(id);
    }


    @Override
    public void onSeekTo(long pos) {
        Log.e(TAG,"onSeekTo");
        super.onSeekTo(pos);
    }

    @Override
    public void onStop() {
        Log.e(TAG,"onStop");
 //       super.onStop();


        if (mediaPlayer1!=null) {
            mediaPlayer1.stop();
            mediaPlayer1.release();
            mediaPlayer1 = null;
        }

        if (mediaPlayer2!=null) {
            mediaPlayer2.stop();
            mediaPlayer2.release();
            mediaPlayer2 = null;
        }

    }


    @Override
    public void onCommand(String command, Bundle extras, ResultReceiver cb) {
        Log.e(TAG,"onCommand");
        super.onCommand(command, extras, cb);
    }


}