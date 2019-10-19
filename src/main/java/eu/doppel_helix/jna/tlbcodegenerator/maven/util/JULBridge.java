/* Copyright (c) 2016 Matthias Bläsing, All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2
 * alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to
 * the project.
 *
 * You may obtain a copy of the LGPL License at:
 *
 * http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */

package eu.doppel_helix.jna.tlbcodegenerator.maven.util;

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
