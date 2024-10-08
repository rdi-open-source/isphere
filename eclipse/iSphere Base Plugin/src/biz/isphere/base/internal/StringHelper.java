/*******************************************************************************
 * Copyright (c) 2012-2023 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.base.internal;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringHelper {

    public static final String WILDCARD_GROUP = "*"; //$NON-NLS-1$
    public static final String WILDCARD_CHARACTER = "?"; //$NON-NLS-1$

    /**
     * Splits a given string of tokens into pieces using a given separator.
     * 
     * @param aText String of tokens, separated by a separator value.
     * @param aSeparator Separator value.
     * @return Array of tokens.
     */
    public static String[] getTokens(String aText, String aSeparator) {
        StringTokenizer tTokenizer = new StringTokenizer(aText, aSeparator);
        int nTokens = tTokenizer.countTokens();
        ArrayList<String> tStringArray = new ArrayList<String>();
        String tItem;
        for (int i = 0; i < nTokens; i++) {
            tItem = tTokenizer.nextToken().trim();
            if (!isNullOrEmpty(tItem)) {
                tStringArray.add(tItem);
            }
        }
        return tStringArray.toArray(new String[tStringArray.size()]);
    }

    /**
     * Concatenates a list of token to a string value, separated by the
     * specified separator value.
     * 
     * @param aTokens String array of tokens.
     * @param aSeparator Separator used to separate the tokens.
     * @return String of tokens, separated by the specified separator.
     */
    public static String concatTokens(String[] aTokens, String aSeparator) {
        StringBuilder tList = new StringBuilder();
        for (String tItem : aTokens) {
            if (!isNullOrEmpty(tItem)) {
                if (tList.length() > 0) {
                    tList.append(aSeparator);
                }
                tList.append(tItem);
            }
        }
        return tList.toString();
    }

    /**
     * Checks a given String for null and empty.
     * 
     * @param aValue String.
     * @return <code>true</code> if the string is null or empty, else
     *         <code>false</code>.
     */
    public static boolean isNullOrEmpty(String aValue) {
        if (aValue == null || aValue.length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns an empty string for a <code>null</code> value.
     * 
     * @param value - string checked for a <code>null</code> value
     * @return string that is not null
     */
    public static String notNull(String value) {

        if (value == null) {
            return ""; //$NON-NLS-1$
        }

        return value;
    }

    /**
     * Removes trailing spaces from a specified input string.
     * 
     * @param aString that is trimmed right
     * @return string without trailing spaces
     */
    public static String trimR(String aString) {
        return aString.replaceAll("\\s+$", "");
    }

    /**
     * Removes leading spaces from a specified input string.
     * 
     * @param aString that is trimmed left
     * @return string without leading spaces
     */
    public static String trimL(String aString) {
        return aString.replaceAll("^\\s+", "");
    }

    /**
     * Returns a fixed length string suitable for IBM API calls.
     * 
     * @param aValue - String value that is expanded to 'aLength'.
     * @param aLength - The length the string is expanded to.
     * @return string with fixed length
     */
    public static String getFixLength(String aValue, int aLength) {
        return getFixLength(aValue, aLength, " ");
    }

    /**
     * Returns a fixed length string suitable for IBM API calls.
     * 
     * @param aValue - String value that is expanded to 'aLength'.
     * @param aLength - The length the string is expanded to.
     * @param aFiller - Character used for filling the value.
     * @return string with fixed length
     */
    public static String getFixLength(String aValue, int aLength, String aFiller) {
        StringBuffer fixLength = new StringBuffer(aValue);
        while (fixLength.length() < aLength) {
            fixLength.append(aFiller);
        }
        return fixLength.toString();
    }

    /**
     * Returns a fixed length string suitable for IBM API calls.
     * 
     * @param aValue - String value that is expanded to 'aLength'.
     * @param aLength - The length the string is expanded to.
     * @return string with fixed length
     */
    public static String getFixLengthLeading(String aValue, int aLength) {
        return getFixLengthLeading(aValue, aLength, " ");
    }

    /**
     * Returns a fixed length string suitable for IBM API calls.
     * 
     * @param aValue - String value that is expanded to 'aLength'.
     * @param aLength - The length the string is expanded to.
     * @param aFiller - Character used for filling the value.
     * @return string with fixed length
     */
    public static String getFixLengthLeading(String aValue, int aLength, String aFiller) {
        StringBuffer fixLength = new StringBuffer();
        while (fixLength.length() < aLength - aValue.length()) {
            fixLength.append(aFiller);
        }
        fixLength.append(aValue);
        return fixLength.toString();
    }

    /**
     * Reverses a given String.
     * 
     * @param aText - String that is put in reverse order.
     * @return reversed string
     */
    public static String reverse(String aText) {
        StringBuilder builder = new StringBuilder(aText);
        return builder.reverse().toString();
    }

    /**
     * <p>
     * Checks whether the character is ASCII 7 bit printable.
     * </p>
     * 
     * <pre>
     *   CharUtils.isAsciiPrintable('a')  = true
     *   CharUtils.isAsciiPrintable('A')  = true
     *   CharUtils.isAsciiPrintable('3')  = true
     *   CharUtils.isAsciiPrintable('-')  = true
     *   CharUtils.isAsciiPrintable('\n') = false
     *   CharUtils.isAsciiPrintable('&copy;') = false
     * </pre>
     * 
     * @param ch the character to check
     * @return true if between 32 and 126 inclusive
     */
    public static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }

    /**
     * Count the number of character that are included in a given string.
     * 
     * @param string - String that is search for a given character
     * @param c - character that is counted
     * @return number of occurrences of 'c'
     */
    public static int count(String string, char c) {
        int count = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count the number of substrings that are included in a given string.
     * 
     * @param string - String that is search for a given character
     * @param substring - substring that is counted
     * @return number of occurrences of 'substring'
     */
    public static int count(String string, String substring) {
        int count = 0;
        int i = 0;
        while ((i = string.indexOf(substring, i)) >= 0) {
            count++;
            i = i + substring.length();
        }
        return count;
    }

    /**
     * Add quotes to a given string.
     * 
     * @param stringToBeQuoted - String the quotes are added to.
     * @return quoted string
     */
    public static String addQuotes(String stringToBeQuoted) {

        StringBuffer stringWithQuotes = new StringBuffer("");
        stringWithQuotes.append("'");
        stringWithQuotes.append(doubleQuotes(stringToBeQuoted));
        stringWithQuotes.append("'");

        return stringWithQuotes.toString();
    }

    /**
     * Doubles quotes of a given string.
     * 
     * @param string - String whose quotes are doubled.
     * @return string with doubled quotes
     */
    public static String doubleQuotes(String string) {

        StringBuffer stringWithDoubledQuotes = new StringBuffer("");

        for (int idx = 0; idx < string.length(); idx++) {
            String character = string.substring(idx, idx + 1);
            stringWithDoubledQuotes.append(character);
            if (character.equals("'")) {
                stringWithDoubledQuotes.append("'");
            }
        }

        return stringWithDoubledQuotes.toString();
    }

    /**
     * Indent and wrap multi-line strings.
     * 
     * @param original the original string to wrap
     * @param width the maximum width of lines
     * @param breakIterator algorithm for breaking lines
     * @param removeNewLines if <code>true</code>, any newlines in the original
     *        string are ignored
     * @return the whole string with embedded newlines
     * @see http
     *      ://www.tutego.de/blog/javainsel/2009/05/texte-umbrechen-word-wrap/
     */
    public static String wrapAndIndentString(String original, String indent, int width) {
        BreakIterator breakIterator = BreakIterator.getWordInstance();

        List<String> lines = wrapStringToArray(original, width, breakIterator, true);
        StringBuffer retBuf = new StringBuffer();

        for (String line : lines) {
            retBuf.append(indent);
            retBuf.append(line);
            retBuf.append('\n');
        }

        return retBuf.toString();
    }

    /**
     * Wrap multi-line strings (and get the individual lines).
     * 
     * @param original the original string to wrap
     * @param width the maximum width of lines
     * @param breakIterator breaks original to chars, words, sentences,
     *        depending on what instance you provide.
     * @param removeNewLines if <code>true</code>, any newlines in the original
     *        string are ignored
     * @return the lines after wrapping
     */
    public static List<String> wrapStringToArray(String original, int width, BreakIterator breakIterator, boolean removeNewLines) {
        if (original.length() == 0) return Arrays.asList(original);

        String[] workingSet;

        // substitute original newlines with spaces,
        // remove newlines from head and tail
        if (removeNewLines) {
            original = original.trim();
            original = original.replace('\n', ' ');
            workingSet = new String[] { original };
        } else {
            StringTokenizer tokens = new StringTokenizer(original, "\n"); // NOI18N
            int len = tokens.countTokens();
            workingSet = new String[len];

            for (int i = 0; i < len; i++)
                workingSet[i] = tokens.nextToken();
        }

        if (width < 1) width = 1;

        if (original.length() <= width) return Arrays.asList(workingSet);

        widthcheck: {
            boolean ok = true;

            for (int i = 0; i < workingSet.length; i++) {
                ok = ok && (workingSet[i].length() < width);

                if (!ok) break widthcheck;
            }

            return Arrays.asList(workingSet);
        }

        ArrayList<String> lines = new ArrayList<String>();

        int lineStart = 0; // the position of start of currently processed line
                           // in
        // the original string

        for (int i = 0; i < workingSet.length; i++) {
            if (workingSet[i].length() < width)
                lines.add(workingSet[i]);
            else {
                breakIterator.setText(workingSet[i]);

                int nextStart = breakIterator.next();
                int prevStart = 0;

                do {
                    while (((nextStart - lineStart) < width) && (nextStart != BreakIterator.DONE)) {
                        prevStart = nextStart;
                        nextStart = breakIterator.next();
                    }

                    if (nextStart == BreakIterator.DONE) nextStart = prevStart = workingSet[i].length();

                    if (prevStart == 0) prevStart = nextStart;

                    lines.add(workingSet[i].substring(lineStart, prevStart));

                    lineStart = prevStart;
                    prevStart = 0;
                } while (lineStart < workingSet[i].length());

                lineStart = 0;
            }
        }

        return lines;
    }

    /**
     * Compares a given string case insensitive with the specified wildcard
     * pattern. Supported wildcard characters are:
     * 
     * <pre>
     * * - replaces a group of characters
     * ? - replaces a single character
     * </pre>
     * 
     * @param pattern - Pattern 'text' is compared to.
     * @param text - Text that is compared to the specified pattern.
     * @return <code>true</code>, when the text matches the pattern, else
     *         <code>false</code>.
     */
    public static boolean matchesGeneric(String text, String pattern) {

        if (text == null) {
            return false;
        }

        if (WILDCARD_GROUP.equals(pattern)) {
            return true;
        }

        // Escape dots (.) and backslashes (\)
        pattern = pattern.replaceAll("\\\\", "\\\\\\\\");
        pattern = pattern.replaceAll("\\.", "\\\\.");

        /**
         * Replace asterisks (*) and question marks (?)<br>
         * Other options:<br>
         * <ul style="list-style-type:square;">
         * <li>(?i) makes the regex case insensitive.</li>
         * <li>(?s) for "single line mode" makes the dot match all characters,
         * including line breaks.</li>
         * <li>(?m) for "multi-line mode" makes the caret and dollar match at
         * the start and end of each line in the subject string.</li>
         * </ul>
         */

        pattern = "^" + pattern.replaceAll("\\" + WILDCARD_GROUP, ".*");
        pattern = pattern.replaceAll("\\" + WILDCARD_CHARACTER, ".") + "$";

        Pattern regexPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = regexPattern.matcher(text);

        return matcher.find();
    }
}
