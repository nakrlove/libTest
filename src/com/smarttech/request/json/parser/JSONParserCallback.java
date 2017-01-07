package com.smarttech.request.json.parser;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Callback interfaces
 * 
 * @author woody
 * 
 */
public interface JSONParserCallback
{
    /**
     * Creates a new JSON object. This is called at the start of parsing a new JSON object, bracketed by {...}
     * 
     * @param keyList
     * @return Object
     * @throws IOException
     */
    Object objectCreate(LinkedList<String> keyList) throws IOException;
    
    /**
     * Called when a new value is added to the created object.
     * 
     * @param keyList
     * @param obj
     * @param str
     * @param value
     * @throws IOException
     */
    void objectAddValue(LinkedList<String> keyList, Object obj, String str, Object value) throws IOException;
    
    /**
     * Called when the object is finished being constructed. This gives the caller code the opportunity to do whatever specific per-object initialization, or even replace the object returned by objectCreate with a new object. (For example, objectCreate
     * 
     * @param keyList
     * @param obj
     * @return Object
     */
    Object objectFinish(LinkedList<String> keyList, Object obj);
    
    /**
     * Create an array object
     * 
     * @param keyList
     * @return Object
     * @throws IOException
     */
    Object arrayCreate(LinkedList<String> keyList) throws IOException;
    
    /**
     * Add a new object to the end of the array
     * 
     * @param keyList
     * @param array
     * @param value
     * @throws IOException
     */
    void arrayAddValue(LinkedList<String> keyList, Object array, Object value) throws IOException;
    
    /**
     * Called when the object is finished being constructed. This gives the caller the opportunity to change the object
     * 
     * @param keyList
     * @param obj
     * @return Object
     */
    Object arrayFinish(LinkedList<String> keyList, Object obj);
}