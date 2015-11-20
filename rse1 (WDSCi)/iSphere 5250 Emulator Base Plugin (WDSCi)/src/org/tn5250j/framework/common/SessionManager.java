/*
 * @(#)SessionManager.java
 * Copyright:    Copyright (c) 2001 - 2004
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
package org.tn5250j.framework.common;

import java.util.*;

import org.tn5250j.tools.logging.*;
import org.tn5250j.interfaces.SessionManagerInterface;
import org.tn5250j.*;


/**
 * The SessionManager is the central repository for access to all sessions.
 * The SessionManager contains a list of all Session objects available.
 */
public class SessionManager implements SessionManagerInterface, TN5250jConstants {

   static private Sessions sessions;
   static private Vector configs;

   private TN5250jLogger log = TN5250jLogFactory.getLogger (this.getClass());
   /**
    * A handle to the unique SessionManager class
    */
   static private SessionManager _instance;

   /**
    * The constructor is made protected to allow overriding.
    */
   protected SessionManager() {
      if (_instance == null) {
         // initialize the settings information
         initialize();
         // set our instance to this one.
         _instance = this;
      }
   }

   /**
    *
    * @return The unique instance of this class.
    */
   static public SessionManager instance() {

      if (_instance == null) {
         _instance = new SessionManager();
      }
      return _instance;

   }

   private void initialize() {
      log.info("New session Manager initialized");
      sessions = new Sessions();
      configs = new Vector(3);

   }

   public Sessions getSessions() {
      return sessions;
   }

   public void closeSession(String sessionName) {

      SessionGUI session = ((Session5250)sessions.item(sessionName)).getGUI();
      if (session != null)
         closeSession(session);

   }

   public void closeSession(SessionGUI sessionObject) {

      sessionObject.closeDown();
      sessions.removeSession(((SessionGUI)sessionObject).getSession());

   }

   public Session5250 openSession(Properties sesProps, String configurationResource
                                                , String sessionName) {
//                                             throws TN5250jException {

      if(sessionName == null)
         sesProps.put(SESSION_TERM_NAME,sesProps.getProperty(SESSION_HOST));
      else
         sesProps.put(SESSION_TERM_NAME,sessionName);

      if (configurationResource == null)
         configurationResource = "";

      sesProps.put(SESSION_CONFIG_RESOURCE
                        ,configurationResource);

      Enumeration e = configs.elements();
      SessionConfig useConfig = null;

      while(e.hasMoreElements()) {
         SessionConfig conf = (SessionConfig)e.nextElement();
         if (conf.getSessionName().equals(sessionName)) {
            useConfig = conf;
         }
      }

      if (useConfig == null) {

         useConfig = new SessionConfig(configurationResource,sessionName);
         configs.add(useConfig);
      }

      Session5250 newSession = new Session5250(sesProps,configurationResource,
                                          sessionName,useConfig);
      sessions.addSession(newSession);
      return newSession;

   }

   /**
    * Convenience method to add a session that is instatiated outside of
    * SessionManager.  An example would be the ProtocolBean.
    *
    * @param newSession
    */
   public void addSession(Session5250 newSession) {

      sessions.addSession(newSession);
   }
}