package com.smarttech.request.json.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeMap;

import com.smarttech.request.json.exception.JSONException;





/**
 * This is a replacement of the built-in JSON parser, which is able to handle parsing directly from a byte stream.
 */
public class JSONParseEngine
{
    private JSONParserCallback fCallback;
    
    private InputStream fInput;
    private int fPos;
    private int fPushByte;
    
    private DynamicByteBuffer fBuffer;
    private TreeMap<ByteBuffer, String> fKeys;
    
    private LinkedList<String> fKeyList;
    
    private int fComplexity;
    
    /**
     * Static byte buffer represents a fixed byte buffer. This is returned instead of a string; you can use toString() to convert this back to a string object in a way which uses a very small memory footprint.
     */
    public class ByteBuffer
    {
        protected byte[] fData;
        
        protected ByteBuffer() {
        }
        
        ByteBuffer(ByteBuffer src) {
            fData = new byte[src.getLength()];
            System.arraycopy(src.getByteArray(), 0, fData, 0, fData.length);
        }
        
        public byte[] getByteArray() {
            return fData;
        }
        
        public int getLength() {
            return fData.length;
        }
        
        public String toString() {
            return seqToString(this);
        }
    }
    
    /**
     * Dynamic version of the byte array buffer, for internal use only.
     */
    class DynamicByteBuffer extends ByteBuffer
    {
        private int fLength;
        
        DynamicByteBuffer() {
            fData = new byte[256];
            fLength = 0;
        }
        
        void reset() {
            fLength = 0;
        }
        
        void append(byte data) {
            if ((fData == null) || (fLength >= fData.length)) {
                byte[] newBuffer = new byte[(fData.length * 3) / 2];
                System.arraycopy(fData, 0, newBuffer, 0, fData.length);
                fData = newBuffer;
            }
            fData[fLength++] = data;
        }
        
        public int getLength() {
            return fLength;
        }
        
        public boolean compareString(CharSequence string) {
            int i, len;
            
            len = string.length();
            if (fLength != len)
                return false;
            for (i = 0; i < len; ++i) {
                if (string.charAt(i) != fData[i])
                    return false;
            }
            return true;
        }
    }
    
    /**
     * Internal compare class used by TreeMap to compare two byte sequences. This allows me to quickly test if a byte sequence has already been converted into a string without unnecessary allocations
     */
    private static class CompareByteSequence implements Comparator<ByteBuffer>
    {
        public int compare(ByteBuffer a, ByteBuffer b) {
            int i;
            int lena = a.getLength();
            int lenb = b.getLength();
            byte[] aval = a.getByteArray();
            byte[] bval = b.getByteArray();
            
            int len = lena;
            if (len > lenb)
                len = lenb;
            for (i = 0; i < len; ++i) {
                if (aval[i] < bval[i])
                    return -1;
                if (aval[i] > bval[i])
                    return 1;
            }
            if (lena < lenb)
                return -1;
            if (lena > lenb)
                return 1;
            return 0;
        }
    }
    
    /**
     * Construct the JSON parser engine for processing
     * 
     * @param callback
     *            The event callback interface to use during parsing.
     */
    public JSONParseEngine(JSONParserCallback callback, InputStream is) {
        fCallback = callback;
        fInput = is;
        fPos = 0;
        fPushByte = -2;
        fBuffer = new DynamicByteBuffer();
        fKeys = new TreeMap<ByteBuffer, String>(new CompareByteSequence());
        fKeyList = new LinkedList<String>();
    }
    
    /**
     * Construct a JSON parser engine with an internal callback mechanism that converts JSON objects into HashMap objects, and JSON Arrays into ArrayList objects. Also, strings are converted into regular strings.
     * 
     * @param is
     */
    public JSONParseEngine(InputStream is) {
        this(new SimpleCallback(), is);
    }
    
    /**
     * Read the next byte
     * 
     * @return
     * @throws IOException
     */
    private int getNextByte() throws IOException {
        ++fPos;
        if (fPushByte != -2) {
            int tmp = fPushByte;
            fPushByte = -2;
            return tmp;
        }
        return fInput.read();
    }
    
    /**
     * Push back the read byte
     * 
     * @param b
     */
    private void pushBackByte(int b) {
        --fPos;
        fPushByte = b;
    }
    
    /**
     * Skip white space
     * 
     * @throws IOException
     */
    private void skipSpace() throws IOException {
        int c;
        while (-1 != (c = getNextByte())) {
            if ((c != ' ') && (c != '\t') && (c != '\n') && (c != '\r'))
                break;
        }
        pushBackByte(c);
    }
    
