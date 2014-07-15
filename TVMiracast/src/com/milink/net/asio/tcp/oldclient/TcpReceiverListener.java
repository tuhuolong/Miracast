
package com.milink.net.asio.tcp.oldclient;

public interface TcpReceiverListener {

    void didDisconnect(TcpReceiver receiver);

    void didRecvBytes(TcpReceiver receiver, byte[] data);
}
