package me.edgeconsult.keys;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by yun on 10/14/17.
 */

public class WebSocketClient {

    private static final String WEB_SOCKET_CLIENT = WebSocketClient.class.getSimpleName();

    private static OkHttpClient client;
    private static WebSocket webSocket;

    static WebSocket getInstance(String url, WebSocketListener webSocketListener) {
        if (client == null) {
            client = new OkHttpClient();
            webSocket = client.newWebSocket(new Request.Builder().url(url).build(), webSocketListener);
            client.dispatcher().executorService().shutdown();
        }
        return webSocket;
    }

    static void closeWebSocket() {
        if (client != null) {
            webSocket.close(1000, "closing");
            client = null;
        }
    }
}
