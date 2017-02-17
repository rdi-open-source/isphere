package org.tn5250j;

import java.awt.*;
import javax.swing.*;
import java.util.*;

import org.tn5250j.tools.logging.*;
import org.tn5250j.tools.LangTool;
import org.tn5250j.gui.TN5250jSecurityAccessDialog;
import org.tn5250j.framework.common.*;

//import org.tn5250j.swing.JTerminal;

public class My5250App extends JApplet implements TN5250jConstants {
    boolean isStandalone = true;
    private SessionManager manager;

    private TN5250jLogger log;

    /** Get a parameter value */
    public String getParameter(String key, String def) {

        return isStandalone ? System.getProperty(key, def) : (getParameter(key) != null ? getParameter(key) : def);
    }

    /** Construct the applet */
    public My5250App() {

    }

    /** Initialize the applet */
    @Override
    public void init() {
        try {
            jbInit();
        } catch (Exception e) {
            if (log == null)
                System.out.println(e.getMessage());
            else
                log.warn("In constructor: ", e);
        }
    }

    /** Component initialization */
    private void jbInit() throws Exception {
        this.setSize(new Dimension(400, 300));

        if (isSpecified(ARG_LOCALE))
            LangTool.init(parseLocale(getParameter(ARG_LOCALE)));
        else
            LangTool.init();

        // Let's check some permissions
        try {
            System.getProperty(".java.policy");
        } catch (SecurityException e) {
            e.printStackTrace();
            TN5250jSecurityAccessDialog.showErrorMessage(e);
            return;
        }
        log = TN5250jLogFactory.getLogger(this.getClass());

        Properties sesProps = new Properties();
        log.info(" We have loaded a new one");

        // Start loading properties - Host must exist
        sesProps.put(SESSION_HOST, getParameter("host"));

        if (isSpecified(ARG_TN_ENHANCED)) {
            sesProps.put(SESSION_TN_ENHANCED, "1");
        }

        if (isSpecified(ARG_HOST_PORT)) {
            sesProps.put(SESSION_HOST_PORT, getParameter(ARG_HOST_PORT));
        }

        // if (isSpecified(ARG_FILENAME,args))
        // propFileName = getParm(ARG_FILENAME,args);

        if (isSpecified(ARG_CODE_PAGE)) {
            sesProps.put(SESSION_CODE_PAGE, getParameter(ARG_CODE_PAGE));
        }

        if (isSpecified(ARG_USE_GUI)) {
            sesProps.put(SESSION_USE_GUI, "1");
        }

        if (isSpecified(ARG_TERM_NAME_SYSTEM)) {
            sesProps.put(SESSION_TERM_NAME_SYSTEM, "1");
        }

        if (isSpecified(ARG_SCREEN_SIZE_132)) {
            sesProps.put(SESSION_SCREEN_SIZE, SCREEN_SIZE_27X132_STR);
        } else {
            sesProps.put(SESSION_SCREEN_SIZE, SCREEN_SIZE_24X80_STR);
        }

        // socks proxy host argument
        if (isSpecified(ARG_PROXY_HOST)) {
            sesProps.put(SESSION_PROXY_HOST, getParameter(ARG_PROXY_HOST));
        }

        // socks proxy port argument
        if (isSpecified(ARG_PROXY_PORT)) {
            sesProps.put(SESSION_PROXY_PORT, getParameter(ARG_PROXY_PORT));
        }

        // check if device name is specified
        if (isSpecified(ARG_DEVICE_NAME)) {
            sesProps.put(SESSION_DEVICE_NAME, getParameter(ARG_DEVICE_NAME));
        }

        // are we to use a ssl and if we are what type
        if (isSpecified(ARG_SSL_TYPE)) {
            sesProps.put(SESSION_SSL_TYPE, getParameter(ARG_SSL_TYPE));
        }

        loadSystemProperty("SESSION_CONNECT_USER");
        loadSystemProperty("SESSION_CONNECT_PASSWORD");
        loadSystemProperty("SESSION_CONNECT_PROGRAM");
        loadSystemProperty("SESSION_CONNECT_LIBRARY");
        loadSystemProperty("SESSION_CONNECT_MENU");

        manager = SessionManager.instance();
        final Session5250 s = manager.openSession(sesProps, "", "Test Applet");
        final SessionGUI gui = new SessionGUI(s);
        // final JTerminal jt = new JTerminal(s);

        this.getContentPane().add(gui);

        s.connect();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // jt.grabFocus();
                gui.grabFocus();
            }
        });

    }

    private void loadSystemProperty(String param) {

        if (isSpecified(param)) System.getProperties().put(param, getParameter(param));

    }

    /** Get Applet information */
    @Override
    public String getAppletInfo() {
        return "tn5250j - " + tn5250jRelease + tn5250jVersion + tn5250jSubVer + " - Jave tn5250 Client";
    }

    /** Get parameter info */
    @Override
    public String[][] getParameterInfo() {
        return null;
    }

    /**
     * Tests if a parameter was specified or not.
     */
    private boolean isSpecified(String parm) {

        if (getParameter(parm) != null) {
            log.info("Parameter " + parm + " is specified as: " + getParameter(parm));
            return true;
        }
        return false;
    }

    /**
     * Returns a local specified by the string localString
     */
    protected static Locale parseLocale(String localString) {
        int x = 0;
        String[] s = { "", "", "" };
        StringTokenizer tokenizer = new StringTokenizer(localString, "_");
        while (tokenizer.hasMoreTokens()) {
            s[x++] = tokenizer.nextToken();
        }
        return new Locale(s[0], s[1], s[2]);
    }

    // static initializer for setting look & feel
    static {
        try {
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }
    }
}