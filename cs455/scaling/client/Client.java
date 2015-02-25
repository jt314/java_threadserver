package cs455.scaling.client;

import cs455.scaling.io.*;

import java.util.*;

import java.util.concurrent.atomic.*;

import java.math.*;

import java.security.MessageDigest;

public class Client implements Runnable
{
    private final List<String> validList;
    private final int R;
    private final ClientConnection cconn;
    private transient volatile boolean HCF = false; //Something bad happened.

    public Client(String a, int p, int r) throws java.io.IOException
    {
	validList = new LinkedList<String>();
	cconn = new ClientConnection(this, a, p);
	R = (r > 0) ? r : 4;
    }

    //public void start()
    //{
    //	cconn.start();
    //}

    public void run()
    {
	cconn.start(); //Start receive op
	
	while(!cconn.getHCF()) {
	    
	    byte[] spam = generateSpam();
	    String hash = computeHash(spam);
	    try {
		cconn.send(spam);
		validList.add(hash);
		System.out.println("Hash "+hash+" Added to system.");
		Thread.sleep(1000/R);
	    }
	    catch(java.io.IOException e) {
		e.printStackTrace();
	    }
	    catch(InterruptedException e) {
		e.printStackTrace();
		// something happens here.
	    }
	}
	System.out.println("OOPS I DIED");
	HCF = true;
    }

    public boolean getHCF()
    {
	return (HCF == true);
    }

    public String computeHash (byte[] data) { 
	MessageDigest digest;
	try {
	    digest = MessageDigest.getInstance("SHA1");
	}
	catch(Exception e) {
	    System.out.println("SHA1 DOES NOT EXIST!");
	    System.exit(-1);
	    return null;
	}
	byte[] hash = digest.digest(data); 
	BigInteger hashInt = new BigInteger(1, hash); 
 
	String returnme = hashInt.toString(16);

	while(returnme.length() < 40)
	    returnme = "0" + returnme;
	
	return returnme;
    }

    public synchronized void removeHash(String hash)
    {
	boolean b = validList.remove(hash);
	if(!b) {
	    System.out.println("Invalid hash " +hash+ " encountered!");
	    return;
	}
	System.out.println("Hash " +hash+ " Removed from system.");
    }

    public byte[] generateSpam()
    {
	byte[] b = new byte[8192];
	new Random().nextBytes(b);
	return b;
    }

    public static void USAGE()
    {
	System.out.println("Usage: cs455.scaling.client.Client <server-host> <server-port> <message-rate>");
    }

    public static void main(String[] args)
    {
	if(args.length != 3) {
	    Client.USAGE();
	    return;
	}
	String host = args[0];
	int port;
	int rate;
	try {
	    port = Integer.parseInt(args[1]);
	    rate = Integer.parseInt(args[2]);
	}
	catch (Exception e) {
	    Client.USAGE();
	    return;
	}

	//Strp 1: Attain connection to registry
	Client cli;
	try {
	    cli = new Client(host,port, rate);
	}
	catch(Exception e) {
	    System.out.println("Error connecting to server. Exiting.");
	    return;
	}
	

	//Step 2: Spawn listening threads
	Thread listenthread; 
	try {
	    listenthread = new Thread(cli);
	}
	catch(Exception e){
	    System.out.println("Error creating Serversocket. Exiting.");
	    return;
	}
	listenthread.start();

	//while(!cli.getHCF()){/* do nothing */}
    }

}
