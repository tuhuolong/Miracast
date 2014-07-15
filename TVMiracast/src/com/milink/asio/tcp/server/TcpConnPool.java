
package com.milink.asio.tcp.server;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class TcpConnPool {

    private ArrayList<TcpConn> mConns = new ArrayList<TcpConn>();

    public ArrayList<TcpConn> getConns() {
        return mConns;
    }

    public void add(TcpConn conn) {
        mConns.add(conn);
    }
    
    public void remove(TcpConn conn) {
        mConns.remove(conn);
    }

    public TcpConn getConn(SocketChannel channel) {
        for (TcpConn conn : mConns) {
            if (conn.getChannel().equals(channel))
                return conn;
        }

        return null;
    }
}