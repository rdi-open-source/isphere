/*******************************************************************************
 * javahexeditor, a java hex editor
 * Copyright (C) 2006-2015 Jordi Bergenthal, pestatije(-at_)users.sourceforge.net
 * The official javahexeditor site is sourceforge.net/projects/javahexeditor
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 * Contributors:
 *     iSphere Project Owners - Maintenance and enhancements
 *******************************************************************************/

package biz.isphere.core.swt.widgets.hexeditor.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Find helper class to find binary and string literals in files. Given a
 * literal, finds its position in the file. It is possible to get subsequent
 * finds. The search is either binary or text based. Text based search uses
 * standard java unicode (all of big and little endian, odd and even address)
 * plus ASCII when the literal falls within ASCII char limits.
 * 
 * @author Jordi
 */
public final class BinaryContentFinder {

    public static final class Match {
        private boolean found;
        private long startPosition;
        private int length;
        private IOException exception;

        Match(boolean found, long position, int length, IOException exception) {
            super();
            this.found = found;
            this.startPosition = position;
            this.length = length;
            this.exception = exception;
        }

        public boolean isFound() {
            return found;
        }

        public long getStartPosition() {
            return startPosition;
        }

        public long getEndPosition() {
            return startPosition + length;
        }

        public int getLength() {
            return length;
        }

        public IOException getException() {
            return exception;
        }

    }

    public static final int MAP_SIZE = 64 * 1024;
    // a search string of 2K should be enough
    public static final int MAX_SEQUENCE_SIZE = 2 * 1024;

    private long bufferPosition = -1L;
    private ByteBuffer byteBuffer;
    private int currentPartFound = -1; // relative positions
    private boolean currentPartFoundIsUnicode;
    private long currentPosition = 0L; // absolute value, start of forward
    // finds,
    // end(exclusive) of backward finds
    private byte[] myByteFindSequenceOriginalOrLowerCase;
    private byte[] myByteFindSequenceAllUpperCase;
    private boolean myCaseSensitive = true;
    private BinaryContent myContent;
    private boolean myDirectionForward = true;
    private String myLiteral;
    private String myLiteralEncodedOriginalOrLowerCase;
    private String myLiteralEncodedAllUpperCase;
    private CharsetEncoder myCharsetEncoder;
    private int myLiteralByteLength = -1;
    private Pattern myPattern;
    private boolean stopSearching;

    /**
     * Create a finder object for a sequence of characters; uses unicode and
     * ASCII traversing
     * 
     * @param literal the char sequence to find
     * @param aContent provider to be traversed
     * @throws CharacterCodingException
     */
    public BinaryContentFinder(String literal, String charset, BinaryContent aContent, boolean beSensitive) throws CharacterCodingException {

        myCharsetEncoder = Charset.forName(charset).newEncoder().onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith(new byte[] { (byte)0xE1 });

        setCaseSensitive(beSensitive);
        myLiteral = literal;

        initSearchUnicodeAscii();
        myContent = aContent;
        bufferPosition = 0L;
        currentPosition = 0L;
    }

    /**
     * Create a finder object for a raw sequence of bytes
     * 
     * @param sequence the byte sequence to find
     * @param aContent provider to be traversed
     */
    public BinaryContentFinder(byte[] sequence, BinaryContent aContent) {

        initSearchHex(sequence);
        myContent = aContent;
        bufferPosition = 0L;
        currentPosition = 0L;
    }

    private void findAllMatches() {
        currentPartFound = findHexAsciiMatchInPart();
        int currentPartFoundUnicode = findUnicodeMatchInPart();
        currentPartFoundIsUnicode = false;

        if (currentPartFoundUnicode >= 0
            && (currentPartFound < 0 || myDirectionForward && currentPartFound > currentPartFoundUnicode || !myDirectionForward
                && currentPartFound < currentPartFoundUnicode)) {
            currentPartFound = currentPartFoundUnicode;
            currentPartFoundIsUnicode = true;
        }
    }

    private int findHexAsciiMatchInPart() {
        if (myByteFindSequenceOriginalOrLowerCase == null) {
            return -1;
        }

        int start = 0;
        int inclusiveEnd = byteBuffer.limit() - myByteFindSequenceOriginalOrLowerCase.length;
        if (!myDirectionForward) {
            start = inclusiveEnd;
            inclusiveEnd = 0;
        }

        for (int i = start; myDirectionForward && i <= inclusiveEnd || !myDirectionForward && i >= inclusiveEnd; i += myDirectionForward ? 1 : -1) {
            boolean matchesSoFar = true;
            for (int j = 0; j < myByteFindSequenceOriginalOrLowerCase.length && matchesSoFar; ++j) {
                byte existing = byteBuffer.get(i + j);
                byte matcher = myByteFindSequenceOriginalOrLowerCase[j];
                if (existing != matcher) {
                    if (myCaseSensitive) {
                        matchesSoFar = false;
                    } else {
                        matcher = myByteFindSequenceAllUpperCase[j];
                        if (existing != matcher) {
                            matchesSoFar = false;
                        }
                    }
                }
            }
            if (matchesSoFar) {
                return i;
            }
        }

        return -1;
    }

