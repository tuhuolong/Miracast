
package com.mi.bonjour;

import java.util.Map;

public interface BonjourListener {

    void onServiceFound(
            String name, 
            String type, 
            String ip, 
            int port,
            Map<String, String> properties);

    void onServiceLost(String name, String type, String ip);
}
