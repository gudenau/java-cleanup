package net.gudenau.lib.cleanup;

public abstract class AutoClean implements AutoCloseable{
    private boolean closed = false;
    
    protected abstract void clean();
    
    @Override
    public final void close(){
        if(!closed){
            closed = true;
            clean();
        }
    }
}
