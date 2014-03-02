package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import server.data.Database;

public class Daemon
{
	public final static String KEYSTORE = "server_key.store";
	public final static String TRUSTSTORE = "server_trust.store";
	
    protected int port;
    protected SSLServerSocket socket;
    protected Database db;
    
    public Daemon() 
    {
        this.db = new Database(Database.FILE);
        this.db.connect();
    }
    
    public void listen(int port) throws IOException
    {
        this.port = port;
        try {
            this.socket = createServerSocket(port);
            this.socket.setNeedClientAuth(true);
        } 
        catch (IOException e) {
            System.out.println("Unable to start Server: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println(String.format("Listening on port %d... (SSL)", port));
        Log.write(String.format("Server started on port %d", port));
        
        /* Kanske inte världens bästa idé men vafan */
        while(true)
        	fork();
    }
    
    public void fork() throws IOException
    {
        SSLSocket client = (SSLSocket)socket.accept();
        Fork fork = new Fork(this, client);
        new Thread(fork).start();
    }
    
    private SSLServerSocket createServerSocket(int port) throws IOException
    {
        SSLServerSocketFactory ssf = null;
        try 
        {
            SSLContext          ctx = SSLContext.getInstance("TLS");
            KeyManagerFactory   kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            KeyStore            ks  = KeyStore.getInstance("JKS");
            KeyStore            ts  = KeyStore.getInstance("JKS");
            
            /* The legendary password */
            char[] password = "password".toCharArray();

            ks.load(new FileInputStream(KEYSTORE),   password); // keystore password (storepass)
            ts.load(new FileInputStream(TRUSTSTORE), password); // truststore password (storepass)
            System.out.println("Key & Trust stores loaded");
            
            kmf.init(ks, password); // certificate password (keypass)
            tmf.init(ts);  // possible to use keystore as truststore here
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            ssf = ctx.getServerSocketFactory();
        } 
        catch (Exception e) {
            System.out.println("Error creating SSL server socket:");
            e.printStackTrace();
            System.exit(-1);
        }
        return (SSLServerSocket)ssf.createServerSocket(port);
    }
}
