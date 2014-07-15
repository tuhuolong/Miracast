
package com.milink.asio.tcp.server;

import java.nio.channels.SocketChannel;

public class TcpConn {

    private SocketChannel mChannel = null;

    public TcpConn(SocketChannel channel) {
        mChannel = channel;
    }

    public SocketChannel getChannel() {
        return mChannel;
    }
}