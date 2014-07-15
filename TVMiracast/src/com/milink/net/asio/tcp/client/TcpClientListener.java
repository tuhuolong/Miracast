
package com.milink.net.asio.tcp.client;

public interface TcpClientListener {

    void didConnected(TcpClient client);

    void didConnectedFailed(TcpClient client);

    void didDisconnect(TcpClient client);

    void didRecvBytes(TcpClient client, byte[] data);

    void didSendBytes(TcpClient client, int size);

    void didSendFailed(TcpClient client);
}
