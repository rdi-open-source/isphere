/*
 * @(#)SessionConfig.java
 * Copyright:    Copyright (c) 2001
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

import java.util.*;
import javax.swing.*;
import java.io.*;
import java.text.MessageFormat;
import java.awt.Color;
import java.awt.Rectangle;

import org.tn5250j.tools.LangTool;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.GUIGraphicsUtils;

/**
 * A host session configuration object
 */
public class SessionConfig implements TN5250jConstants {

   private String configurationResource;
   private String sessionName;
   private boolean connected;
   private int sessionType;
   private Properties sesProps;
   private Vector listeners;
   private String sslType;
   private boolean usingDefaults;

   public SessionConfig (String configurationResource,
                           String sessionName) {

      this.configurationResource = configurationResource;
      this.sessionName = sessionName;
      loadConfigurationResource();

   }

   public String getConfigurationResource() {

      if (configurationResource == null || configurationResource == "") {
         configurationResource = "TN5250JDefaults.props";
         usingDefaults = true;
      }

      return configurationResource;

   }

   public String getSessionName() {
      return sessionName;
   }

   /**
    * Notify all registered listeners of the onSessionChanged event.
    *
    * @param state  The state change property object.
    */
   protected void fireConfigChanged(SessionConfigEvent sce) {

      if (listeners != null) {
         int size = listeners.size();
         for (int i = 0; i < size; i++) {
            SessionConfigListener target =
                    (SessionConfigListener)listeners.elementAt(i);
            target.onConfigChanged(sce);
         }
      }
   }

   public void firePropertyChange(Object source, String propertyName,
            Object oldValue, Object newValue) {

      if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
         return;
      }

      java.util.Vector targets = null;
      synchronized (this) {
         if (listeners != null) {
            targets = (java.util.Vector) listeners.clone();
         }
      }

      SessionConfigEvent sce = new SessionConfigEvent(source,
             propertyName, oldValue, newValue);

