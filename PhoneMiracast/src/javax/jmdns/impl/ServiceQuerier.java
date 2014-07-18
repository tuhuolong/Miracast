package javax.jmdns.impl;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class ServiceQuerier {
    private static Logger logger = Logger.getLogger(ServiceQuerier.class.getName());
    private static final int QUERY_INTERVAL = 30 * 1000;
    private final String _type;
    private final JmDNSImpl _jmDNSImpl;
    private final QuerierTimerTask _task;
    private final Timer _timer;

    public ServiceQuerier(JmDNSImpl jmdns, String type) {
        this._type = type;
        this._jmDNSImpl = jmdns;
        this._timer = new Timer();
        this._task = new QuerierTimerTask();
    }

    public void start() {
        logger.info("start query:"+_type);
        _timer.schedule(_task, QUERY_INTERVAL, QUERY_INTERVAL);
    }

    public void stop() {
        logger.info("stop query:"+_type);
        _task.cancel();
        _timer.cancel();
        _timer.purge();
    }

    class QuerierTimerTask extends TimerTask {

        @Override
        public void run() {
            startServiceResolver();
        }

        public void startServiceResolver() {
            logger.info("to start query:"+_type);
            DNSTaskStarter.Factory.getInstance().getStarter(_jmDNSImpl).startServiceResolver(_type);
        }
    }
}
