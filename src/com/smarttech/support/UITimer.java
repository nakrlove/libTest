/*  UITimer.java
 *
 *  Created on Mar 2, 2009 by William Edward Woody
 */

package com.smarttech.support;

import java.util.TreeSet;


/**
 * The fundamental problem with using the java.util.timer package for handling periodic UI events is that there is no 'link' or throttling depending on how the runnable objects tossed into the UI thread are processed. Thus, if you set up a timer to fire 20 times a second, and the UI thread only exeuctes at 18hz, you wind up with a backlog of queued events.<br>
 * <br>
 * 
 * This class is designed to resolve this problem. This is a private implementation of a Timer-like class but will throttle execution based on the completion of the runnable inserted into the UI thread. This is intended only for high-frequency repeating items, such as animations, and will always assure that no more than one runnable event is inserted into the event queue of the main UI thread.<br>
 * <br>
 * 
 * Internally each item in the event queue is handled with a two-state wait: the first wait delays until an item expires. The second waits until the item completes executing.
 */
public class UITimer
{
    private static UITimer gTimer;
    private TreeSet<Task> fQueue = new TreeSet<Task>();
    private boolean fExecute;
    
    /**
     * Internal task which handles the timer code Comment
     */
    public class Task implements Runnable, Comparable<Task>
    {
        private Runnable fTask;
        private long fExpires;
        private boolean fRepeat;
        private long fRepeatTime;
        
        Task(Runnable task, long expires, boolean repeat, long repeatTime) {
            fTask = task;
            fExpires = expires + System.currentTimeMillis();
            fRepeat = repeat;
            fRepeatTime = repeatTime;
        }
        
        public void run() {
            try {
                fTask.run();
            } finally {
                synchronized (UITimer.this) {
                    if (fRepeat) {
                        long t = System.currentTimeMillis() + 10; // 1/100 sec
                        fExpires += fRepeatTime;
                        if (fExpires < t)
                            fExpires = t; // always force a requeue delay
                        fQueue.add(this);
                    }
                    fExecute = false;
                    UITimer.this.notifyAll();
                }
            }
        }
        
        public int compareTo(Task another) {
            if (fExpires < another.fExpires)
                return -1;
            if (fExpires > another.fExpires)
                return 1;
            return 0;
        }
        
        /**
         * Cancel execution of this task. Does nothing if the task is not in the task queue
         */
        public void cancel() {
            synchronized (UITimer.this) {
                fRepeat = false; // in case we are executing; prevent requeue
                fQueue.remove(this); // if we are not executing, remove from queue
                UITimer.this.notifyAll(); // trigger a recalculation of expire time
            }
        }
    }
    
    /**
     * Internal thread handles timing of items.
     */
    private class MyThread extends Thread
    {
        public void run() {
            synchronized (UITimer.this) {
                for (;;) {
                    /*
                     * Step 1: Find the next thing to expire, and set my wait time
                     */
                    if (fQueue.isEmpty()) {
                        /*
                         * Nothing to execute.
                         */
                        try {
                            UITimer.this.wait();
                        } catch (InterruptedException e) {
                        }
                    } else {
                        /*
                         * There is an item on my queue. Figure out how long I have to wait until it is active, then set up a wait.
                         */
                        Task item = fQueue.first();
                        long time = item.fExpires - System.currentTimeMillis();
                        if (time <= 0) {
                            time = 1; // guarantee a minimum wait time.
                        }
                        try {
                            UITimer.this.wait(time);
                        } catch (InterruptedException e) {
                        }
                    }
                    
                    /*
                     * Step 2: Determine if we have a frontmost item, and if it expired. If we do, then execute. Note that we can wake up prior to the item expiring, if, for example we inserted a new item in the queue or canceled a queue item. When this happens the task is not executed, but we drop back to step 1 where we re-calculate my sleep time.
                     */

                    if (!fQueue.isEmpty()) {
                        /*
                         * Get the first item and execute it. The task itself will execute in the main UI thread. This thread will then wait until the item signals it completed by clearing the fExecute flag. That will have the net result of throttling repeating tasks so we don't flood with events faster than the events can drain.
                         */
                        Task item = fQueue.first();
                        long time = System.currentTimeMillis();
                        if (time >= item.fExpires) {
                            fQueue.remove(item);
                            
                            fExecute = true;
                            UIThread.newInstance().executeInUIThread(item);
                            while (fExecute) {
                                try {
                                    UITimer.this.wait();
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Internal construction of my UITimer thread and queue.
     */
    private UITimer() {
        MyThread thread = new MyThread();
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Get the timer shared instance
     * 
     * @return UITimer
     */
    public static UITimer sharedInstance() {
        if (gTimer == null) {
            gTimer = new UITimer();
        }
        return gTimer;
    }
    
    /**
     * Enqueue a task for execution after the expiration time
     * 
     * @param expires
     * @param task
     */
    public synchronized Task startOneShot(long expires, Runnable task) {
        Task t = new Task(task, expires, false, 0);
        fQueue.add(t);
        notifyAll();
        return t;
    }
    
    /**
     * Enqueue a task for exeuction as a repeating task
     * @param repeat
     * @param task
     */
    public synchronized Task startRepeating(long repeat, Runnable task) {
        Task t = new Task(task, repeat, true, repeat);
        fQueue.add(t);
        notifyAll();
        return t;
    }
}
