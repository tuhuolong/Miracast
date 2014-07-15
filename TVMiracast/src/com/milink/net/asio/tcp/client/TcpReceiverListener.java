
package com.milink.net.asio.tcp.client;

public interface TcpReceiverListener {

    void didDisconnect(TcpReceiver receiver);

    void didRecvBytes(TcpReceiver receiver, byte[] data);
}
