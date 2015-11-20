/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001-2003
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.4
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.tn5250j.tools.logging.*;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.TabClosedListener;
import org.tn5250j.interfaces.GUIViewInterface;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.gui.TN5250jTabbedPane;

public class Gui5250Frame extends GUIViewInterface implements
                                                    ChangeListener,
                                                    TN5250jConstants,
                                                    SessionListener,
                                                    TabClosedListener,
                                                    SessionJumpListener {

   BorderLayout borderLayout1 = new BorderLayout();
//   JTabbedPane sessionPane = new JTabbedPane();
   TN5250jTabbedPane sessionPane = new TN5250jTabbedPane();
   private int selectedIndex = 0;
   private boolean embedded = false;
   private boolean hideTabBar = false;
   public static int count = 0;
   private TN5250jLogger log = TN5250jLogFactory.getLogger (this.getClass());


   //Construct the frame
   public Gui5250Frame(My5250 m) {
      super(m);
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try  {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   //Component initialization
   private void jbInit() throws Exception  {

      this.getContentPane().setLayout(borderLayout1);

      // update the frame sequences
      frameSeq = sequence++;
      
      
//      try
//      {
//        ClassLoader loader = Gui5250Frame.class.getClassLoader();
//        if (loader == null)
//          loader = ClassLoader.getSystemClassLoader();
//
//        Class       tabClass;
//        Constructor keyProcessorConstructor;
//
//        try {
//           String className = "org.tn5250j.gui.TN5250jTabbedPane";
//           if (!OperatingSystem.hasJava14())
//              className += "13";
//
//           tabClass = loader.loadClass(className);
//           tabConstructor = tabkeyProcessorClass.getConstructor(null);
//           Object obj = keyProcessorConstructor.newInstance(null);
//      }
//      catch (Throwable t) {
//      }
      
      sessionPane.setBorder(BorderFactory.createEtchedBorder());
      sessionPane.setBounds(new Rectangle(78, 57, 5, 5));
      sessionPane.setOpaque(true);
      sessionPane.setRequestFocusEnabled(false);
      sessionPane.setDoubleBuffered(false);

      sessionPane.addChangeListener(this);
      sessionPane.addtabCloseListener(this);

      Properties props = ConfigureFactory.getInstance().
                           getProperties(ConfigureFactory.SESSIONS);

      if (props.getProperty("emul.hideTabBar","no").equals("yes"))
         hideTabBar = true;

      if (!hideTabBar) {
         this.getContentPane().add(sessionPane, BorderLayout.CENTER);
      }

      if (count == 0) setSessionTitle();

      if (packFrame)
         pack();
      else
         validate();


   }

   public void centerFrame() {

      if (packFrame)
         pack();
      else
         validate();

      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;

      setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height) / 2);


   }

   public int getFrameSequence() {

      return frameSeq;
   }

   //Overridden so we can exit on System Close
   protected void processWindowEvent(WindowEvent e) {
      super.processWindowEvent(e);
      if(e.getID() == WindowEvent.WINDOW_CLOSING) {
         me.closingDown(this);
      }
   }


   public void update(Graphics g) {
      paint(g);
   }

   public void onSessionJump(SessionJumpEvent jumpEvent) {

      switch (jumpEvent.getJumpDirection()) {

         case JUMP_PREVIOUS:
            prevSession();
            break;
         case JUMP_NEXT:
            nextSession();
            break;
      }
   }

   private void nextSession() {

      final int index = sessionPane.getSelectedIndex();
      sessionPane.setForegroundAt(index,Color.black);
//      sessionPane.setIconAt(index,unfocused);
      sessionPane.setIconAt(unfocused,index);


      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            int index1 = index;
            if (index1 < sessionPane.getTabCount() - 1) {
               sessionPane.setSelectedIndex(++index1);
               sessionPane.setForegroundAt(index1,Color.blue);
//               sessionPane.setIconAt(index1,focused);
               sessionPane.setIconAt(focused,index1);

            }
            else {
               sessionPane.setSelectedIndex(0);
               sessionPane.setForegroundAt(0,Color.blue);
//               sessionPane.setIconAt(0,focused);
               sessionPane.setIconAt(focused,0);

            }

            ((SessionGUI)sessionPane.getComponent(sessionPane.getSelectedIndex())).grabFocus();

            setSessionTitle();
       }
      });

   }

   private void prevSession() {

      final int index = sessionPane.getSelectedIndex();
      sessionPane.setForegroundAt(index,Color.black);
//      sessionPane.setIconAt(index,unfocused);
      sessionPane.setIconAt(unfocused,index);

      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            int index1 = index;
            if (index1 == 0) {
               sessionPane.setSelectedIndex(sessionPane.getTabCount() - 1);
               sessionPane.setForegroundAt(sessionPane.getSelectedIndex(),Color.blue);
//               sessionPane.setIconAt(sessionPane.getSelectedIndex(),focused);
               sessionPane.setIconAt(focused,sessionPane.getSelectedIndex());

            }
            else {
               sessionPane.setSelectedIndex(--index1);
               sessionPane.setForegroundAt(index1,Color.blue);
//               sessionPane.setIconAt(index1,focused);
               sessionPane.setIconAt(focused,index1);

            }

            ((SessionGUI)sessionPane.getComponent(sessionPane.getSelectedIndex())).grabFocus();
            setSessionTitle();
       }
      });
   }

   public void stateChanged(ChangeEvent e) {

//      JTabbedPane p = (JTabbedPane)e.getSource();
      TN5250jTabbedPane p = (TN5250jTabbedPane)e.getSource();

      p.setForegroundAt(selectedIndex,Color.black);
//      p.setIconAt(selectedIndex,unfocused);
      p.setIconAt(unfocused,selectedIndex);

      SessionGUI sg = (SessionGUI)p.getComponentAt(selectedIndex);
      sg.setVisible(false);

      sg = (SessionGUI)p.getSelectedComponent();

      if (sg == null)
         return;

      sg.setVisible(true);
      sg.grabFocus();

      selectedIndex = p.getSelectedIndex();
      p.setForegroundAt(selectedIndex,Color.blue);
//      p.setIconAt(selectedIndex,focused);
      p.setIconAt(focused,selectedIndex);

      setSessionTitle();

   }

   private void setSessionTitle() {

      SessionGUI ses = getSessionAt(selectedIndex);

      if (ses != null && ses.getAllocDeviceName() != null && ses.isConnected()) {
         if (sequence - 1 > 0)
            setTitle(ses.getAllocDeviceName() + " - tn5250j <" + sequence + "> - " + tn5250jRelease + tn5250jVersion + tn5250jSubVer);
         else
            setTitle(ses.getAllocDeviceName() + " - tn5250j - " + tn5250jRelease + tn5250jVersion + tn5250jSubVer);
      }
      else {

         if (sequence - 1 > 0)
            setTitle("tn5250j <" + sequence + "> - " + tn5250jRelease + tn5250jVersion + tn5250jSubVer);
         else
            setTitle("tn5250j - " + tn5250jRelease + tn5250jVersion + tn5250jSubVer);
      }

		count +=1;

   }

   public void addSessionView(String tabText,SessionGUI sessionView) {

      final SessionGUI session = sessionView;

      if (hideTabBar && sessionPane.getTabCount() == 0 && !embedded) {

         this.getContentPane().add(session, BorderLayout.CENTER);
         session.addSessionListener(this);

         session.resizeMe();
         repaint();
         if (packFrame)
            pack();
         else
            validate();
         embedded = true;
         session.grabFocus();
         setSessionTitle();
      }
      else {

         if (hideTabBar && sessionPane.getTabCount() == 0 ) {
            SessionGUI ses = null;
            for (int x=0; x < this.getContentPane().getComponentCount(); x++) {

               if (this.getContentPane().getComponent(x) instanceof SessionGUI) {
                  ses = (SessionGUI)(this.getContentPane().getComponent(x));
                  this.getContentPane().remove(x);
                  break;
               }
            }

            //ses = (Session)(this.getContentPane().getComponent(0));
//            sessionPane.addTab(tabText,focused,ses);
            sessionPane.addTab(tabText,ses,focused);
            final SessionGUI finalSession = ses;

            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  finalSession.resizeMe();
                  finalSession.repaint();
               }
            });


            if (ses.getAllocDeviceName() != null)
               sessionPane.setTitleAt(0,ses.getAllocDeviceName());
            else
               sessionPane.setTitleAt(0,ses.getSessionName());

            ses.addSessionListener(this);
            ses.addSessionJumpListener(this);

            this.getContentPane().add(sessionPane, BorderLayout.CENTER);
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  repaint();
                  finalSession.grabFocus();
               }
            });
         }

