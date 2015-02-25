package cs455.scaling.server;

import cs455.scaling.io.*;

import java.util.*;

import java.util.concurrent.atomic.*;

import java.math.*;

import java.net.*;
import java.nio.*;
import java.nio.channels.*;

import java.io.IOException;

import java.security.MessageDigest;

import java.nio.channels.spi.SelectorProvider;

public class Server implements Runnable
{
    private final InetAddress HOST = null;
    private final int PORT;

    private final ThreadPoolManager POOL;

    private ServerSocketChannel SERVCHAN;
    private Selector SELECT;


    public Server(int port, int pool) throws IOException
    {
	PORT = port;
	POOL = new ThreadPoolManager((pool > 0)? pool : 10);
	SELECT = initSelector();
	POOL.start();
    }

    public void run()
    {
	

	while (true) {
	    try {
		// Wait for an event one of the registered channels
		if(SELECT.select() == 0) {

		    //System.out.println("NOPE!");
		    continue;
		}

		// Iterate over the set of keys for which events are available
		Iterator selectedKeys = SELECT.selectedKeys().iterator();
		while (selectedKeys.hasNext()) {
		    SelectionKey key = (SelectionKey) selectedKeys.next();
		    selectedKeys.remove();

		    if (!key.isValid()) {
			continue;
		    }

		    // Check what event is available and deal with it
		    if (key.isAcceptable()) {
			accept(key);
		    }
		    else if (key.isReadable()) {
			//System.out.println("READING!");
			POOL.add(key);
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    private void accept(SelectionKey key) throws IOException {
	ServerSocketChannel servSocket = (ServerSocketChannel) key.channel();
	SocketChannel channel = servSocket.accept();
	//client info contains information about port and host, as well as 
	//pending data for that client
	System.out.println("Accepting incoming connection ");
	channel.configureBlocking(false);
	channel.register(SELECT, SelectionKey.OP_READ);
    }


    private Selector initSelector() throws IOException {
	// Create a new selector
	Selector socketSelector = SelectorProvider.provider().openSelector();

	// Create a new non-blocking server socket channel
	SERVCHAN = ServerSocketChannel.open();
	SERVCHAN.configureBlocking(false);

	// Bind the server socket to the specified address and port
	InetSocketAddress isa = new InetSocketAddress(HOST, PORT);
	SERVCHAN.socket().bind(isa);

	// Register the server socket channel, indicating an interest in 
	// accepting new connections
	SERVCHAN.register(socketSelector, SelectionKey.OP_ACCEPT);

	return socketSelector;
    }


    public static void USAGE()
    {
	System.out.println("Usage: cs455.scaling.server.Server <server-port> <threadpoolsize>");
    }

    public static void main(String[] args)
    {
	if(args.length != 2) {
	    Server.USAGE();
	    return;
	}
	int port;
	int pool;
	try {
	    port = Integer.parseInt(args[0]);
	    pool = Integer.parseInt(args[1]);
	}
	catch (Exception e) {
	    Server.USAGE();
	    return;
	}
	
	try{
	    Server s = new Server(port, pool);
	    Thread t = new Thread(s);
	    t.start();
	}
	catch(Exception e) { 
	    System.out.println("ERROR: THE SERVER FAILED TO START.");
	}

    }

}
