
package com.milink.milink.client;

import android.util.Log;

import com.milink.milink.common.IQ;
import com.milink.net.asio.tcp.client.TcpClient;
import com.milink.net.asio.tcp.client.TcpClientListener;

public class MiLinkClient implements TcpClientListener {

    private static final String TAG = MiLinkClient.class.getSimpleName();

    private TcpClient mClient = null;
    private MiLinkClientListener mListener = null;

    public MiLinkClient(MiLinkClientListener listener) {
        mClient = new TcpClient(this);
        mListener = listener;
    }

    public void connect(String ip, int port, int millisecond) {
        mClient.connect(ip, port, millisecond);
    }

    public void disconnect() {
        mClient.disconnect();
    }

    public boolean isConnected() {
        return mClient.isConnected();
    }

    public boolean send(IQ iq) {
        boolean result = false;

        do
        {
            String msg = iq.toString();
            if (msg == null)
                break;

            result = mClient.send(msg.getBytes());
        } while (false);

        return result;
    }

    @Override
    public void onConnected(TcpClient client) {
        Log.d(TAG, String.format("onConnected: %s:%d", client.getPeerIp(), client.getPeerPort()));
        mListener.onConnected();
    }

    @Override
    public void onConnectedFailed(TcpClient client) {
        Log.d(TAG, String.format("onConnectedFailed: %s:%d", client.getPeerIp(), client.getPeerPort()));
        mListener.onConnectedFailed();
    }

    @Override
    public void onDisconnect(TcpClient client) {
        Log.d(TAG, String.format("onDisconnect: %s:%d", client.getPeerIp(), client.getPeerPort()));
        mListener.onDisconnect();
    }

    @Override
    public void onReceived(TcpClient client, byte[] data) {
        Log.d(TAG, String.format("onReceived: %s:%d", client.getPeerIp(), client.getPeerPort()));

        IQ iq = IQ.create(data);
        if (iq != null) {
            mListener.onReceived(iq);
        }
    }
}