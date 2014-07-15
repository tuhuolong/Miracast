
package com.milink.milink.server;

import com.milink.milink.common.IQ;

public interface MiLinkServerListener {

    void onReceived(String ip, int port, IQ iq);
}