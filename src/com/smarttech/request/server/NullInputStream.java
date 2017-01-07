package com.smarttech.request.server;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an empty input stream
 */
public class NullInputStream extends InputStream
{
    
    @Override
    public int available() throws IOException {
        return 0;
    }
    
    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        return 0;
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        return 0;
    }
    
    @Override
    public int read() throws IOException {
        return -1;
    }
}
