
package com.milink.net.asio.tcp.oldclient;

public class TcpPacket {

    public enum Type {
        Unknown,
        Exit,
        Normal,
    };

    public Type type = Type.Unknown;
    public byte[] data = null;
}
