/**
 * Title: KeyMapper
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.1
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
package org.tn5250j.keyboard;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.KeyStroke;

import org.tn5250j.TN5250jConstants;
import org.tn5250j.event.KeyChangeListener;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.interfaces.OptionAccessFactory;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.system.OperatingSystem;

public class KeyMapper {

    private static HashMap mappedKeys;
    private static KeyStroker workStroke;
    private static String keyMapName;
    private static String lastKeyMnemonic;
    private static Vector listeners;
    private static boolean useJava14;

    /**
     * String value for the jdk 1.4 version of KeyStroker
     */
    private static final String STROKER_NAME14 = "org.tn5250j.keyboard.KeyStroker14";
    /**
     * String value for the jdk versions of KeyStroker less than 1.4
     */
    private static final String STROKER_NAME = "org.tn5250j.keyboard.KeyStroker";

    private static final Constructor NEW_STROKER1;
    private static final Constructor NEW_STROKER2;
    private static final Constructor NEW_STROKER3;

    static {

        useJava14 = OperatingSystem.hasJava14() && !OperatingSystem.isMacOS();
        Class stroker_class;

        // the different KeyStroker constructors
        Constructor constructor1;
        Constructor constructor2;
        Constructor constructor3;

        try {

            ClassLoader loader = KeyMapper.class.getClassLoader();

            if (loader == null) loader = ClassLoader.getSystemClassLoader();

            if (useJava14)
                stroker_class = loader.loadClass(STROKER_NAME14);
            else
                stroker_class = loader.loadClass(STROKER_NAME);

            constructor1 = stroker_class.getConstructor(new Class[] { KeyEvent.class });

            constructor2 = stroker_class.getConstructor(new Class[] { KeyEvent.class, boolean.class });

            constructor3 = stroker_class.getConstructor(new Class[] { int.class, boolean.class, boolean.class, boolean.class, boolean.class,
                int.class });

        } catch (Throwable t) {
            stroker_class = null;
            constructor1 = null;
            constructor2 = null;
            constructor3 = null;
        }

        NEW_STROKER1 = constructor1;
        NEW_STROKER2 = constructor2;
        NEW_STROKER3 = constructor3;
    }

    public static void init() {

        if (mappedKeys != null) return;

        mappedKeys = new HashMap(60);
        workStroke = newKeyStroker(0, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD);

        Properties keys = ConfigureFactory.getInstance().getProperties(ConfigureFactory.KEYMAP);

        if (!loadKeyStrokes(keys)) {
            // keycode shift control alternate

            // Key <-> Keycode , isShiftDown , isControlDown , isAlternateDown,
            // location

            // my personal preference
            mappedKeys.put(newKeyStroker(10, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[fldext]");

            if (useJava14) {
                mappedKeys.put(newKeyStroker(17, false, true, false, false, KeyStroker.KEY_LOCATION_RIGHT), "[enter]");
                mappedKeys.put(newKeyStroker(10, false, false, false, false, KeyStroker.KEY_LOCATION_NUMPAD), "[enter].alt2");
            } else
                mappedKeys.put(newKeyStroker(17, false, true, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[enter]");

            mappedKeys.put(newKeyStroker(8, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[backspace]");
            mappedKeys.put(newKeyStroker(9, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[tab]");
            mappedKeys.put(newKeyStroker(9, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[backtab]");
            mappedKeys.put(newKeyStroker(127, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[delete]");
            mappedKeys.put(newKeyStroker(155, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[insert]");
            mappedKeys.put(newKeyStroker(19, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[clear]");

            if (useJava14)
                mappedKeys.put(newKeyStroker(17, false, true, false, false, KeyStroker.KEY_LOCATION_LEFT), "[reset]");
            else
                mappedKeys.put(newKeyStroker(27, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[reset]");

            if (useJava14)
                mappedKeys.put(newKeyStroker(27, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[sysreq]");
            else
                mappedKeys.put(newKeyStroker(27, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[sysreq]");

            mappedKeys.put(newKeyStroker(35, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[eof]");
            mappedKeys.put(newKeyStroker(36, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[home]");
            mappedKeys.put(newKeyStroker(39, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[right]");
            mappedKeys.put(newKeyStroker(39, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[nextword]");
            mappedKeys.put(newKeyStroker(37, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[left]");
            mappedKeys.put(newKeyStroker(37, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[prevword]");
            mappedKeys.put(newKeyStroker(38, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[up]");
            mappedKeys.put(newKeyStroker(40, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[down]");
            mappedKeys.put(newKeyStroker(34, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pgdown]");
            mappedKeys.put(newKeyStroker(33, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pgup]");

            mappedKeys.put(newKeyStroker(96, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad0]");
            mappedKeys.put(newKeyStroker(97, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad1]");
            mappedKeys.put(newKeyStroker(98, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad2]");
            mappedKeys.put(newKeyStroker(99, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad3]");
            mappedKeys.put(newKeyStroker(100, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad4]");
            mappedKeys.put(newKeyStroker(101, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad5]");
            mappedKeys.put(newKeyStroker(102, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad6]");
            mappedKeys.put(newKeyStroker(103, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad7]");
            mappedKeys.put(newKeyStroker(104, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad8]");
            mappedKeys.put(newKeyStroker(105, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[keypad9]");

            mappedKeys.put(newKeyStroker(109, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[field-]");
            mappedKeys.put(newKeyStroker(107, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[field+]");
            mappedKeys.put(newKeyStroker(112, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf1]");
            mappedKeys.put(newKeyStroker(113, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf2]");
            mappedKeys.put(newKeyStroker(114, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf3]");
            mappedKeys.put(newKeyStroker(115, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf4]");
            mappedKeys.put(newKeyStroker(116, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf5]");
            mappedKeys.put(newKeyStroker(117, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf6]");
            mappedKeys.put(newKeyStroker(118, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf7]");
            mappedKeys.put(newKeyStroker(119, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf8]");
            mappedKeys.put(newKeyStroker(120, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf9]");
            mappedKeys.put(newKeyStroker(121, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf10]");
            mappedKeys.put(newKeyStroker(122, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf11]");
            mappedKeys.put(newKeyStroker(123, false, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf12]");
            mappedKeys.put(newKeyStroker(112, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf13]");
            mappedKeys.put(newKeyStroker(113, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf14]");
            mappedKeys.put(newKeyStroker(114, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf15]");
            mappedKeys.put(newKeyStroker(115, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf16]");
            mappedKeys.put(newKeyStroker(116, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf17]");
            mappedKeys.put(newKeyStroker(117, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf18]");
            mappedKeys.put(newKeyStroker(118, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf19]");
            mappedKeys.put(newKeyStroker(119, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf20]");
            mappedKeys.put(newKeyStroker(120, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf21]");
            mappedKeys.put(newKeyStroker(121, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf22]");
            mappedKeys.put(newKeyStroker(122, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf23]");
            mappedKeys.put(newKeyStroker(123, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[pf24]");
            mappedKeys.put(newKeyStroker(112, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[help]");

            mappedKeys.put(newKeyStroker(72, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[hostprint]");

            if (useJava14)
                mappedKeys.put(newKeyStroker(67, false, true, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[copy]");
            else
                mappedKeys.put(newKeyStroker(67, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[copy]");

            if (useJava14)
                mappedKeys.put(newKeyStroker(86, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[paste]");
            else
                mappedKeys.put(newKeyStroker(86, false, true, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[paste]");

            mappedKeys.put(newKeyStroker(39, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[markright]");
            mappedKeys.put(newKeyStroker(37, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[markleft]");
            mappedKeys.put(newKeyStroker(38, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[markup]");
            mappedKeys.put(newKeyStroker(40, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[markdown]");

            mappedKeys.put(newKeyStroker(155, true, false, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[dupfield]");
            mappedKeys.put(newKeyStroker(17, true, true, false, false, KeyStroker.KEY_LOCATION_STANDARD), "[newline]");
            mappedKeys.put(newKeyStroker(34, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[jumpnext]");
            mappedKeys.put(newKeyStroker(33, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD), "[jumpprev]");

            initISphereSpecialKeys();

            saveKeyMap();
        } else {

            setKeyMap(keys);
            boolean isDirty = initISphereSpecialKeys();
            if (isDirty) {
                saveKeyMap();
            }

        }

    }

    private static boolean initISphereSpecialKeys() {

        boolean isDirty = false;

        // Add iSphere special key strokes for:
        // a) Moving between main sessions: Ctrl+Up and Ctrl+Down
        if (!isKeyStrokeDefined(TN5250jConstants.MNEMONIC_NEXT_SESSION)) {
            mappedKeys.put(new KeyStroker(38, false, true, false, false, KeyStroker.KEY_LOCATION_STANDARD), TN5250jConstants.MNEMONIC_NEXT_SESSION);
            isDirty = true;
        }

        if (!isKeyStrokeDefined(TN5250jConstants.MNEMONIC_PREVIOUS_SESSION)) {
            mappedKeys.put(new KeyStroker(40, false, true, false, false, KeyStroker.KEY_LOCATION_STANDARD),
                TN5250jConstants.MNEMONIC_PREVIOUS_SESSION);
            isDirty = true;
        }

        // b) Moving between minor (multiple) sessions: Alt+Up and Alt+Down
        if (!isKeyStrokeDefined(TN5250jConstants.MNEMONIC_NEXT_MULTIPLE_SESSION)) {
            mappedKeys.put(new KeyStroker(38, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD),
                TN5250jConstants.MNEMONIC_NEXT_MULTIPLE_SESSION);
            isDirty = true;
        }

        if (!isKeyStrokeDefined(TN5250jConstants.MNEMONIC_PREVIOUS_MULTIPLE_SESSION)) {
            mappedKeys.put(new KeyStroker(40, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD),
                TN5250jConstants.MNEMONIC_PREVIOUS_MULTIPLE_SESSION);
            isDirty = true;
        }

        // c) Scrolling sessions: Ctrl+Alt+Up, Ctrl+Alt+Down, Ctrl+Alt+Left and
        // Ctrl+Alt+Right
        if (!isKeyStrokeDefined(TN5250jConstants.MNEMONIC_SCROLL_SESSION_UP)) {
            mappedKeys.put(new KeyStroker(38, false, true, true, false, KeyStroker.KEY_LOCATION_STANDARD),
                TN5250jConstants.MNEMONIC_SCROLL_SESSION_UP);
            isDirty = true;
        }

        if (!isKeyStrokeDefined(TN5250jConstants.MNEMONIC_SCROLL_SESSION_DOWN)) {
            mappedKeys.put(new KeyStroker(40, false, true, true, false, KeyStroker.KEY_LOCATION_STANDARD),
                TN5250jConstants.MNEMONIC_SCROLL_SESSION_DOWN);
            isDirty = true;
        }

        if (!isKeyStrokeDefined(TN5250jConstants.MNEMONIC_SCROLL_SESSION_LEFT)) {
            mappedKeys.put(new KeyStroker(37, false, true, true, false, KeyStroker.KEY_LOCATION_STANDARD),
                TN5250jConstants.MNEMONIC_SCROLL_SESSION_LEFT);
            isDirty = true;
        }

        if (!isKeyStrokeDefined(TN5250jConstants.MNEMONIC_SCROLL_SESSION_RIGHT)) {
            mappedKeys.put(new KeyStroker(39, false, true, true, false, KeyStroker.KEY_LOCATION_STANDARD),
                TN5250jConstants.MNEMONIC_SCROLL_SESSION_RIGHT);
            isDirty = true;
        }

        return isDirty;
    }

    public static boolean isNextMajorSessionKeyStroke(KeyEvent ke) {
        if (TN5250jConstants.MNEMONIC_NEXT_SESSION.equals(getKeyStrokeText(ke))) {
            return true;
        }
        return false;
    }

    public static boolean isPreviousMajorSessionKeyStroke(KeyEvent ke) {
        if (TN5250jConstants.MNEMONIC_PREVIOUS_SESSION.equals(getKeyStrokeText(ke))) {
            return true;
        }
        return false;
    }

    public static boolean isNextMinorSessionKeyStroke(KeyEvent ke) {
        if (TN5250jConstants.MNEMONIC_NEXT_MULTIPLE_SESSION.equals(getKeyStrokeText(ke))) {
            return true;
        }
        return false;
    }

    public static boolean isPreviousMinorSessionKeyStroke(KeyEvent ke) {
        if (TN5250jConstants.MNEMONIC_PREVIOUS_MULTIPLE_SESSION.equals(getKeyStrokeText(ke))) {
            return true;
        }
        return false;
    }

    public static boolean isScrollSessionUpKeyStroke(KeyEvent ke) {
        if (TN5250jConstants.MNEMONIC_SCROLL_SESSION_UP.equals(getKeyStrokeText(ke))) {
            return true;
        }
        return false;
    }

    public static boolean isScrollSessionDownKeyStroke(KeyEvent ke) {
        if (TN5250jConstants.MNEMONIC_SCROLL_SESSION_DOWN.equals(getKeyStrokeText(ke))) {
            return true;
        }
        return false;
    }

    public static boolean isScrollSessionLeftKeyStroke(KeyEvent ke) {
        if (TN5250jConstants.MNEMONIC_SCROLL_SESSION_LEFT.equals(getKeyStrokeText(ke))) {
            return true;
        }
        return false;
    }

    public static boolean isScrollSessionRightKeyStroke(KeyEvent ke) {
        if (TN5250jConstants.MNEMONIC_SCROLL_SESSION_RIGHT.equals(getKeyStrokeText(ke))) {
            return true;
        }
        return false;
    }

    private static KeyStroker newKeyStroker(int keyCode, boolean isShiftDown, boolean isControlDown, boolean isAltDown, boolean isAltGrDown,
        int location) {

        Integer keyInt = new Integer(keyCode);
        Boolean shiftBool = new Boolean(isShiftDown);
        Boolean controlBool = new Boolean(isControlDown);
        Boolean altBool = new Boolean(isAltDown);
        Boolean altGrBool = new Boolean(isAltGrDown);
        Integer locationInt = new Integer(location);

        try {
            Object obj = NEW_STROKER3.newInstance(new Object[] { keyInt, shiftBool, controlBool, altBool, altGrBool, locationInt });
            return (KeyStroker)obj;
        } catch (Throwable crap) {

        }

        return new KeyStroker(keyCode, isShiftDown, isControlDown, isAltDown, isAltGrDown, location);

    }

    private static KeyStroker newKeyStroker(KeyEvent keyEvent) {

        try {
            Object obj = NEW_STROKER1.newInstance(new Object[] { keyEvent });
            return (KeyStroker)obj;
        } catch (Throwable crap) {

        }

        return new KeyStroker(keyEvent);

    }

    private static KeyStroker newKeyStroker(KeyEvent keyEvent, boolean isAltGrDown) {

        Boolean altGrBool = new Boolean(isAltGrDown);

        try {
            Object obj = NEW_STROKER2.newInstance(new Object[] { keyEvent, altGrBool });
            return (KeyStroker)obj;
        } catch (Throwable crap) {

        }

        return new KeyStroker(keyEvent, isAltGrDown);

    }

    private static boolean loadKeyStrokes(Properties keystrokes) {

        keystrokes = ConfigureFactory.getInstance().getProperties(ConfigureFactory.KEYMAP);
        if (keystrokes != null && keystrokes.size() > 0)
            return true;
        else
            return false;
    }

    private static void parseKeyStrokes(Properties keystrokes) {

        String theStringList = "";
        String theKey = "";
        Enumeration ke = keystrokes.propertyNames();
        while (ke.hasMoreElements()) {
            theKey = (String)ke.nextElement();

            if (OptionAccessFactory.getInstance().isRestrictedOption(theKey)) {
                continue;
            }

            theStringList = keystrokes.getProperty(theKey);
            int x = 0;
            int kc = 0;
            boolean is = false;
            boolean ic = false;
            boolean ia = false;
            boolean iag = false;
            int location = KeyStroker.KEY_LOCATION_STANDARD;

            StringTokenizer tokenizer = new StringTokenizer(theStringList, ",");

            // first is the keycode
            kc = Integer.parseInt(tokenizer.nextToken());
            // isShiftDown
            if (tokenizer.nextToken().equals("true"))
                is = true;
            else
                is = false;
            // isControlDown
            if (tokenizer.nextToken().equals("true"))
                ic = true;
            else
                ic = false;
            // isAltDown
            if (tokenizer.nextToken().equals("true"))
                ia = true;
            else
                ia = false;

            // isAltDown Gr
            if (tokenizer.hasMoreTokens()) {
                if (tokenizer.nextToken().equals("true"))
                    iag = true;
                else
                    iag = false;

                if (tokenizer.hasMoreTokens()) {
                    location = Integer.parseInt(tokenizer.nextToken());
                }
            }

            mappedKeys.put(newKeyStroker(kc, is, ic, ia, iag, location), theKey);

        }

    }

    protected static void setKeyMap(Properties keystrokes) {

        parseKeyStrokes(keystrokes);

    }

    public final static boolean isEqualLast(KeyEvent ke) {
        return workStroke.equals(ke);
    }

    public final static void saveKeyMap() {

        Properties map = ConfigureFactory.getInstance().getProperties(ConfigureFactory.KEYMAP);

        map.clear();

        // save off the keystrokes in the keymap
        Collection v = mappedKeys.values();
        Set o = mappedKeys.keySet();
        Iterator k = o.iterator();
        Iterator i = v.iterator();
        while (k.hasNext()) {
            KeyStroker ks = (KeyStroker)k.next();
            String keyVal = ks.toString();
            map.put(i.next(), ks.toString());
        }

        ConfigureFactory.getInstance().saveSettings(ConfigureFactory.KEYMAP,
            "------ Key Map key=keycode,isShiftDown,isControlDown,isAltDown,isAltGrDown,location --------");
    }

    public final static String getKeyStrokeText(KeyEvent ke) {
        return getKeyStrokeText(ke, false);
    }

    public final static String getKeyStrokeText(KeyEvent ke, boolean isAltGr) {
        if (!workStroke.equals(ke, isAltGr)) {
            workStroke.setAttributes(ke, isAltGr);
            lastKeyMnemonic = (String)mappedKeys.get(workStroke);
        }

        if (lastKeyMnemonic != null && lastKeyMnemonic.endsWith(KeyStroker.altSuffix)) {

            lastKeyMnemonic = lastKeyMnemonic.substring(0, lastKeyMnemonic.indexOf(KeyStroker.altSuffix));
        }

        return lastKeyMnemonic;

    }

    public final static String getKeyStrokeMnemonic(KeyEvent ke) {
        return getKeyStrokeMnemonic(ke, false);
    }

    public final static String getKeyStrokeMnemonic(KeyEvent ke, boolean isAltGr) {

        workStroke.setAttributes(ke, isAltGr);
        String keyMnemonic = (String)mappedKeys.get(workStroke);

        if (keyMnemonic != null && keyMnemonic.endsWith(KeyStroker.altSuffix)) {

            keyMnemonic = keyMnemonic.substring(0, keyMnemonic.indexOf(KeyStroker.altSuffix));
        }

        return keyMnemonic;

    }

    public final static int getKeyStrokeCode() {
        return workStroke.hashCode();
    }

    public final static String getKeyStrokeDesc(String which) {

        Collection v = mappedKeys.values();
        Set o = mappedKeys.keySet();
        Iterator k = o.iterator();
        Iterator i = v.iterator();
        while (k.hasNext()) {
            KeyStroker ks = (KeyStroker)k.next();
            String keyVal = (String)i.next();
            if (keyVal.equals(which)) return ks.getKeyStrokeDesc();
        }

        return LangTool.getString("key.dead");
    }

    public final static KeyStroker getKeyStroker(String which) {

        Collection v = mappedKeys.values();
        Set o = mappedKeys.keySet();
        Iterator k = o.iterator();
        Iterator i = v.iterator();
        while (k.hasNext()) {
            KeyStroker ks = (KeyStroker)k.next();
            String keyVal = (String)i.next();
            if (keyVal.equals(which)) return ks;
        }

        return null;
    }

    public final static boolean isKeyStrokeDefined(String which) {

        Collection v = mappedKeys.values();
        Set o = mappedKeys.keySet();
        Iterator k = o.iterator();
        Iterator i = v.iterator();
        while (k.hasNext()) {
            KeyStroker ks = (KeyStroker)k.next();
            String keyVal = (String)i.next();
            if (keyVal.equals(which)) return true;
        }

        return false;
    }

    public final static boolean isKeyStrokeDefined(KeyEvent ke) {
        return isKeyStrokeDefined(ke, false);
    }

    public final static boolean isKeyStrokeDefined(KeyEvent ke, boolean isAltGr) {

        workStroke.setAttributes(ke, isAltGr);
        return (null != (String)mappedKeys.get(workStroke));

    }

    public final static KeyStroke getKeyStroke(String which) {

        Collection v = mappedKeys.values();
        Set o = mappedKeys.keySet();
        Iterator k = o.iterator();
        Iterator i = v.iterator();
        while (k.hasNext()) {
            KeyStroker ks = (KeyStroker)k.next();
            String keyVal = (String)i.next();
            if (keyVal.equals(which)) {
                int mask = 0;

                if (ks.isShiftDown()) mask |= InputEvent.SHIFT_MASK;
                if (ks.isControlDown()) mask |= InputEvent.CTRL_MASK;
                if (ks.isAltDown()) mask |= InputEvent.ALT_MASK;
                if (ks.isAltGrDown()) mask |= InputEvent.ALT_GRAPH_MASK;

                return KeyStroke.getKeyStroke(ks.getKeyCode(), mask);
            }
        }

        return KeyStroke.getKeyStroke(0, 0);
    }

    public final static void removeKeyStroke(String which) {

        Collection v = mappedKeys.values();
        Set o = mappedKeys.keySet();
        Iterator k = o.iterator();
        Iterator i = v.iterator();
        while (k.hasNext()) {
            KeyStroker ks = (KeyStroker)k.next();
            String keyVal = (String)i.next();
            if (keyVal.equals(which)) {
                mappedKeys.remove(ks);
                return;
            }
        }

    }

    public final static void setKeyStroke(String which, KeyEvent ke) {

        if (ke == null) return;
        setKeyStroker(which, new KeyStroker(ke));

    }

    public final static void setKeyStroke(String which, KeyEvent ke, boolean isAltGr) {

        if (ke == null) return;
        setKeyStroker(which, new KeyStroker(ke, isAltGr));

    }

    public final static void setKeyStroker(String which, KeyStroker keyStroker) {

        if (keyStroker == null) return;
        Collection<String> v = mappedKeys.values();
        Set<KeyStroker> o = mappedKeys.keySet();
        Iterator<KeyStroker> k = o.iterator();
        Iterator<String> i = v.iterator();
        while (k.hasNext()) {
            KeyStroker ks = k.next();
            String keyVal = i.next();
            if (keyVal.equals(which)) {
                mappedKeys.remove(ks);
                mappedKeys.put(keyStroker, keyVal);
                return;
            }
        }

        // if we got here it was a dead key and we need to add it.
        mappedKeys.put(keyStroker, which);

    }

    public final static HashMap getKeyMap() {

        return mappedKeys;
    }

    /**
     * Add a KeyChangeListener to the listener list.
     * 
     * @param listener The KeyChangedListener to be added
     */
    public static synchronized void addKeyChangeListener(KeyChangeListener listener) {

        if (listeners == null) {
            listeners = new java.util.Vector(3);
        }
        listeners.addElement(listener);

    }

    /**
     * Remove a Key Change Listener from the listener list.
     * 
     * @param listener The KeyChangeListener to be removed
     */
    public synchronized void removeKeyChangeListener(KeyChangeListener listener) {
        if (listeners == null) {
            return;
        }
        listeners.removeElement(listener);

    }

    /**
     * Notify all registered listeners of the Key Change Event.
     * 
     */
    public static void fireKeyChangeEvent() {

        if (listeners != null) {
            System.out.println(" changed key ");
            int size = listeners.size();
            for (int i = 0; i < size; i++) {
                KeyChangeListener target = (KeyChangeListener)listeners.elementAt(i);
                target.onKeyChanged();
            }
        }
    }

    public static boolean hasFastCursorMappingConflicts() {

        init();

        KeyStroker fastCursorUpKeyStroker = getKeyStroker(TN5250jConstants.MNEMONIC_FAST_CURSOR_UP);
        KeyStroker nextMultipleSessionKeyStroker = getKeyStroker(TN5250jConstants.MNEMONIC_NEXT_MULTIPLE_SESSION);

        if (fastCursorUpKeyStroker == null && "Alt + Up".equals(nextMultipleSessionKeyStroker.getKeyStrokeDesc())) { //$NON-NLS-1$
            return true;
        }

        KeyStroker fastCursorDownKeyStroker = getKeyStroker(TN5250jConstants.MNEMONIC_FAST_CURSOR_DOWN);
        KeyStroker previousMultipleSessionKeyStroker = getKeyStroker(TN5250jConstants.MNEMONIC_PREVIOUS_MULTIPLE_SESSION);

        if (fastCursorDownKeyStroker == null && "Alt + Down".equals(previousMultipleSessionKeyStroker.getKeyStrokeDesc())) { //$NON-NLS-1$
            return true;
        }

        return false;
    }

    public static void resolveFastCursorMappingConflicts() {

        // Remove mappings for: next/previous multiple session
        removeKeyStroke(TN5250jConstants.MNEMONIC_NEXT_MULTIPLE_SESSION);
        removeKeyStroke(TN5250jConstants.MNEMONIC_PREVIOUS_MULTIPLE_SESSION);

        // Add mappings for: next/previous multiple session
        setKeyStroker(TN5250jConstants.MNEMONIC_NEXT_MULTIPLE_SESSION,
            new KeyStroker(39, false, true, false, false, KeyStroker.KEY_LOCATION_STANDARD));
        setKeyStroker(TN5250jConstants.MNEMONIC_PREVIOUS_MULTIPLE_SESSION, new KeyStroker(37, false, true, false, false,
            KeyStroker.KEY_LOCATION_STANDARD));

        // Add mappings for: fast cursor up/down
        setKeyStroker(TN5250jConstants.MNEMONIC_FAST_CURSOR_UP, new KeyStroker(38, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD));
        setKeyStroker(TN5250jConstants.MNEMONIC_FAST_CURSOR_DOWN, new KeyStroker(40, false, false, true, false, KeyStroker.KEY_LOCATION_STANDARD));

        saveKeyMap();

        fireKeyChangeEvent();
    }
}
