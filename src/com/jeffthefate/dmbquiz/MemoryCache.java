package com.jeffthefate.dmbquiz;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class MemoryCache {

    private Map<Integer, BitmapDrawableEx> cache=Collections.synchronizedMap(
            new LinkedHashMap<Integer, BitmapDrawableEx>(2,1.5f,true));//Last argument true for LRU ordering
    private long size=0;//current allocated size
    private long limit=1000000;//max memory in bytes

    public MemoryCache(){
        //use 25% of available heap size
        setLimit(Runtime.getRuntime().maxMemory()/8);
    }
    
    public void setLimit(long new_limit){
        limit=new_limit;
    }

    public BitmapDrawableEx get(int id){
        Log.i(Constants.LOG_TAG, "cache: " + cache.size());
        try{
            if(!cache.containsKey(id))
                return null;
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            return cache.get(id);
        }catch(NullPointerException ex){
            return null;
        }
    }

    public void put(int id, BitmapDrawableEx drawable){
        try {
            if (cache.containsKey(id))
                size -= getSizeInBytes(cache.get(id));
            cache.put(id, drawable);
            drawable.setIsCached(true);
            size += getSizeInBytes(drawable);
            checkSize();
        } catch (Throwable th) {
            th.printStackTrace();
        }
        Log.d(Constants.LOG_TAG, "cache: " + cache.size());
    }
    
    private void checkSize() {
        if(size>limit){
            Log.d(Constants.LOG_TAG, "size: " + size);
            Log.d(Constants.LOG_TAG, "limit: " + limit);
            Iterator<Entry<Integer, BitmapDrawableEx>> iter=cache.entrySet().iterator();//least recently accessed item will be the first one iterated  
            while(iter.hasNext()){
                Entry<Integer, BitmapDrawableEx> entry=iter.next();
                size-=getSizeInBytes(entry.getValue());
                //((BitmapDrawable)entry.getValue()).getBitmap().recycle();
                iter.remove();
                ((BitmapDrawableEx)(entry.getValue())).setIsCached(false);
                if(size<=limit)
                    break;
            }
        }
    }

    public void clear() {
        Iterator<Entry<Integer, BitmapDrawableEx>> iter=cache.entrySet().iterator();//least recently accessed item will be the first one iterated  
        while(iter.hasNext()){
            Entry<Integer, BitmapDrawableEx> entry=iter.next();
            iter.remove();
            ((BitmapDrawableEx)(entry.getValue())).setIsCached(false);
        }
        cache.clear();
        Log.e(Constants.LOG_TAG, "cache cleared");
    }

    long getSizeInBytes(Drawable drawable) {
        if(drawable==null)
            return 0;
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}