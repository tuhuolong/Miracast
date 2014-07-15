
package com.ouyang.test;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.milink.milink.common.IQ;
import com.milink.milink.server.MiLinkServer;
import com.milink.milink.server.MiLinkServerListener;

public class MainActivity extends Activity implements MiLinkServerListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private MiLinkServer mMilinkServer = new MiLinkServer(this);

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

    @Override
    public void onRecvRequest(String ip, int port, IQ iq) {
        Log.d(TAG, String.format("recv: %s", iq.toString()));
    }
}
