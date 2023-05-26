package org.geworkbenchweb.plugins.marina;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.logging.Log;

/**
 * based on StreamGobbler by David Ervin and Shannon Hastings
 * Reads input from a stream as long as more data is available
 */
public class StreamGobbler extends Thread {

    public static final String TYPE_OUT = "OUT";
    public static final String TYPE_ERR = "ERR";

    private InputStream gobble;
    private String type;
    private PrintStream redirect;
    private Log log;
    private LogPriority priority;
    
    /**
     * Creates a stream gobbler which will just read the input stream until it's gone
     * 
     * @param is
     * @param type
     */
    public StreamGobbler(InputStream is, String type) {
        this(is, type, null);
    }
    

    /**
     * Creates a stream gobbler to consume an input stream and redirect its 
     * contents to an output stream
     * 
     * @param is
     * @param type
     * @param redrirect
     */
    public StreamGobbler(InputStream is, String type, OutputStream redrirect) {
        this.gobble = is;
        this.type = type;
        if (redrirect != null) {
            this.redirect = new PrintStream(redrirect);
        }
    }
  
    /**
     * Creates a stream gobbler to consume an input stream and redirect
     * its contents to a Log with the specified priority level
     * 
     * @param is
     * @param type
     * @param log
     * @param priority
     */
    public StreamGobbler(InputStream is, String type, Log log, LogPriority priority) {
        this.gobble = is;
        this.type = type;
        this.log = log;
        this.priority = priority;
    }


    /**
     * creates readers to handle the text created by the external program
     */
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(gobble);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (redirect != null || (log != null && priority != null)) {
                    line = type + "> " + line;
                    if (redirect != null) {
                        redirect.println(line);
                    } else {
                        switch (priority) {
                            case INFO:
                                log.info(line);
                                break;
                            case DEBUG:
                                log.debug(line);
                                break;
                            case WARN:
                                log.warn(line);
                                break;
                            case ERROR:
                                log.error(line);
                                break;
                            case FATAL:
                            	log.error("[FATAL]"+line);
                            	break;
                            default:
                            	log.error("[default]"+line);
                            	break;
                        }
                    }
                }
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    
    /**
      *  LogPriority
      *  Priority with which to log output from the stream gobbler
      * 
      * @author David Ervin
      * 
      * @created Apr 17, 2008 9:29:33 AM
      * @version $Id: StreamGobbler.java,v 1.5 2008-04-17 14:55:06 dervin Exp $
     */
    public static enum LogPriority {
        INFO, DEBUG, WARN, ERROR, FATAL
    }
}