//         sessionPane.addTab(tabText,focused,session);
         sessionPane.addTab(tabText,session,focused);

         sessionPane.setForegroundAt(sessionPane.getSelectedIndex(),Color.black);
//         sessionPane.setIconAt(sessionPane.getSelectedIndex(),unfocused);
         sessionPane.setIconAt(unfocused,sessionPane.getSelectedIndex());

         sessionPane.setSelectedIndex(sessionPane.getTabCount()-1);
         sessionPane.setForegroundAt(sessionPane.getSelectedIndex(),Color.blue);
//         sessionPane.setIconAt(sessionPane.getSelectedIndex(),focused);
         sessionPane.setIconAt(focused,sessionPane.getSelectedIndex());

         session.addSessionListener(this);
         session.addSessionJumpListener(this);

         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               session.resizeMe();
               session.repaint();
               session.grabFocus();
            }
         });
      }
   }

   public void tabClosed(int tabToBeClosed){

      me.closeSession(this.getSessionAt(tabToBeClosed));
   }

   public void removeSessionView(SessionGUI targetSession) {

      if (hideTabBar && sessionPane.getTabCount() == 0) {
         for (int x=0; x < getContentPane().getComponentCount(); x++) {

            if (getContentPane().getComponent(x) instanceof SessionGUI) {
               getContentPane().remove(x);
            }
         }
      }
      else {

         int index = sessionPane.indexOfComponent(targetSession);
         log.info("session found and closing down " + index);
         targetSession.removeSessionListener(this);
         targetSession.removeSessionJumpListener(this);
         int tabs = sessionPane.getTabCount();
         sessionPane.remove(index);
         tabs--;


         if (index < tabs) {
            sessionPane.setSelectedIndex(index);
            sessionPane.setForegroundAt(index,Color.blue);
//            sessionPane.setIconAt(index,focused);
            sessionPane.setIconAt(focused,index);
            ((SessionGUI)sessionPane.getComponentAt(index)).requestFocus();
         }
         else {

            if (tabs > 0) {
               sessionPane.setSelectedIndex(0);
               sessionPane.setForegroundAt(0,Color.blue);
//               sessionPane.setIconAt(0,focused);
               sessionPane.setIconAt(focused,0);
               ((SessionGUI)sessionPane.getComponentAt(0)).requestFocus();
            }

         }
      }
   }

   public int getSessionViewCount() {

      if (hideTabBar && sessionPane.getTabCount() == 0) {
         for (int x=0; x < this.getContentPane().getComponentCount(); x++) {

            if (this.getContentPane().getComponent(x) instanceof SessionGUI) {
               return 1;
            }
         }
         return 0;
      }
      else
         return sessionPane.getTabCount();
   }

   public SessionGUI getSessionAt( int index) {

      if (hideTabBar && sessionPane.getTabCount() == 0) {
         for (int x=0; x < this.getContentPane().getComponentCount(); x++) {

            if (this.getContentPane().getComponent(x) instanceof SessionGUI) {
               return (SessionGUI)getContentPane().getComponent(x);
            }
         }
         return null;
      }
      else {
         if (sessionPane.getTabCount() <= 0)
            return null;
         return (SessionGUI)sessionPane.getComponentAt(index);
      }
   }

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      Session5250 ses5250 = (Session5250)changeEvent.getSource();
      SessionGUI ses = ses5250.getGUI();

      switch (changeEvent.getState()) {
         case STATE_CONNECTED:

            final String d = ses.getAllocDeviceName();
            if (d != null) {
               System.out.println(changeEvent.getState() + " " + d);
               final int index = sessionPane.indexOfComponent(ses);
               if (index >= 0) {
                  Runnable tc = new Runnable () {
                     public void run() {
                        sessionPane.setTitleAt(index,d);
                     }
                  };
                  SwingUtilities.invokeLater(tc);
               }
               setSessionTitle();

            }
            break;
      }
   }

   public boolean containsSession(SessionGUI session) {

      if (hideTabBar && sessionPane.getTabCount() == 0) {
         for (int x=0; x < this.getContentPane().getComponentCount(); x++) {

            if (this.getContentPane().getComponent(x) instanceof SessionGUI) {
               return ((SessionGUI)getContentPane().getComponent(x)).equals(session);
            }
         }
         return false;
      }
      else
         return (sessionPane.indexOfComponent(session) >= 0);

   }

}
