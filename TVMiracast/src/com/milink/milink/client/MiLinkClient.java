
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

    public boolean connect(String ip, int port, int millisecond) {
        return mClient.connect(ip, port, millisecond);
    }

    public boolean disconnect() {
        return mClient.disconnect();
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
        mListener.onConnected();
    }

    @Override
    public void onConnectedFailed(TcpClient client) {
        mListener.onConnectedFailed();
    }

    @Override
    public void onDisconnect(TcpClient client) {
        mListener.onDisconnect();
    }

    @Override
    public void onReceived(TcpClient client, byte[] data) {
        Log.d("Recv", new String(data));

        IQ iq = IQ.create(data);
        if (iq != null) {
            mListener.onReceived(iq);
        }
    }
}