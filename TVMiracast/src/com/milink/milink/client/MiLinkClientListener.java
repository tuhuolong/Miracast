
package com.milink.milink.client;

import com.milink.milink.common.IQ;

public interface MiLinkClientListener {

    void onConnected();

    void onConnectedFailed();

    void onDisconnect();

    void onReceived(IQ iq);
}
