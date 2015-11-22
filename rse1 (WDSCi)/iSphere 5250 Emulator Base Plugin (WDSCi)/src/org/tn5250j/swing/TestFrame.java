package org.tn5250j.swing;

import java.awt.*;
import javax.swing.*;

import org.tn5250j.*;
import org.tn5250j.interfaces.*;

public class TestFrame extends JFrame {
    static {
        ConfigureFactory.getInstance();
        // WVL - LDC : 11/07/2003
        //
        // LEAVE THIS INITIALIZER IN THIS PLACE SO IT HAPPENS
        // BEFORE ANY OTHER STATIC INITIALISATION
        org.tn5250j.tools.LangTool.init();
    }

    public static void main(String[] args) {
        JFrame frm = new TestFrame();
        frm.pack();
        frm.setVisible(true);
    }

    public TestFrame() {
        super("Terminal");
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        try {
            String system = "LDCDEV";
            String config = system + ".properties";

            session = new SessionBean(config, system);
            session.setHostName("193.168.51.1");
            session.setCodePage("Cp1141");
            session.connect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        terminal = new JTerminal(session.getSession());

        // RepaintManager repaintManager =
        // RepaintManager.currentManager(terminal);
        // repaintManager.setDoubleBufferingEnabled(false);
        // terminal.setDebugGraphicsOptions(DebugGraphics.FLASH_OPTION);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(terminal);
    }

    private SessionBean session;
    private JTerminal terminal;
}
