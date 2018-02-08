# Cleanup
A small library that implements a cleanup callback for when an object is no longer refrenced and the GarbageCollector runs.

[![Build Status](https://travis-ci.org/gudenau/java-cleanup.svg?branch=master)](https://travis-ci.org/gudenau/java-cleanup)

## Example
```Java
public class TestClass{
    publiic TestClass(){
        long handle = allocateNativeResource();
        Cleaner.addCleaner(this, ()->freeNativeResource(handle));
    }

    private native long allocateNativeResource();
    private native void freeNativeResource(long handle);
}
```
