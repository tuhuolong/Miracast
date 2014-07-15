
package com.milink.milink;

import android.util.Log;

import com.milink.net.asio.tcp.server.TcpConn;
import com.milink.net.asio.tcp.server.TcpServer;
import com.milink.net.asio.tcp.server.TcpServerListener;

public class MiLinkServer implements TcpServerListener {

    private static final String TAG = MiLinkServer.class.getSimpleName();

    private TcpServer mServer = null;
    private MiLinkServerListener mListener = null;

    public MiLinkServer(MiLinkServerListener listener) {
        mServer = new TcpServer(0, this);
        mListener = listener;
    }

    public void start() {
        mServer.start();
    }

    public void stop() {
        mServer.stop();
    }

    public boolean send(String ip, int port, IQ iq) {
        boolean result = false;

        do
        {
            String msg = iq.toString();
            if (msg == null)
                break;

            TcpConn conn = mServer.getConnPool().getConn(ip, port);
            if (conn == null)
                break;

            result = mServer.send(conn, msg.getBytes());
        } while (false);

        return result;
    }

    @Override
    public void onAccept(TcpServer server, TcpConn conn) {
        Log.d(TAG, String.format("onAccept: %s:%d", conn.getIp(), conn.getPort()));
    }

    @Override
    public void onReceived(TcpServer server, TcpConn conn, byte[] data) {
        Log.d(TAG, String.format("onReceive: %s:%d", conn.getIp(), conn.getPort()));

        IQ iq = IQ.create(data);
        if (iq != null) {
            mListener.onRecvRequest(conn.getIp(), conn.getPort(), iq);
        }
    }

    @Override
    public void onConnectionClosed(TcpServer server, TcpConn conn) {
        Log.d(TAG, String.format("onConnectionClosed: %s:%d", conn.getIp(), conn.getPort()));
    }
}