    /**
     * Convert a string sequence to a string. This operates by maintaining a buffer of all the string sequences and the converted UTF8 strings, and does it in a way which only allocates memory if a string doesn't already exist.
     * 
     * @return
     * @throws IOException
     */
    private String seqToString(ByteBuffer byteBuffer) {
        String str = fKeys.get(byteBuffer);
        if (str == null) {
            ByteBuffer b = new ByteBuffer(byteBuffer);
            try {
                str = new String(b.getByteArray(), 0, b.getLength(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                /*
                 * Unicode encryption error. Decrypt with byte markers for all high-byte character encodings.
                 */
                StringBuffer sbuf = new StringBuffer();
                byte[] data = b.getByteArray();
                int i, len = b.getLength();
                for (i = 0; i < len; ++i) {
                    byte c = data[i];
                    if (c <= 0) {
                        sbuf.append("\\").append((0x00FF) & c);
                    } else if (c == '\\') {
                        sbuf.append("\\\\");
                    } else {
                        sbuf.append((char) c);
                    }
                }
                str = sbuf.toString();
            }
            fKeys.put(b, str);
        }
        return str;
    }
    
    /**
     * External interface to parse
     * @return Object
     * @throws IOException
     */
    
    public Object parse() throws IOException {
        return parseValue();
    }
    
    /**
     * Returns metric of the complexity of the object we just parsed. This is the number of calls into parseValue(), which determines what object is being parsed. Thus, { "foo": "bar" } would be 2: one for the container object and one for the contained field.
     * 
     * @return Overall object complexity
     */
    public int getComplexity() {
        return fComplexity;
    }
    
    /**
     * Parse the input stream, calling into the supplied callback engine, returning a parsed object. The parsed object is optionally returned by the callback stuff
     * 
     * @return The parsed object.
     */
    private Object parseValue() throws IOException {
        int c;
        int start = fPos;
        
        ++fComplexity;
        
        try {
            
            skipSpace();
            c = getNextByte();
            
            if (c == '{') {
                pushBackByte(c);
                return parseObject();
            } else if (c == '"') {
                pushBackByte(c);
                parseString();
                return fBuffer;
            } else if ((c == '-') || ((c >= '0') && (c <= '9'))) {
                pushBackByte(c);
                return parseNumber();
            } else if (c == '[') {
                pushBackByte(c);
                return parseArray();
            } else {
                /*
                 * Pull token. Must be 'true', 'false' or 'null'
                 */

                fBuffer.reset();
                while (Character.isLetter(c) || (c >= 0x80)) {
                    fBuffer.append((byte) c);
                    c = getNextByte();
                }
                pushBackByte(c);
                
                if (fBuffer.compareString("true"))
                    return new Boolean(true);
                else if (fBuffer.compareString("false"))
                    return new Boolean(false);
                else if (fBuffer.compareString("null"))
                    return null;
                else
                    throw new JSONException("Unkonwn token: " + fBuffer.toString(), start, fPos);
            }
        } catch (Exception ex) {
            throw new JSONException("Embedded exception in value3", start, fPos, ex);
        }
    }
    
    private Object parseArray() throws IOException {
        int c;
        int start = fPos;
        Object array = fCallback.arrayCreate(fKeyList);
        
        try {
            skipSpace();
            c = getNextByte();
            if (c != '[') {
                throw new JSONException("Expected '[' at character position " + (fPos - 1), start, fPos);
            }
            
            skipSpace();
            c = getNextByte();
            if (c != ']') {
                pushBackByte(c);
                
                for (;;) {
                    skipSpace();
                    
                    Object val = parseValue();
                    
                    if (array != null) {
                        fCallback.arrayAddValue(fKeyList, array, val);
                    }
                    skipSpace();
                    c = getNextByte();
                    if (c == ']')
                        break;
                    if (c != ',') {
                        throw new JSONException("Expected ',' in array at character position " + (fPos - 1), start, fPos);
                    }
                }
            }
            
            if (array == null)
                return null;
            return fCallback.arrayFinish(fKeyList, array);
        } catch (Exception ex) {
            throw new JSONException("Embedded exception in value1", start, fPos, ex);
        }
    }
    
    private Object parseNumber() throws IOException {
        boolean isInt = true;
        int c;
        int start = fPos;
        
        skipSpace();
        fBuffer.reset();
        
        /*
         * Get first character
         */

        c = getNextByte();
        if (c == '-') {
            fBuffer.append((byte) c);
            c = getNextByte();
        }
        if (c == '0') {
            /* 0 sits alone */
            fBuffer.append((byte) c);
            c = getNextByte();
        } else if ((c >= '1') || (c <= '9')) {
            /* After 1-9, get digits 0-9 */
            while ((c >= '0') && (c <= '9')) {
                fBuffer.append((byte) c);
                c = getNextByte();
                
            }
        } else {
            throw new JSONException("Expected integer digit at character position " + (fPos - 1), start, fPos);
        }
        
        /*
         * At this point we expect an optional '.' (digits) sequence
         */

        if (c == '.') {
            isInt = false;
            do {
                fBuffer.append((byte) c);
                c = getNextByte();
            } while ((c >= '0') && (c <= '9'));
        }
        
        /*
         * Now we expect an optional [e|E]([+|-])digits*
         */

        if ((c == 'e') || (c == 'E')) {
            isInt = false;
            fBuffer.append((byte) c);
            c = getNextByte();
            if ((c == '+') || (c == '-')) {
                fBuffer.append((byte) c);
                c = getNextByte();
            }
            
            if ((c < '0') || (c > '9')) {
                throw new JSONException("Expected exponent digit at character position " + (fPos - 1), start, fPos);
            }
            while ((c >= '0') && (c <= '9')) {
                fBuffer.append((byte) c);
                c = getNextByte();
            }
        }
        pushBackByte(c);
        
        /*
         * When we get here we've parsed the whole thing. Return the number
         */
        Double g = new Double(fBuffer.toString());
        // if ((g>2147483648F || g<-2147483648F) && isInt==true) {
        if ((g > Integer.MAX_VALUE || g < Integer.MIN_VALUE) && isInt == true) {
            return new BigInteger(fBuffer.toString());
            
        }
        
        /*
         * 
         * if (isInt) { return new Integer(fBuffer.toString()); } else { return new Double(fBuffer.toString()); }
         */
        if (isInt) {
            return new Integer(fBuffer.toString());
        }
        
        return g;
        
    }
    
    private int parseHexByte() throws IOException {
        int c;
        int start = fPos;
        
        c = getNextByte();
        if ((c >= '0') && (c <= '9'))
            return c - '0';
        if ((c >= 'a') && (c <= 'f'))
            return c - 'a' + 10;
        if ((c >= 'A') && (c <= 'F'))
            return c - 'A' + 10;
        
        throw new JSONException("Expected hex character at position " + (fPos - 1), start, fPos);
    }
    
    /**
     * Parse the string. Takes advantage of the UTF8 character set design
     * 
     * @return
     */
    private void parseString() throws IOException {
        int c;
        int start = fPos;
        
        fBuffer.reset();
        skipSpace();
        c = getNextByte();
        if (c != '"') {
            throw new JSONException("Expected '\"', got '" + c + "' at character position " + (fPos - 1), start, fPos);
        }
        for (;;) {
            c = getNextByte();
            
            if (c == '\"') {
                break;
            } else if (c == '\\') {
                // Escape character
                c = getNextByte();
                if (c == '\"') {
                    fBuffer.append((byte) '"');
                } else if (c == '\\') {
                    fBuffer.append((byte) '\\');
                } else if (c == '/') {
                    fBuffer.append((byte) '/');
                } else if (c == 'b') {
                    fBuffer.append((byte) '\b');
                } else if (c == 'f') {
                    fBuffer.append((byte) '\f');
                } else if (c == 'n') {
                    fBuffer.append((byte) '\n');
                } else if (c == 'r') {
                    fBuffer.append((byte) '\r');
                } else if (c == 't') {
                    fBuffer.append((byte) '\t');
                } else if (c == 'u') {
                    /* 4 hex digits */
                    int i = parseHexByte();
                    i = (i << 4) | parseHexByte();
                    i = (i << 4) | parseHexByte();
                    i = (i << 4) | parseHexByte();
                    
                    /*
                     * Here's where this gets interesting. We need to convert from UTF-16 to UTF-8 encoding, so we can convert back...
                     */
                    if (i < 0x80) {
                        fBuffer.append((byte) i);
                    } else if (i < 0x7FF) {
                        fBuffer.append((byte) (0xC0 | (i >> 6)));
                        fBuffer.append((byte) (0x80 | (0x3F & i)));
                    } else {
                        fBuffer.append((byte) (0xE0 | (i >> 12)));
                        fBuffer.append((byte) (0x80 | (0x3F & (i >> 6))));
                        fBuffer.append((byte) (0x80 | (0x3F & i)));
                    }
                } else {
                    throw new JSONException("Illegal escape character found at character position " + (fPos - 2), start, fPos);
                }
            } else {
                fBuffer.append((byte) c);
            }
        }
        
    }
    
    /**
     * Parse an object
     * 
     * @return
     */
    private Object parseObject() throws IOException {
        Object obj = fCallback.objectCreate(fKeyList);
        int start = fPos;
        int c;
        
        try {
            skipSpace();
            c = getNextByte();
            if (c != '{') {
                throw new JSONException("Expected '{' at character position " + (fPos - 1), start, fPos);
            }
            skipSpace();
            
            c = getNextByte();
            if (c != '}') {
                /* Read the fields in this object */
                pushBackByte(c);
                for (;;) {
                    /* Read key */
                    parseString();
                    String str = fBuffer.toString();
                    fKeyList.addLast(str);
                    
                    /* Read colon separator */
                    skipSpace();
                    c = getNextByte();
                    if (c != ':') {
                        throw new JSONException("Expected ':' at character position " + (fPos - 1), start, fPos);
                    }
                    
                    /* Read value */
                    Object val = parseValue();
                    if (obj != null) {
                        fCallback.objectAddValue(fKeyList, obj, str, val);
                    }
                    
                    fKeyList.removeLast();
                    
                    /* Read separator or terminus */
                    skipSpace();
                    c = getNextByte();
                    if (c == ',')
                        continue;
                    if (c == '}')
                        break;
                    throw new JSONException("Expected ',' at character position " + (fPos - 1), start, fPos);
                }
            }
            if (obj == null)
                return null;
            return fCallback.objectFinish(fKeyList, obj);
        } catch (Exception ex) {
            throw new JSONException("Embedded exception in object", start, fPos, ex);
        }
    }
}
