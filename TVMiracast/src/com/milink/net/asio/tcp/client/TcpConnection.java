
package com.milink.net.asio.tcp.client;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class TcpConnection implements TcpSenderListener, TcpReceiverListener {
    private static final String TAG = TcpConnection.class.getSimpleName();
    private static final Integer SLEEP_INTERVAL = 100; // milliseconds;

    private TcpConnectionListener mListener = null;
    private SocketChannel mChannel = null;
    private Thread mConnThread = null;

    private String mIp = null;
    private int mPort = 0;
    private Integer mTimeout = 0;

    private TcpSender mSender = null;
    private TcpReceiver mReceiver = null;

    public TcpConnection(String ip, int port, int timeout, TcpConnectionListener listener) {
        if (mChannel != null) {
            this.disconnect();
            mChannel = null;
        }

        try {
            mChannel = SocketChannel.open();
            mChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mIp = ip;
        mPort = port;
        mTimeout = timeout;
        mListener = listener;

        mConnThread = new Thread(new AsyncConnect());
        mConnThread.start();
    }

    public void disconnect() {
        try {

            if (mChannel != null) {

                if (mChannel.isConnected())
                {
                    mChannel.close();
                    mChannel = null;

                    if (mSender != null) {
                        mSender.close();
                        mSender = null;
                    }

                    if (mReceiver != null) {
                        mReceiver.close();
                        mReceiver = null;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean asyncSend(byte[] data) {
        if (!mChannel.isConnected())
            return false;

        if (mSender != null)
            mSender.push(data);

        return true;
    }

    public String selfIp() {
        return mChannel.socket().getLocalAddress().getHostAddress().toString();
    }

    public String peerIp() {
        return mIp;
    }

    public int peerPort() {
        return mPort;
    }

    private class AsyncConnect implements Runnable {

        @Override
        public void run() {
            try {
                mChannel.connect(new InetSocketAddress(mIp, mPort));

                int count = 0;
                while (!mChannel.finishConnect()) {
                    count += SLEEP_INTERVAL;
                    if (count > mTimeout) {
                        Log.i(TAG, String.format("connect %s:%d failed", mIp, mPort));

                        mListener.didConnectedFailed(TcpConnection.this);
                        return;
                    }
                    else
                    {
                        Log.i(TAG, String.format("waiting for connection establish (%s:%d)", mIp,
                                mPort));
                    }

                    Thread.sleep(SLEEP_INTERVAL);
                }

                Log.i(TAG, String.format("connect %s:%d OK", mIp, mPort));

                // start receiver & sender
                mReceiver = new TcpReceiver(mChannel, TcpConnection.this);
                mSender = new TcpSender(mChannel, TcpConnection.this);

                mListener.didConnected(TcpConnection.this);
            } catch (IOException e) {
                e.printStackTrace();
                mListener.didConnectedFailed(TcpConnection.this);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                mListener.didConnectedFailed(TcpConnection.this);
            } catch (InterruptedException e) {
                e.printStackTrace();
                mListener.didConnectedFailed(TcpConnection.this);
            }
        }
    }

    @Override
    public void didSendBytes(TcpSender sender, int sendBytes) {
        mListener.didSendBytes(this, sendBytes);
    }

    @Override
    public void didRecvBytes(TcpReceiver receiver, byte[] data) {
        mListener.didRecvBytes(this, data);
    }

    @Override
    public void didDisconnect(TcpReceiver receiver) {
        mListener.didDisconnect(this);

        if (mReceiver != null) {
            mReceiver.close();
            mReceiver = null;
        }

        if (mSender != null) {
            mSender.close();
            mSender = null;
        }
    }
}
