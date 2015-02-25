package cs455.scaling.io;

import java.net.*;

import cs455.scaling.server.*;

import java.util.concurrent.atomic.*;

import java.nio.*;
import java.nio.channels.*;

import java.util.*;

public class ThreadPoolManager
{
    private final Worker[] workers;
    private final Thread[] threads;

    private final List<SelectionKey> keys = new LinkedList<SelectionKey>();

    

    public ThreadPoolManager(int n){
	workers = new Worker[n];
	threads = new Thread[n];
	for (int i=0; i < n; ++i){
	    workers[i] = new Worker(this);
	    threads[i] = new Thread(workers[i]);
	}
    }

    public void start()
    {
	try {
	    for(int i=0; i<workers.length; i++) {
		threads[i].start();
	    }
	}
	catch (Exception e){}
	
    }

    public Task getTask()
    {
	synchronized(keys) {
	    while(keys.size() < 1) {return null;}
	    //System.out.println("A THING WAS GRABBED!");
	    SelectionKey k = keys.remove(0);
	    return new Task(k);
	    //return tasks.remove();
	}
    }

    public void add(SelectionKey k)
    {
	//System.out.println("IM ADDING THINGS!");
	synchronized(keys) {
	    if(keys.indexOf(k) >= 0) return;
	    //System.out.println("A thing was added.");
	    keys.add(k);
	}
    }

    

}
