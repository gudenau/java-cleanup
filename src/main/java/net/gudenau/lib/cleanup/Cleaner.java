package net.gudenau.lib.cleanup;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
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
    private static final List<Cleaner> cleaners = new LinkedList<>();
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
            cleaner.callback.run();
            synchronized($LOCK$){
                cleaners.remove(cleaner);
            }
        }
    }
    
    public static void addCleaner(@NonNull Object object, @NonNull Runnable callback){
        synchronized($LOCK$){
            cleaners.add(new Cleaner(object, callback));
        }
    }
    
    private final Runnable callback;
    
    private Cleaner(Object reference, Runnable callback){
        super(reference, referenceQueue);
        this.callback = callback;
    }
}
