/*  JSONException.java
 *
 */

package com.smarttech.request.json.exception;

import java.io.IOException;

public class JSONException extends IOException
{
    private static final long serialVersionUID = -1636446046269422506L;
    String fStr;
    int fStart;
    int fPos;
    
    public JSONException(String string, int start, int pos) {
        super(string);
        fStart = start;
        fPos = pos;
    }
    
    public JSONException(String string, int start, int pos, Throwable ex) {
        super(string);
        super.initCause(ex);
        fStart = start;
        fPos = pos;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        buf.append(super.toString());
        buf.append(" [ start == ").append(fStart).append(", pos == ").append(fPos).append("]");
        return buf.toString();
    }
    
    public int getStart() {
        return fStart;
    }
    
    public int getPos() {
        return fPos;
    }
}
