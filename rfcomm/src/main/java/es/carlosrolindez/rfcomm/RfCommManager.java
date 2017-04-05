package es.carlosrolindez.rfcomm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public abstract class RfCommManager<TypeRfSocket> {

    private static final String TAG = "RfCommManager";

    public static final String message_content = "byte_chain";

    public static final String STARTED = "es.carlosrolindez.started";
    public static final String MESSAGE = "es.carlosrolindez.message";
    public static final String STOPPED = "es.carlosrolindez.stopped";
    public static final String CLOSED  = "es.carlosrolindez.closed";

    private boolean server= false;
    private boolean connected = false;

    protected TypeRfSocket socket = null;

    private InputStream iStream;
    private OutputStream oStream;
    BlockingQueue<String> mMessageQueue;
    private final int QUEUE_CAPACITY = 5;

    private final LocalBroadcastManager mLocalBroadcastManager;

    protected RfCommManager(Context context) {
        this.socket = null;
        this.server = false;
        this.connected = false;

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    protected abstract InputStream getInputStream();
    protected abstract OutputStream getOutputStream();
    protected abstract void closeSocket();

    public boolean isSocketConnected() {
        return this.connected;
    }

    public void setSocket(final TypeRfSocket socket, boolean server) {
        this.socket = socket;
        this.server = server;
        iStream = getInputStream();
        oStream = getOutputStream();
        if ((iStream==null) || (oStream==null)) {
            stopSocket();
        }
        mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);

        connected = true;

        // reading Thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent;

                byte[][] buffer = new byte[4][512];
                int bytes;
                int bufferNumber = 0;

                // sometimes InputStream was re-filled before activity has time to attend previous handler message
                // In that situation buffer was corrupted by new InputStream before being read by activity.
                // queue of 4 buffers was used to prevent that problem
                intent = new Intent(STARTED);
                mLocalBroadcastManager.sendBroadcast(intent);

                while (true) {
                    try {
                        // Read from the InputStream
                        bytes = iStream.read(buffer[bufferNumber%4]);
                        if (bytes == -1) {
                            break;
                        }
                        // Send the obtained bytes to the UI Activity
                        intent = new Intent(MESSAGE);
                        intent.putExtra(message_content, new String(buffer[bufferNumber%4], 0, bytes));
                        mLocalBroadcastManager.sendBroadcast(intent);
                        bufferNumber++;
                    } catch (IOException e) {
                        Log.e(TAG,"End of socket");
                        intent = new Intent(STOPPED);
                        mLocalBroadcastManager.sendBroadcast(intent);
                        RfCommManager.this.socket = null;
                        RfCommManager.this.server = false;
                        RfCommManager.this.connected = false;
                        return;
                    }
                }
            }

        }).start();


        // writing Thread
        new Thread(new Runnable() {


            @Override
            public void run() {

                while (true) {
                    try {
                        String msg = mMessageQueue.take();
                        oStream.write(msg.getBytes(Charset.defaultCharset()));
                    } catch (InterruptedException ie) {
                        Log.e(TAG, "Message sending loop interrupted, exiting");
                        stopSocket();
                    } catch (IOException e) {
                        stopSocket();
                    }
                }
            }
        }).start();

    }

    public void write(String buffer) {
        try {
            mMessageQueue.put(buffer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void stopSocket() {
        if (socket!=null) {
            closeSocket();
            Intent intent = new Intent(CLOSED);
            mLocalBroadcastManager.sendBroadcast(intent);
        }
        socket = null;
        server = false;
        connected = false;
    }

}
