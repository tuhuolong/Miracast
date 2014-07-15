
package com.milink.net.asio.tcp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TcpClient {

    private static final String TAG = TcpClient.class.getSimpleName();
    private TcpClientListener mListener = null;
    private boolean mIsConnected = false;
    private SocketChannel mChannel = null;
    private SelectWorker mSelectWorker = null;
    private RecvWorker mRecvWorker = null;
    private SendWorker mSendWorker = null;

    public TcpClient(TcpClientListener listener) {
        mListener = listener;

        try {
            mChannel = SocketChannel.open();
            mChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(String ip, int port, int millisecond) {
        if (! mIsConnected) {
            mRecvWorker = new RecvWorker();
            mSendWorker = new SendWorker();
            mSelectWorker = new SelectWorker(ip, port, millisecond);
        }
    }

    public void disconnect() {
        if (mIsConnected) {
            mSelectWorker.close();
            mSendWorker.close();
            mRecvWorker.close();
        }
    }

    public boolean isConnected() {
        synchronized (this) {
            return mIsConnected;
        }
    }
    
    public class SelectWorker implements Runnable {
        private static final int DEFAULT_CONNECT_TIMEOUT = 1000 * 5;
        private Selector mSelector = null;
        private Thread mThread = null;
        private String mIp = null;
        private int mPort = 0;
        private int mTimeout = 0;
        private Boolean mLoop = true;
        
        public SelectWorker(String ip, int port, int timeout) {
            mIp = ip;
            mPort = port;
            mTimeout = (timeout > 0) ? timeout : DEFAULT_CONNECT_TIMEOUT;
            
            mThread = new Thread(this);
            mThread.start();
        }

        public void close() {
            try {
                mSelector.close();
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                mSelector = Selector.open();
            } catch (IOException e1) {
                e1.printStackTrace();
                return;
            }

            try {
                mChannel.register(mSelector, SelectionKey.OP_CONNECT);
                mChannel.connect(new InetSocketAddress(mIp, mPort));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                mSelector.select(mTimeout);
            } catch (IOException e) {
                mListener.onConnectedFailed(TcpClient.this);
                return;
            }

            mListener.onConnected(TcpClient.this);
            
            mLoop = true;
            while (mLoop) {

                try {
                    mSelector.select();
                } catch (IOException e) {
                    break;
                }

                try {
                    Set<SelectionKey> readyKeys = mSelector.selectedKeys();
                    Iterator<SelectionKey> iter = readyKeys.iterator();

                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        postSelect(key);
                    }
                } catch (ClosedSelectorException e) {
                    break;
                }
            }

            mListener.onConnectedFailed(TcpClient.this);
        }

        private void postSelect(SelectionKey key) {
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

                    mRecvWorker.putData(data);

                    buf.clear();
                }
                else {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mLoop = false;
                }
            }
        }
    }

    public class RecvWorker implements Runnable {

        private static final int MAX_RECV_QUEUE_LENGTH = 128;
        private BlockingQueue<TcpPacket> mQueue = null;
        private Thread mThread = null;

        public RecvWorker() {
            mQueue = new ArrayBlockingQueue<TcpPacket>(MAX_RECV_QUEUE_LENGTH);
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

            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void putData(byte[] data) {
            TcpPacket packet = new TcpPacket();
            packet.type = TcpPacket.Type.Receive;
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

                if (packet.type == TcpPacket.Type.Exit) {
                    break;
                }

                else if (packet.type == TcpPacket.Type.Receive) {
                    mListener.onReceived(TcpClient.this, packet.data);
                }
            }

            mQueue.clear();
        }
    }

    public class SendWorker implements Runnable {

        private static final int MAX_SEND_QUEUE_LENGTH = 128;
        private BlockingQueue<TcpPacket> mQueue = null;
        private Thread mThread = null;

        public SendWorker() {
            mQueue = new ArrayBlockingQueue<TcpPacket>(MAX_SEND_QUEUE_LENGTH);
            mThread = new Thread(this);
            mThread.start();
        }

        public void close() {
            synchronized (this) {
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

            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void putData(byte[] data) {
            synchronized (this) {
                TcpPacket packet = new TcpPacket();
                packet.type = TcpPacket.Type.Send;
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
                TcpPacket packet = null;

                try {
                    packet = mQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                if (packet.type == TcpPacket.Type.Exit) {
                    break;
                }
                else if (packet.type == TcpPacket.Type.Send) {
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
                }
            }

            mQueue.clear();
        }
    }
}