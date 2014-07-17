
package com.ouyang.test;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.milink.milink.common.IQ;
import com.milink.milink.common.IQ.Type;
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
    
    public void onConnect(View button) {
        String ip = "10.0.10.142";
        int port = 8080;
        int timeout = 1000 * 5;

        mMiLinkClient.connect(ip, port, timeout);
    }
    
    public void onDisconnect(View button) {
        mMiLinkClient.disconnect();
    }

    public void onSend(View button) {

        IQ iq = new IQ(IQ.Type.Set,
                "1",
                com.milink.milink.common.Contants.XMLNS_MIRACAST,
                "start",
                "<root><ip>10.0.10.108</ip><port>1234</port></root>");

        mMiLinkClient.send(iq);
    }

    @Override
    public void onReceived(String ip, int port, IQ iq) {
        Log.d(TAG, String.format("recv from: %s:%d : %s", ip, port, iq.toString()));
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectedFailed() {
        Log.d(TAG, "onConnectedFailed");
    }

    @Override
    public void onDisconnect() {
        Log.d(TAG, "onDisconnect");
    }

    @Override
    public void onReceived(IQ iq) {
        Log.d(TAG, String.format("recv: %s", iq.toString()));
    }
}
