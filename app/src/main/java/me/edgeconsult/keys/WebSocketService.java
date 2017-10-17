package me.edgeconsult.keys;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import org.java_websocket.client.*;
import org.java_websocket.handshake.ServerHandshake;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by yun on 10/15/17.
 */

public class WebSocketService extends Service {

    private final IBinder mBinder = new MyBinder();

    private final ArrayList<MyListener> mListeners
            = new ArrayList<MyListener>();
    private final Handler mHandler = new Handler();

    private String targetURL = "wss://owncloudhk.net/app";
    private org.java_websocket.client.WebSocketClient mWebSocketClient;

    private boolean connected = false;

    @Override
    public void onCreate() {
        Log.i(WebSocketService.class.getSimpleName(), "onCreate");
    }

    @Override
    public void onDestroy() {
        Log.i(WebSocketService.class.getSimpleName(), "onDestroy");
    }

    public void connect(String token) {
        URI uri;
        try {
            uri = new URI(targetURL + "?access_token=" + token);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        mWebSocketClient = new org.java_websocket.client.WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("{\"type\":\"custom\",\"data\":\"Hello from " +
                        Build.MANUFACTURER + " " + Build.MODEL + "\"}");
            }

            @Override
            public void onMessage(String s) {
                Log.i("Websocket", "Message " + s);
                for (int i=mListeners.size()-1; i>=0; i--) {
                    Log.i("Websocket", String.valueOf(i));
                    mListeners.get(i).onMessage(s);
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
                connected = false;
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        mWebSocketClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
        mWebSocketClient.connect();
        connected = true;
    }

    public boolean connected() {
        return connected;
    }

    public void send(String text) {
        if (connected)
            mWebSocketClient.send(text);
    }

    public void registerListener(MyListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(MyListener listener) {
        mListeners.remove(listener);
    }

    public class MyBinder extends Binder {
        WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}