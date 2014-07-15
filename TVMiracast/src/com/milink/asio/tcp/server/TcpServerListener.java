package com.milink.asio.tcp.server;

public interface TcpServerListener {

    void onAccept(TcpServer server, TcpConn conn);
    
    void onReceive(TcpServer server, TcpConn conn, byte[] data);
    
    void onConnectionClosed(TcpServer server, TcpConn conn);
}