      if (targets != null) {
         int size = targets.size();
         for (int i = 0; i < size; i++) {
            SessionConfigListener target =
                    (SessionConfigListener)targets.elementAt(i);
            target.onConfigChanged(sce);
         }
      }
   }

   public Properties getProperties() {

      return sesProps;
   }

   public void setSessionProps(Properties props) {

      sesProps.putAll(props);

   }

   public void setModified() {

      sesProps.setProperty("saveme","");
   }

   public void saveSessionProps(java.awt.Container parent) {

      if (sesProps.containsKey("saveme")) {

         sesProps.remove("saveme");

         Object[] args = {getConfigurationResource()};
         String message = MessageFormat.format(
                           LangTool.getString("messages.saveSettings"),
                           args);

         int result = JOptionPane.showConfirmDialog(parent,message);

         if (result == JOptionPane.OK_OPTION) {
            saveSessionProps();
         }


      }

   }

   public void saveSessionProps() {

      if (usingDefaults) {

         ConfigureFactory.getInstance().saveSettings("dfltSessionProps",
                                                      getConfigurationResource(),
                                                      "");

      }
      else {
         try {
            FileOutputStream out = new FileOutputStream(settingsDirectory() + getConfigurationResource());
               // save off the width and height to be restored later
            sesProps.store(out,"------ Defaults --------");
         }
         catch (FileNotFoundException fnfe) {}
         catch (IOException ioe) {}
      }
   }

   private void loadConfigurationResource() {

      sesProps = new Properties();

      if (configurationResource == null || configurationResource == "") {
         configurationResource = "TN5250JDefaults.props";
         usingDefaults = true;
         loadDefaults();
      }
      else {
         try {
            FileInputStream in = new FileInputStream(settingsDirectory() + getConfigurationResource());
            sesProps.load(in);
            if (sesProps.size() == 0)
               loadDefaults();
         }
         catch (IOException ioe) {
            System.out.println("Information Message: Properties file is being "
                                 + "created for first time use:  File name "
                                 + getConfigurationResource());
            loadDefaults();
         }
         catch (SecurityException se) {
            System.out.println(se.getMessage());
         }
      }
   }

   private String settingsDirectory() {
      return ConfigureFactory.getInstance().getProperty("emulator.settingsDirectory");
   }

   private void loadDefaults() {

      try {

         sesProps = ConfigureFactory.getInstance().getProperties(
                                    "dfltSessionProps",
                                    getConfigurationResource(),true,
                                    "Default Settings");
         if (sesProps.size() == 0) {

            Properties schemaProps = new Properties();

            java.net.URL file=null;

            ClassLoader cl = this.getClass().getClassLoader();
            if (cl == null)
               cl = ClassLoader.getSystemClassLoader();
            file = cl.getResource("tn5250jSchemas.properties");
            schemaProps.load(file.openStream());

            // we will now load the default schema
            String prefix = schemaProps.getProperty("schemaDefault");
            sesProps.setProperty("colorBg",schemaProps.getProperty(prefix + ".colorBg"));
            sesProps.setProperty("colorRed",schemaProps.getProperty(prefix + ".colorRed"));
            sesProps.setProperty("colorTurq",schemaProps.getProperty(prefix + ".colorTurq"));
            sesProps.setProperty("colorCursor",schemaProps.getProperty(prefix + ".colorCursor"));
            sesProps.setProperty("colorGUIField",schemaProps.getProperty(prefix + ".colorGUIField"));
            sesProps.setProperty("colorWhite",schemaProps.getProperty(prefix + ".colorWhite"));
            sesProps.setProperty("colorYellow",schemaProps.getProperty(prefix + ".colorYellow"));
            sesProps.setProperty("colorGreen",schemaProps.getProperty(prefix + ".colorGreen"));
            sesProps.setProperty("colorPink",schemaProps.getProperty(prefix + ".colorPink"));
            sesProps.setProperty("colorBlue",schemaProps.getProperty(prefix + ".colorBlue"));
            sesProps.setProperty("colorSep",schemaProps.getProperty(prefix + ".colorSep"));
            sesProps.setProperty("colorHexAttr",schemaProps.getProperty(prefix + ".colorHexAttr"));
            sesProps.setProperty("font",GUIGraphicsUtils.getDefaultFont());
            ConfigureFactory.getInstance().saveSettings("dfltSessionProps",
                                                         getConfigurationResource(),
                                                         "");
         }
      }
      catch (IOException ioe) {
         System.out.println("Information Message: Properties file is being "
                              + "created for first time use:  File name "
                              + getConfigurationResource());
      }
      catch (SecurityException se) {
         System.out.println(se.getMessage());
      }
   }

   public boolean isPropertyExists(String prop) {
      return sesProps.containsKey(prop);
   }

   public String getStringProperty(String prop) {

      if (sesProps.containsKey(prop))
         return (String)sesProps.get(prop);
      else
         return "";

   }

   public int getIntegerProperty(String prop) {

      if (sesProps.containsKey(prop)) {
         try {
            int i = Integer.parseInt((String)sesProps.get(prop));
            return i;
         }
         catch (NumberFormatException ne) {
            return 0;
         }
      }
      else
         return 0;

   }

   public Color getColorProperty(String prop) {

      if (sesProps.containsKey(prop)) {
         Color c = new Color(getIntegerProperty(prop));
         return c;
      }
      else
         return null;

   }

   public Rectangle getRectangleProperty(String key) {

      Rectangle rectProp = new Rectangle();

      if (sesProps.containsKey(key)) {
         String rect = sesProps.getProperty(key);
         StringTokenizer stringtokenizer = new StringTokenizer(rect, ",");
         if (stringtokenizer.hasMoreTokens())
            rectProp.x = Integer.parseInt(stringtokenizer.nextToken());
         if (stringtokenizer.hasMoreTokens())
            rectProp.y = Integer.parseInt(stringtokenizer.nextToken());
         if (stringtokenizer.hasMoreTokens())
            rectProp.width = Integer.parseInt(stringtokenizer.nextToken());
         if (stringtokenizer.hasMoreTokens())
            rectProp.height = Integer.parseInt(stringtokenizer.nextToken());

      }

      return rectProp;

   }

   public void setRectangleProperty(String key, Rectangle rect) {

      String rectStr = rect.x + "," +
                        rect.y + "," +
                        rect.width + "," +
                        rect.height;
      sesProps.setProperty(key,rectStr);
   }

   public float getFloatProperty(String prop) {

      if (sesProps.containsKey(prop)) {
         float f = Float.parseFloat((String)sesProps.get(prop));
         return f;
      }
      else
         return 0.0f;

   }

   public Object setProperty(String key, String value ) {
      return sesProps.setProperty(key,value);
   }

   public Object removeProperty(String key) {
      return sesProps.remove(key);
   }

   public synchronized Vector getSessionConfigListeners () {

      return listeners;
   }

   /**
    * Add a SessionConfigListener to the listener list.
    *
    * @param listener  The SessionListener to be added
    */
   public synchronized void addSessionConfigListener(SessionConfigListener listener) {

      if (listeners == null) {
          listeners = new java.util.Vector(3);
      }
      listeners.addElement(listener);

   }

   /**
    * Remove a SessionListener from the listener list.
    *
    * @param listener  The SessionListener to be removed
    */
   public synchronized void removeSessionConfigListener(SessionConfigListener listener) {
      if (listeners == null) {
          return;
      }
      listeners.removeElement(listener);

   }

}