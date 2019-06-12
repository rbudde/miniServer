package de.budde.jetty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownHook extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(ShutdownHook.class);
    private final boolean doLog;
    private final String uri;

    public ShutdownHook(boolean doLog, String uri) {
        this.doLog = doLog;
        this.uri = uri;
    }

    @Override
    public void run() {
        // to avoid sonar complaints. Note: the parameter construction doesn't degrade performance, if it is in a shutdown hook :-)
        if ( this.doLog && LOG.isInfoEnabled() ) {
            LOG.info("Server shutdown at \"" + this.uri + "\". See you later.");
        }
    }
}