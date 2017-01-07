/*  UIThread.java
 *
 *  Created on Feb 16, 2009 by William Edward Woody
 */

package com.smarttech.support;

import android.os.Handler;
import android.os.Looper;

/**
 * Provides support for inserting messages into the main UI thread. This is here because the pattern repeats all over the place. The rational is to keep all activities involving UI updates and the like in the same message queue. We also only need one handler for sorting out messages, so this keeps it in one place.
 */
public class UIThread
{
//    final private static Handler gHandler = new Handler(Looper.getMainLooper());
    private static Handler gHandler = null;
    private static UIThread uithread = null;
    
    public static UIThread newInstance(){
    	if( uithread == null ){
    		uithread = new UIThread();
    	}
    	return uithread;
    }
    
    public UIThread(){
    	initializeHandler();
    }
    
    /**
     * Utility function executes the runnable in the main UI thread. I'm sort of annoyed there isn't a global to do this...
     * 
     * @param r
     */
    public void executeInUIThread(Runnable r) {
        gHandler.post(r);
    }
    
    
    public void executeInUIThread(Runnable r,long time){
    	gHandler.postDelayed(r,time);
    }
    
    /**
     * Only used by InitechClient.onCreate to register it as the activity in the UI thread that owns the current UI event loop. Call in main thread
     * 
     */
    public void initializeHandler() {
        if (gHandler == null) {
            gHandler = new Handler();
        }
    }
}
