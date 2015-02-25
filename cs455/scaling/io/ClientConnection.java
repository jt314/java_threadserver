package cs455.scaling.io;

import java.net.*;

import cs455.scaling.client.*;

import java.util.concurrent.atomic.*;

public class ClientConnection implements Runnable
{
    //public static final int MESSAGING_NODE_CONNECTION = 0;
    //public static final int REGISTRY_CONNECTION = 1;


    private final Client c;
    private final Socket s; //This is the socket used to communicate data
    //    private Message b = null;
    //private boolean busy = false;
    private volatile boolean HCF = false; //Something bad happened.
    private final Thread t;


    public ClientConnection(Client cli, String address, int port) throws java.io.IOException
    {
	c = cli;
	s = new Socket(address, port);
	t = new Thread(this);
	//t.start();
    }

    /*    
    public Connection(Node node ,Socket sock)
    {
	n = node;
	s = sock;
	t = new Thread(this);
	//t.start();
    }
    */

    public void start() {
	try{
	    t.start();
	}
	catch(IllegalThreadStateException e){}
    }

    /*
    public void HALTCATCHFIRE()
    {
	HCF = true;
	t.interrupt();
    }
    */
    
    public boolean getHCF()
    {
	return (HCF == true);
    }
   
    public void run()
    {
	//	System.out.println("THREAD START!");
	String h;
	while(!HCF) {
	    try{
  		//MessageHandlingThread m = new MessageHandlingThread(recv());
		//Thread t = new Thread(m);
		//t.start();
		h = recv();
		c.removeHash(h);
	    }
	    catch(Exception e){
		//e.printStackTrace();
		HCF = true;
		//return;
	    }
	}
    }

    
    //public Message get_Message(){return b;}

    private String recv()
    {
	java.io.BufferedInputStream innerstr;
	java.io.DataInputStream outerstr;
	try {
	    //System.out.println("AM I RECEIVING ANYTHING??");
	    innerstr = new java.io.BufferedInputStream(s.getInputStream());
	    outerstr = new java.io.DataInputStream(innerstr);

	    //int l = outerstr.readInt(); //Get size of message

	    byte[] receiveme = new byte[8192]; //WAY TOO BIG
	    int rc = outerstr.read(receiveme);
	    
	    java.io.ByteArrayInputStream instr = new java.io.ByteArrayInputStream(receiveme);
	    java.io.DataInputStream outstr = new java.io.DataInputStream(innerstr);
	    
	    return outstr.readUTF();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    return "";
	}
	/*
	finally {
	    if(innerstr != null) innerstr.close();
	    if(outerstr != null) outerstr.close();
	}
	*/
    }

    public InetAddress getLocalAddress(){return s.getLocalAddress();}
    public InetAddress getInetAddress(){return s.getInetAddress();}
    public String getLocalHost(){return s.getLocalAddress().getHostName();}
    public String getRemoteHost(){return s.getInetAddress().getHostName();}
    public int getLocalPort(){return s.getLocalPort();}
    public int getPort(){return s.getPort();}
    public SocketAddress getLocalSocketAddress(){return s.getLocalSocketAddress();}
    public SocketAddress getRemoteSocketAddress(){return s.getRemoteSocketAddress();}

    public void send(byte[] sendme) throws java.io.IOException
    {
	java.io.BufferedOutputStream innerstr = new java.io.BufferedOutputStream(s.getOutputStream());
	java.io.DataOutputStream outerstr = new java.io.DataOutputStream(innerstr);

	outerstr.write(sendme,0,sendme.length);
	outerstr.flush();
    }

    public void clear()
    {
	//b = null;
    }

}

/*
class MessageHandlingThread implements Runnable
{
    // b;

    //MessageHandlingThread(byte[] x){x=b;}

    public void run()
    {
	//	MessagingNode.Handle_Message(b);
    }
}
*/
