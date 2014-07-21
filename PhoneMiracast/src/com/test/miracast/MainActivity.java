
package com.test.miracast;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.milink.bonjour.Bonjour;
import com.milink.bonjour.BonjourListener;
import com.milink.milink.client.MiLinkClient;
import com.milink.milink.client.MiLinkClientListener;
import com.milink.milink.common.IQ;
import com.milink.miracast.ScreenMirroring;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends Activity implements MiLinkClientListener, BonjourListener {

    private static final String MILINK = "_milink._tcp.local.";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static MiLinkClient mClient = null;
    private static Bonjour mBonjour = null;
    private ArrayList<Device> mDevices = new ArrayList<Device>();

    private class Device {
        public String ip;
        public int port;
        public String name;
        public String type;
    }

    private int mDeviceCurrentPosition = 0;
    private int mActionId = 0;
    private boolean mMiracast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        mBonjour = new Bonjour(this, this);
        mBonjour.start();
        mBonjour.discoveryService(MILINK);

        mClient = new MiLinkClient(this);

        Device device = new Device();
        device.ip = "127.0.0.1";
        device.type = "_milink._tcp";
        device.name = "我的手机";
        device.port = 0;
        mDevices.add(device);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi = menu.add(R.string.push);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        mi.setIcon(android.R.drawable.ic_menu_share);

        return true;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        mBonjour.stop();
        mClient.disconnect();
        super.onDestroy();
    }

    private void startMiracast(String ip, int port) {
        if (!mMiracast) {
            mMiracast = true;
            mClient.connect(ip, port, 1000 * 5);
        }
    }

    private void stopMiracast() {
        if (mMiracast) {
            mMiracast = false;

            String param = "<root/>";
            IQ iq = new IQ(IQ.Type.Set,
                    mActionId++,
                    com.milink.milink.contants.Xmlns.MIRACAST,
                    com.milink.milink.contants.miracast.Actions.STOP,
                    param.getBytes());

            mClient.send(iq);
            mClient.disconnect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        synchronized (mDevices) {
            final ArrayList<Device> deviceList = new ArrayList<Device>();
            for (Device device : mDevices) {
                deviceList.add(device);
            }

            final ArrayList<String> names = new ArrayList<String>();
            for (Device device : mDevices) {
                names.add(device.name);
            }

            String[] deviceNames = new String[names.size()];
            names.toArray(deviceNames);

            new AlertDialog.Builder(this).setTitle("Device List").setItems(
                    deviceNames,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int pos) {
                            if (pos == 0) {
                                mDeviceCurrentPosition = 0;
                                stopMiracast();
                                getActionBar().setTitle("Stop Miracast");
                            } else if (pos != mDeviceCurrentPosition) {
                                if (mDeviceCurrentPosition != 0) {
                                    stopMiracast();
                                }

                                mDeviceCurrentPosition = pos;
                                Device d = deviceList.get(pos);
                                getActionBar().setTitle(
                                        String.format("Start Miracast to : %s", d.name));

                                startMiracast(d.ip, d.port);
                            } else {
                                mDeviceCurrentPosition = pos;
                                Device d = deviceList.get(pos);
                                getActionBar().setTitle(
                                        String.format("Start Miracast to : %s", d.name));

                                startMiracast(d.ip, d.port);
                            }
                        }

                    })
                    .create().show();
        }

        return true;
    }

    @Override
    public void onConnected(MiLinkClient client) {
        Log.d(TAG, "onConnected");

        String ip = client.getSelfIp();
        int port = 9999;
        int ret = ScreenMirroring.getInstance().start(ip, port);
        Log.d(TAG, String.format("ScreenMirroring.start: %d", ret));
        
        String param = String.format("<root><ip>%s</ip><port>%d</port></root>", ip, port);

        IQ iq = new IQ(IQ.Type.Set,
                mActionId++,
                com.milink.milink.contants.Xmlns.MIRACAST,
                com.milink.milink.contants.miracast.Actions.START,
                param.getBytes());

        mClient.send(iq);
    }

    @Override
    public void onConnectedFailed(MiLinkClient client) {
        Log.d(TAG, "onConnectedFailed");

        //getActionBar().setTitle("Start Miracast to : %s failed!");
        
        int ret = ScreenMirroring.getInstance().stop();
        Log.d(TAG, String.format("ScreenMirroring.start: %d", ret));
    }

    @Override
    public void onDisconnect(MiLinkClient client) {
        Log.d(TAG, "onDisconnect");
    }

    @Override
    public void onReceived(MiLinkClient client, IQ iq) {
        Log.d(TAG, "onReceived");
        Log.d(TAG, iq.toString());
    }

    @Override
    public void onEvent(MiLinkClient client, IQ iq) {
        Log.d(TAG, "onEvent");
        
        if (iq.getType() != IQ.Type.Event)
            return;
        
        if (! iq.getXmlns().equalsIgnoreCase(com.milink.milink.contants.Xmlns.MIRACAST))
            return;
        
        if (iq.getEvent().equalsIgnoreCase(com.milink.milink.contants.miracast.Events.STOPPED)) {
            Log.d(TAG, "TV stopped!");
            ScreenMirroring.getInstance().stop();
            return;
        }
    }

    @Override
    public void onServiceFound(String name, String type, String ip, int port,
            Map<String, String> properties) {
        Log.d(TAG, String.format("onServiceFound: %s %s %s:%d", name, type, ip, port));

        Device device = new Device();
        device.ip = ip;
        device.type = type;
        device.name = name;
        device.port = port;

        mDevices.add(device);
    }

    @Override
    public void onServiceLost(String name, String type, String ip) {
        Log.d(TAG, String.format("onServiceLost: %s %s %s:%d", name, type, ip));

        for (Device device: mDevices) {
            if (device.ip.equalsIgnoreCase(ip)) {
                mDevices.remove(device);
                break;
            }
        }
    }
}
