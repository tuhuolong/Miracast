
package com.milink.net.asio.tcp.client;

import android.util.Log;

public class TcpClient implements TcpConnectionListener {

    private static final String TAG = TcpClient.class.getSimpleName();
    private TcpClientListener mListener = null;
    private TcpConnection mConn = null;
    private boolean mIsConnected = false;

    public TcpClient(TcpClientListener listener) {
        mListener = listener;
    }

    @Override
    protected void finalize() throws Throwable {
        this.disconnect();
        super.finalize();
    }

    public void connect(String ip, int port, int millisecond) {
        synchronized (this) {
            if (mConn != null)
                mConn.disconnect();

            mConn = new TcpConnection(ip, port, millisecond, this);
        }
    }

    public void disconnect() {
        synchronized (this) {
            if (mConn != null) {
                mConn.disconnect();
                mConn = null;
            }
        }
    }

    public boolean isConnected() {
        synchronized (this) {
            return mIsConnected;
        }
    }

    public String selfIp() {
        synchronized (this) {
            if (mConn != null)
                return mConn.selfIp();

            return null;
        }
    }

    public String peerIp() {
        synchronized (this) {
            if (mConn != null)
                return mConn.peerIp();

            return null;
        }
    }

    public int peerPort() {
        synchronized (this) {
            if (mConn != null)
                return mConn.peerPort();

            return 0;
        }
    }

    public boolean send(byte[] data) {
        synchronized (this) {
            if (mConn == null)
                return false;

            if (data == null)
                return false;

            return mConn.asyncSend(data);
        }
    }

    @Override
    public void didConnected(TcpConnection conn) {
        Log.v(TAG, "didConnected");

        synchronized (this) {
            mIsConnected = true;
        }

        mListener.didConnected(this);
    }

    @Override
    public void didConnectedFailed(TcpConnection conn) {
        Log.v(TAG, "didConnectedFailed");

        synchronized (this) {
            mIsConnected = false;
        }

        mListener.didConnectedFailed(this);
    }

    @Override
    public void didDisconnect(TcpConnection conn) {
        Log.v(TAG, String.format("didDisconnect: %s:%d", conn.peerIp(), conn.peerPort()));

        synchronized (this) {
            mIsConnected = false;
        }

        mListener.didDisconnect(this);
    }

    @Override
    public void didRecvBytes(TcpConnection conn, byte[] data) {
        mListener.didRecvBytes(this, data);
    }

    @Override
    public void didSendBytes(TcpConnection conn, int size) {
        mListener.didSendBytes(this, size);
    }

}
