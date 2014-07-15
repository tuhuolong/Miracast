
package com.milink.net.asio.tcp.oldclient;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TcpReceiver implements Runnable {

    private TcpReceiverListener mListener = null;
    private Thread mSelectorThread = null;
    private Selector mSelector = null;
    private boolean mLoop = false;
    private Receiver mReceiver = null;

    public TcpReceiver(SocketChannel channel, TcpReceiverListener listener) {
        try {
            mSelector = Selector.open();
            channel.register(mSelector, SelectionKey.OP_READ);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        mListener = listener;
        mReceiver = new Receiver();

        mSelectorThread = new Thread(this);
        mSelectorThread.start();
    }

    public void close() {
        mSelector.wakeup();
    }

    @Override
    public void run() {
        mLoop = true;
        while (mLoop) {

            try {
                mSelector.select();
            } catch (IOException e) {
                break;
            }

            Iterator<SelectionKey> it = mSelector.selectedKeys().iterator();
            if (!it.hasNext())
                break;

            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                processSelectionKey(key);
            }
        }

        mReceiver.close();
        mListener.didDisconnect(this);
    }

    private void processSelectionKey(SelectionKey key) {

        // read data
        if (key.isValid() && key.isReadable()) {
            SocketChannel channel = (SocketChannel) key.channel();

            ByteBuffer buf = ByteBuffer.allocateDirect(1024);
            int numBytesRead = 0;
            try {
                numBytesRead = channel.read(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (numBytesRead > 0) {
                buf.flip();

                byte[] data = new byte[numBytesRead];
                buf.get(data, 0, numBytesRead);
                mReceiver.push(data);
                buf.clear();
            }
            else {
                mLoop = false;
            }
        }
    }

    public class Receiver implements Runnable {

        private Thread mThread = null;
        private BlockingQueue<TcpPacket> mQueue = null;

        public Receiver() {
            mQueue = new ArrayBlockingQueue<TcpPacket>(128);
            mThread = new Thread(this);
            mThread.start();
        }

        public void close() {
            TcpPacket packet = new TcpPacket();
            packet.type = TcpPacket.Type.Exit;

            synchronized (this) {
                mQueue.clear();
                try {
                    mQueue.put(packet);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void push(byte[] data) {
            TcpPacket packet = new TcpPacket();
            packet.type = TcpPacket.Type.Normal;
            packet.data = data.clone();

            synchronized (this) {
                try {
                    mQueue.put(packet);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            while (true) {
                TcpPacket packet;
                try {
                    packet = mQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                if (packet.type == TcpPacket.Type.Exit)
                    break;

                mListener.didRecvBytes(TcpReceiver.this, packet.data);
            }

            mQueue.clear();
        }
    }
}
