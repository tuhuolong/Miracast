
package com.milink.asio.tcp.client;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TcpSender implements Runnable {

    private SocketChannel mChannel = null;
    private TcpSenderListener mListener = null;
    private Thread mThread = null;
    private BlockingQueue<TcpPacket> mQueue = null;

    public TcpSender(SocketChannel channel, TcpSenderListener listener) {
        mChannel = channel;
        mListener = listener;
        mQueue = new ArrayBlockingQueue<TcpPacket>(128);
        mThread = new Thread(this);
        mThread.start();
    }

    public void close() {
        synchronized(this) {
            TcpPacket packet = new TcpPacket();
            packet.type = TcpPacket.Type.Exit;

            synchronized (this) {
                try {
                    mQueue.clear();
                    mQueue.put(packet);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void push(byte[] data) {
        synchronized(this) {
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
    }

    @Override
    public void run() {
        while (true) {
            // get packet
            TcpPacket packet = null;
            try {
                packet = mQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            if (packet.type == TcpPacket.Type.Exit)
                break;

            // send packet
            ByteBuffer buffer = ByteBuffer.wrap(packet.data);
            buffer.clear();

            int writeSize = 0;
            while (true) {
                int size = 0;
                try {
                    size = mChannel.write(buffer);
                } catch (IOException e) {
                    break;
                }

                writeSize += size;
                if (writeSize < packet.data.length) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                else {
                    break;
                }
            }

            mListener.didSendBytes(this, packet.data.length);
        }

        mQueue.clear();
    }
}
