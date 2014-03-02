package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.Packet;
import common.PacketReader;
import common.PacketWriter;
import common.User;

public class Client implements Runnable
{
    protected String host;
    protected int port;
    protected boolean ready;
    protected SSLSocket socket;
    protected SSLSession session;
    protected X509Certificate cert;
    
    protected Gson gson;
    protected PacketReader input;
    protected PacketWriter output;
    protected NetworkState state;
    
    public Client(String host, int port)
    {
        this.host = host;
        this.port = port;
        this.state = new NetworkState(this);
        this.gson = new Gson();
    }
    
    public NetworkState getState() {
        return state;
    }
    
    @Override
    public void run()
    {
        try {
            connect(this.host, this.port);
            
            this.input  = new PacketReader(socket.getInputStream());
            this.output = new PacketWriter(socket.getOutputStream());
            this.ready  = true;
            
            while(socket.isConnected()) 
            {
                Packet packet = input.read();
                switch(packet.getType()) {
                    case Packet.MESSAGE:
                        handleMessage(packet.getString());
                        break;
                    case Packet.AUTH:
                        handleAuth(packet);
                        break;
                    case Packet.QUERY_USER:
                        handleQueryUser(packet);
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
    
    protected void handleAuth(Packet packet)
    {
        if (packet.getCode() == Packet.SUCCESS) {
            User user = gson.fromJson(packet.getString(), User.class);
            System.out.println("Authenticated as " + user);
            state.setUser(user);
        }
        else {
            System.out.println("Authentication failed");
            state.setUser(new User.None());
        }
    }
    
    protected void handleQueryUser(Packet packet)
    {
        Type listType = new TypeToken<ArrayList<User>>() { }.getType();
        ArrayList<User> list = gson.fromJson(packet.getString(), listType);
        state.setQueryUsers(list);
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
        this.ready = false;
        if (socket == null) return;
        try {
            socket.close();
        }
        catch(IOException ex) {
            /* silence */
        }
    }
    
    public boolean isConnected()
    {
        return this.ready;
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
