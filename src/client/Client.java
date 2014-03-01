package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;

import common.Packet;
import common.PacketReader;
import common.PacketWriter;

public class Client implements Runnable
{
    protected String host;
    protected int port;
    protected SSLSocket socket;
    protected SSLSession session;
    protected X509Certificate cert;
    
    protected PacketReader input;
    protected PacketWriter output;
    
    public Client(String host, int port)
    {
        this.host = host;
        this.port = port;
    }
    
    @Override
    public void run()
    {
        try {
            connect(this.host, this.port);
            
            this.input  = new PacketReader(socket.getInputStream());
            this.output = new PacketWriter(socket.getOutputStream());
            
            while(socket.isConnected()) {
                Packet packet = input.read();
                switch(packet.getType()) {
                    case Packet.MESSAGE:
                        handleMessage(packet.getString());
                        break;
                }
            }
        }
        catch(IOException ex) {
            System.out.println("Disconnected from server: " + ex.getMessage());
        }
        finally {
            close();
        }
    }
    
    /* Packet handlers */
    
    protected void handleMessage(String message)
    {
        System.out.println("<< " + message);
    }
    
    /* Socket functions */
    
    public void write(Packet packet) 
    {
        try {
            output.write(packet);
        }
        catch(IOException ex) {
            System.out.println("Write error: " + ex.getMessage());
            close();
        }
    }
    
    public void connect(String host, int port) throws IOException
    {
        this.socket  = getSocket(host, port);
        this.socket.startHandshake();
        this.session = socket.getSession();
        this.cert    = (X509Certificate)session.getPeerCertificateChain()[0];
        System.out.println(String.format("Connected to %s on port %d", host, port));
    }
    
    public void close()
    {
        if (socket == null) return;
        try {
            socket.close();
        }
        catch(IOException ex) {
            /* silence */
        }
    }

    protected SSLSocket getSocket(String host, int port) throws IOException
    {
        SSLSocketFactory factory = null;
        try {
            char[] password = "password".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore ts = KeyStore.getInstance("JKS");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            SSLContext ctx = SSLContext.getInstance("TLS");
            ks.load(new FileInputStream("client_key.store"), password);  // keystore password (storepass)
            ts.load(new FileInputStream("client_trust.store"), password); // truststore password (storepass);
            kmf.init(ks, password); // user password (keypass)
            tmf.init(ts); // keystore can be used as truststore here
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            factory = ctx.getSocketFactory();
            return (SSLSocket)factory.createSocket(host, port);
        } 
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