    private int findUnicodeMatchInPart() {
        if (myPattern == null) {
            return -1;
        }

        int result = Integer.MAX_VALUE;
        if (!myDirectionForward) {
            result = -1;
        }
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        Matcher matcher = myPattern.matcher(byteBuffer.asCharBuffer());

        for (int encoding = 0; encoding < 4; ++encoding) {
            while (matcher.find()) {
                int index = matcher.start() * 2 + (encoding >= 2 ? 1 : 0);
                if (myDirectionForward && result > index || !myDirectionForward && result < index) {
                    result = index;
                }
                if (myDirectionForward) {
                    break;
                }
            }
            if (encoding == 0) {
                byteBuffer.order(ByteOrder.BIG_ENDIAN);
            } else if (encoding == 1 && byteBuffer.limit() > 0) {
                byteBuffer.position(1);
            } else if (encoding == 2) {
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            }
            matcher.reset(byteBuffer.asCharBuffer());
        }
        if (result == Integer.MAX_VALUE) {
            result = -1;
        }

        return result;
    }

    private long getContentLength() {
        if (myContent == null) {
            return 0L;
        }

        return myContent.length();
    }

    /**
     * Get the next position and length of a matching literal.
     * 
     * @return The {@link Match} describing the result, either not found, found
     *         or exception.
     */
    public Match getNextMatch() {
        stopSearching = false;
        try {
            populatePart();
            findAllMatches();

            while (currentPartFound < 0) { // end of part
                if (nextPart() == null || stopSearching) {
                    stopSearching = false;
                    return new Match(false, 0, 0, null); // end of file
                }
                findAllMatches();
            }

            long resultStartPosition = bufferPosition + currentPartFound;

            int resultLength;
            if (currentPartFoundIsUnicode) {
                resultLength = myLiteralByteLength;
            } else {
                resultLength = myByteFindSequenceOriginalOrLowerCase.length;
            }

            setNewStart(resultStartPosition + (myDirectionForward ? 1 : resultLength - 1));

            return new Match(true, resultStartPosition, resultLength, null);

        } catch (IOException ex) {
            return new Match(false, 0, 0, ex);
        }
    }

    private void initSearchHex(byte[] sequence) {
        myByteFindSequenceOriginalOrLowerCase = sequence;

        if (sequence.length > MAX_SEQUENCE_SIZE) {
            myByteFindSequenceOriginalOrLowerCase = new byte[MAX_SEQUENCE_SIZE];
            System.arraycopy(sequence, 0, myByteFindSequenceOriginalOrLowerCase, 0, MAX_SEQUENCE_SIZE);
        }

        myLiteralByteLength = myByteFindSequenceOriginalOrLowerCase.length;
    }

    /**
     * Get the current location being searched in the content. Approximate
     * value.
     * 
     * @return position in the content
     */
    public long getSearchPosition() {
        return bufferPosition;
    }

