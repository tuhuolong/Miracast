
package com.mi.milink.server;

import com.mi.milink.common.IQ;

public interface MiLinkServerListener {

    void onAccept(MiLinkServer server, String ip, int port);

    void onReceived(MiLinkServer server, String ip, int port, IQ iq);

    void onConnectionClosed(MiLinkServer server, String ip, int port);
}