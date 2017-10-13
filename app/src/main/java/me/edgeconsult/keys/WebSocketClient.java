package me.edgeconsult.keys;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by yun on 10/14/17.
 */

public class WebSocketClient {

    private static OkHttpClient client = null;
    private static WebSocket webSocket;

    static WebSocket getInstance(String url, WebSocketListener webSocketListener) {
        if (client == null) {
            Request request = new Request.Builder().url(url).build();
            webSocket = client.newWebSocket(request, webSocketListener);
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