    private void initSearchUnicodeAscii() throws CharacterCodingException {

        if (myCaseSensitive) {
            myLiteralEncodedOriginalOrLowerCase = new String(myCharsetEncoder.encode(CharBuffer.wrap(myLiteral.toCharArray())).array());
            myLiteralEncodedAllUpperCase = myLiteralEncodedOriginalOrLowerCase;
        } else {
            myLiteralEncodedOriginalOrLowerCase = new String(myCharsetEncoder.encode(CharBuffer.wrap(myLiteral.toLowerCase().toCharArray())).array());
            myLiteralEncodedAllUpperCase = new String(myCharsetEncoder.encode(CharBuffer.wrap(myLiteral.toUpperCase().toCharArray())).array());
        }

        // everything-quoted regular expression
        StringBuilder regex = new StringBuilder("\\Q");

        // 16 bit Unicode chars
        if (myLiteralEncodedOriginalOrLowerCase.length() * 2 > MAX_SEQUENCE_SIZE) {
            myLiteralEncodedOriginalOrLowerCase = myLiteralEncodedOriginalOrLowerCase.substring(0, MAX_SEQUENCE_SIZE / 2);
        }
        myLiteralByteLength = myLiteralEncodedOriginalOrLowerCase.length() * 2;

        boolean isAsciiCompatible = true;
        byte[] tmpBytesOriginalOrLowerCase = new byte[myLiteralEncodedOriginalOrLowerCase.length()];
        byte[] tmpBytesAllUpperCase = new byte[tmpBytesOriginalOrLowerCase.length];
        char previous = '\0';
        for (int i = 0; i < myLiteralEncodedOriginalOrLowerCase.length(); ++i) {

            char aChar = myLiteralEncodedOriginalOrLowerCase.charAt(i);
            regex.append(aChar);

            if (previous == '\\' && aChar == 'E') {
                regex.append("\\\\E\\Q"); //$NON-NLS-1$
            }

            previous = aChar;

            // if (myLiteral instanceof String) {
            tmpBytesOriginalOrLowerCase[i] = myLiteralEncodedOriginalOrLowerCase.substring(i, i + 1).getBytes()[0];
            tmpBytesAllUpperCase[i] = myLiteralEncodedAllUpperCase.substring(i, i + 1).getBytes()[0];
            // } else {
            // tmpBytes[i] = (byte)aChar;
            // tmpBytesLowercase[i] = (byte)aChar;
            // if (tmpBytes[i] >= 'A' && tmpBytes[i] <= 'Z') {
            // tmpBytesLowercase[i] = (byte)(tmpBytesLowercase[i] + 32);
            // }

            if (myLiteral.charAt(i) > 255) {
//                isAsciiCompatible = false;
            }
            // }
        }
        regex.append("\\E");

        int ignoreCaseFlags = 0;
        if (!myCaseSensitive) {
            ignoreCaseFlags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }

        myPattern = Pattern.compile(regex.toString(), ignoreCaseFlags);

        if (isAsciiCompatible) {
            myByteFindSequenceOriginalOrLowerCase = tmpBytesOriginalOrLowerCase;
            myByteFindSequenceAllUpperCase = tmpBytesAllUpperCase;
        }
    }

    private ByteBuffer nextPart() throws IOException {

        long newPos = bufferPosition + byteBuffer.limit() - myLiteralByteLength + 1L;
        if (!myDirectionForward) {
            newPos = bufferPosition - MAP_SIZE + myLiteralByteLength - 1L;
        }

        if (newPos < 0L) {
            newPos = 0L;
        }

        int size = (int)Math.min(MAP_SIZE, getContentLength() - newPos);
        if (!myDirectionForward) {
            size = (int)(bufferPosition + myLiteralByteLength - 1L - newPos);
        }

        if (size < myLiteralByteLength) {
            return null;
        }

        bufferPosition = newPos;
        populatePart(size);

        return byteBuffer;
    }

    private void populatePart() throws IOException {

        int size = MAP_SIZE;
        if (!myDirectionForward) {
            size = (int)Math.min(MAP_SIZE, currentPosition);
        }

        populatePart(size);
    }

    private void populatePart(int size) throws IOException {

        if (myContent == null) {
            return;
        }

        byteBuffer = null;
        // multiple FileChannel.read(byteBuffer) leak memory, so don't reuse
        // buffer
        byteBuffer = ByteBuffer.allocate(MAP_SIZE);
        byteBuffer.limit(size);
        byteBuffer.position(0);

        myContent.get(byteBuffer, bufferPosition);

        byteBuffer.limit(byteBuffer.position());
        byteBuffer.position(0);
    }

    /**
     * Sets the case sensitiveness. The default is always case sensitive (not
     * ignore case)
     * 
     * @param beSensitive set to true will not match 'a' with 'A'
     * @throws CharacterCodingException
     */
    private void setCaseSensitive(boolean beSensitive) throws CharacterCodingException {

        if (myCaseSensitive == beSensitive) {
            return;
        }

        myCaseSensitive = beSensitive;
        if (myLiteralEncodedOriginalOrLowerCase != null) {
            initSearchUnicodeAscii();
        }
    }

    /**
     * Sets the search direction. The default search direction is always forward
     * 
     * @param goForward set to true for forward search
     */
    public void setDirectionForward(boolean goForward) {
        myDirectionForward = goForward;
    }

    /**
     * Sets new search start point in the file. Inclusive in forward finds,
     * exclusive in backward ones.
     * 
     * @param startPoint next match search will start from this point
     */
    public void setNewStart(long startPoint) {

        if (startPoint < 0L || startPoint > getContentLength()) {
            return;
        }

        currentPosition = startPoint;
        bufferPosition = startPoint;
        if (!myDirectionForward) {
            bufferPosition = startPoint - MAP_SIZE;
        }

        if (bufferPosition < 0L) {
            bufferPosition = 0L;
        }
    }

    /**
     * Stop searching. Long running searches can be stopped from another thread.
     */
    public void stopSearching() {
        stopSearching = true;
    }
}
