
package com.milink.milink;

public interface MiLinkServerListener {

    void onRecvRequest(String ip, int port, IQ iq);
}