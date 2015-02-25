package cs455.scaling.io;

import java.net.*;

import cs455.scaling.server.*;

import java.nio.*;
import java.nio.channels.*;

import java.math.*;

import java.util.*;

import java.security.*;

import java.util.concurrent.atomic.*;

import java.io.IOException;

public class Task
{
    private final SelectionKey key;

    public Task(SelectionKey k){
	key = k;
    }

    public void execute()
    {
	try{
	    byte[] msg = read(key);
	    if(msg == null) throw new Exception("Read failed.");
	    String hash = computeHash(msg);
	    System.out.println("Received hash " +hash + " from " + ((SocketChannel)key.channel()).getRemoteAddress());
	    byte[] sendme = marshalHash(hash);
	    write(key,sendme);
	    System.out.println("Sending Hash "+hash);
	}
	catch (Exception e){
	    key.interestOps(SelectionKey.OP_READ);
	    e.printStackTrace();
	}
	
    }

    
    private byte[] read(SelectionKey key) throws IOException {
	SocketChannel channel = (SocketChannel) key.channel();
	ByteBuffer buffer = ByteBuffer.allocate(8192);
	int read = 0;
	try {
	    while (buffer.hasRemaining() && read != -1) {
		read = channel.read(buffer);
	    }
	} catch (IOException e) {
	    /* Abnormal termination */
	    key.cancel();
	    channel.close();
	    return null;
	}
	if (read == -1) {
	    /* Connection was terminated by the client. */
	    key.cancel();
	    channel.close();
	    return null;
	}
	buffer.flip();
	byte[] bufferBytes = new byte[8192];
	buffer.get(bufferBytes);
	//client.getPendingWriteList().add(computeHash(bufferBytes));
	key.interestOps(SelectionKey.OP_WRITE);
	return bufferBytes;
    }
    

    private void write(SelectionKey key, byte[] sendme) throws IOException {
	SocketChannel channel = (SocketChannel) key.channel();
	/* Get a list of pending writes; write out on the channel. */
	ByteBuffer buffer = ByteBuffer.wrap(sendme); //WRITE THIS.
	channel.write(buffer);
	key.interestOps(SelectionKey.OP_READ);
    }

    private byte[] marshalHash(String str)
    {
	java.io.ByteArrayOutputStream innerstr = new java.io.ByteArrayOutputStream();
	java.io.DataOutputStream outerstr = new java.io.DataOutputStream(innerstr);
	try{
	    outerstr.writeUTF(str);
	    return innerstr.toByteArray();
	}
	catch(Exception e){
	    return new byte[40];
	}
    }

    private String computeHash (byte[] data) { 
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

}
