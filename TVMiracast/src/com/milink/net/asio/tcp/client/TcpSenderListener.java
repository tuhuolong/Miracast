
package com.milink.net.asio.tcp.client;

public interface TcpSenderListener {

    void didSendBytes(TcpSender sender, int sendBytes);
}
