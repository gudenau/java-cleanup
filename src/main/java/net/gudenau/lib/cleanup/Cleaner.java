package net.gudenau.lib.cleanup;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.gudenau.lib.annotation.NonNull;

/**
 * A lightweight way to implement automatic cleanup
 * when an object is no longer reachable.
 * */
public class Cleaner extends PhantomReference<Object>{
    private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
    // Because it won't work without
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final List<Cleaner> cleaners = new ArrayList<>();
    private static final Thread cleanerThread;
    private static final Object $LOCK$ = new Object[0];
    
    private volatile static boolean shouldRun = true;
    
    static{
        cleanerThread = new Thread(Cleaner::thread);
        cleanerThread.setName("Cleaner Thread");
        cleanerThread.setDaemon(true);
        cleanerThread.setPriority(Thread.MIN_PRIORITY);
        cleanerThread.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            shouldRun = false;
            cleanerThread.interrupt();
        }));
    }
    
    private static void thread(){
        while(shouldRun){
            Cleaner cleaner;
            try{
                cleaner = (Cleaner)referenceQueue.remove();
            }catch(InterruptedException ignored){
                continue;
            }
            cleaner.clean();
            synchronized($LOCK$){
                cleaners.remove(cleaner);
            }
        }
    }
    
    /**
     * Creates a new {@link net.gudenau.lib.cleanup.Cleaner Cleaner}.
     *
     * Make sure there are no references to the object inside the callback!
     *
     * @param object The object to watch
     * @param callback The callback to clean the object
     * */
    public static Cleaner addCleaner(@NonNull Object object, @NonNull Runnable callback){
        synchronized($LOCK$){
            Cleaner cleaner = new Cleaner(object, callback);
            cleaners.add(cleaner);
            return cleaner;
        }
    }
    
    private final Runnable callback;
    private volatile boolean clean = false;
    
    private Cleaner(Object reference, Runnable callback){
        super(reference, referenceQueue);
        this.callback = callback;
    }
    
    /**
     * Invoke the cleaner callback, if it has yet to be invoked.
     * */
    public void clean(){
        synchronized(this){
            if(!clean){
                clean = true;
                callback.run();
                synchronized($LOCK$){
                    cleaners.remove(this);
                }
            }
        }
    }
}
