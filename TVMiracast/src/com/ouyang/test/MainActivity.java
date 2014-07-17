
package com.ouyang.test;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.milink.milink.common.IQ;
import com.milink.milink.server.MiLinkServer;
import com.milink.milink.server.MiLinkServerListener;
import com.milink.milink.client.MiLinkClient;
import com.milink.milink.client.MiLinkClientListener;

public class MainActivity extends Activity implements MiLinkServerListener, MiLinkClientListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private MiLinkServer mMilinkServer = new MiLinkServer(this);
    private MiLinkClient mMiLinkClient = new MiLinkClient(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onStart(View button) {
        mMilinkServer.start();
    }

    public void onStop(View button) {
        mMilinkServer.stop();
    }
    
    public void onPubEvent(View button) {
        String param = "<root><ip>10.0.10.108</ip><port>1234</port></root>"; 
        IQ iq = new IQ(IQ.Type.Event,
                "1",
                com.milink.milink.contants.Xmlns.MIRACAST,
                com.milink.milink.contants.miracast.Events.STOPPED,
                param.getBytes());
        
        mMilinkServer.publishEvent(iq);
    }

    public void onConnect(View button) {
//        String ip = "10.0.10.142";
//        int port = 8080;
        String ip = "10.0.10.136";
        int port = 9999;
        int timeout = 1000 * 5;

        mMiLinkClient.connect(ip, port, timeout);
    }
    
    public void onDisconnect(View button) {
        mMiLinkClient.disconnect();
    }

    public void onSend(View button) {

        String param = "<root><ip>10.0.10.108</ip><port>1234</port></root>"; 
        IQ iq = new IQ(IQ.Type.Set,
                "1",
                com.milink.milink.contants.Xmlns.MIRACAST,
                com.milink.milink.contants.miracast.Actions.START,
                param.getBytes());

        mMiLinkClient.send(iq);
    }

    @Override
    public void onAccept(MiLinkServer server, String ip, int port) {
        Log.d(TAG, String.format("onAccept from: %s:%d", ip, port));
    }

    @Override
    public void onReceived(MiLinkServer server, String ip, int port, IQ iq) {
        Log.d(TAG, String.format("onReceived from: %s:%d : %s", ip, port, iq.toString()));
    }

    @Override
    public void onConnectionClosed(MiLinkServer server, String ip, int port) {
        Log.d(TAG, String.format("onClosed from: %s:%d", ip, port));
    }

    @Override
    public void onConnected(MiLinkClient client) {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectedFailed(MiLinkClient client) {
        Log.d(TAG, "onConnectedFailed");
    }

    @Override
    public void onDisconnect(MiLinkClient client) {
        Log.d(TAG, "onDisconnect");
    }

    @Override
    public void onReceived(MiLinkClient client, IQ iq) {
        Log.d(TAG, String.format("onReceived: %s", iq.toString()));
    }

    @Override
    public void onEvent(MiLinkClient client, IQ iq) {
        Log.d(TAG, String.format("onEvent: %s", iq.toString()));
    }
}
