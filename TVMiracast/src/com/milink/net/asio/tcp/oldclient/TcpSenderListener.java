
package com.milink.net.asio.tcp.oldclient;

public interface TcpSenderListener {

    void didSendBytes(TcpSender sender, int sendBytes);
}
