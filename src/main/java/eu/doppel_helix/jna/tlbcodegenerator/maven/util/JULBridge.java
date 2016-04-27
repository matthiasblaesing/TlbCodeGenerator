package eu.doppel_helix.jna.tlbcodegenerator.maven.util;

import edu.emory.mathcs.backport.java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.maven.plugin.logging.Log;

public class JULBridge extends Handler {
    
    private final Log mavenLogger;

    private final Formatter formatter = new SimpleFormatter();

    private final Level originalLevel;
    private final List<Handler> originalHandlers = new LinkedList<>();
   
    
    public JULBridge(Log mavenLogger) {
        this.mavenLogger = mavenLogger;
        
        Logger rootLogger = Logger.getLogger("");
        originalLevel = rootLogger.getLevel();
        for(Handler h: rootLogger.getHandlers()) {
            originalHandlers.add(h);
            rootLogger.removeHandler(h);
        }
        
        if(mavenLogger.isDebugEnabled()) {
            rootLogger.setLevel(Level.FINE);
        }
        rootLogger.addHandler(this);
    }
    
    public void restore() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.removeHandler(this);
        
        for(Handler h: originalHandlers) {
            rootLogger.addHandler(h);
        }
        
        rootLogger.setLevel(originalLevel);
    }
    
    @Override
    public void publish(LogRecord record) {
        String message = formatter.formatMessage(record);
        int level = record.getLevel().intValue();
        if (level >= Level.SEVERE.intValue()) {
            if (record.getThrown() != null) {
                mavenLogger.error(message, record.getThrown());
            } else {
                mavenLogger.error(message);
            }
        } else if (level >= Level.WARNING.intValue()) {
            if (record.getThrown() != null) {
                mavenLogger.warn(message, record.getThrown());
            } else {
                mavenLogger.warn(message);
            }
        } else if (level >= Level.INFO.intValue()) {
            if (record.getThrown() != null) {
                mavenLogger.info(message, record.getThrown());
            } else {
                mavenLogger.info(message);
            }
        } else if (record.getThrown() != null) {
            mavenLogger.debug(message, record.getThrown());
        } else {
            mavenLogger.debug(message);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

}
