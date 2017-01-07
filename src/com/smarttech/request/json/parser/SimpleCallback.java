/*  SimpleCallback.java
 *
 *  Created on May 21, 2009 by William Edward Woody
 */

package com.smarttech.request.json.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Simple callback class uses HashMap and ArrayList in order to store objects and arrays. This is used internally when the JSONParserEngine is not provided a callback.
 */
class SimpleCallback implements JSONParserCallback
{
    @SuppressWarnings("unchecked")
    public void arrayAddValue(LinkedList<String> keyList, Object array, Object value) throws IOException {
        /**
         * Note: we get a byte buffer in lieu of a string; this handles converting the byte buffer into a string for storage.
         */
        if (value instanceof JSONParseEngine.ByteBuffer) {
            value = value.toString();
        }
        ((ArrayList) array).add(value);
    }
    
    @SuppressWarnings("unchecked")
    public Object arrayCreate(LinkedList<String> keyList) throws IOException {
        return new ArrayList();
    }
    
    public Object arrayFinish(LinkedList<String> keyList, Object obj) {
        return obj;
    }
    
    @SuppressWarnings("unchecked")
    public void objectAddValue(LinkedList<String> keyList, Object obj, String str, Object value) throws IOException {
        /**
         * Note: we get a byte buffer in lieu of a string; this handles converting the byte buffer into a string for storage.
         */
        if (value instanceof JSONParseEngine.ByteBuffer) {
            value = value.toString();
        }
        ((HashMap) obj).put(str, value);
    }
    
    @SuppressWarnings("unchecked")
    public Object objectCreate(LinkedList<String> keyList) throws IOException {
        return new HashMap();
    }
    
    public Object objectFinish(LinkedList<String> keyList, Object obj) {
        return obj;
    }
}
