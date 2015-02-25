package cs455.scaling.io;

import java.net.*;

import cs455.scaling.server.*;

import java.util.concurrent.atomic.*;

public class Worker implements Runnable
{
    private volatile Task task = null;

    private final ThreadPoolManager manager;

    public Worker(ThreadPoolManager m) {
	manager = m;
    }

    public boolean isBusy(){
	return (task != null);
    }


    public void run()
    {
	while(true) {
	    task = manager.getTask();
	    if(task == null) continue;
	    task.execute();
	    task = null;
	}
	
    }
}
