
package com.milink.asio.tcp.client;

public class TcpPacket {

    public enum Type {
        Unknown,
        Exit,
        Normal,
    };

    public Type type = Type.Unknown;
    public byte[] data = null;
}
