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
import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.Packet;
import common.PacketReader;
import common.PacketWriter;
import common.Record;
import common.User;

public class Client implements Runnable
{
	public final static String KEYSTORE   = "client_key.store";
	public final static String TRUSTSTORE = "client_trust.store";
	
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
                switch(packet.getType()) 
                {
                    case Packet.MESSAGE:
                        handleMessage(packet.getString());
                        break;
                    case Packet.AUTH:
                        handleAuth(packet);
                        break;
                    case Packet.QUERY_USER:
                        handleQueryUser(packet);
                        break;
                    case Packet.QUERY_REC:
                    	handleQueryRecord(packet);
                    	break;
                    case Packet.POST:
                    	handlePostRecord(packet);
                    	break;
                    case Packet.DELETE:
                    	handleDeleteRecord(packet);
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
        JOptionPane.showMessageDialog(null, message);
    }
    
    protected void handleAuth(Packet packet)
    {
        if (packet.getCode() == Packet.SUCCESS) {
            User user = gson.fromJson(packet.getString(), User.class);
            state.setUser(user);
        }
        else {
        	state.error(packet.getString());
        }
    }
    
    protected void handleQueryUser(Packet packet)
    {
    	if (packet.getCode() == Packet.DENIED) {
    		state.error(packet.getString());
    		return;
    	}
    	
        Type listType = new TypeToken<ArrayList<User>>() { }.getType();
        ArrayList<User> list = gson.fromJson(packet.getString(), listType);
        state.setUserList(list);
    }
    
    protected void handleQueryRecord(Packet packet)
    {
    	if (packet.getCode() == Packet.DENIED) {
    		state.error(packet.getString());
    		return;
    	}
    	
    	Type listType = new TypeToken<ArrayList<Record>>() { }.getType();
        ArrayList<Record> list = gson.fromJson(packet.getString(), listType);
        state.setRecordList(list);
    }
    
    public void handlePostRecord(Packet packet)
    {
    	if (packet.getCode() == Packet.DENIED) {
    		state.error(packet.getString());
    		return;
    	}
    	
    	Record record = gson.fromJson(packet.getString(), Record.class);
    	state.setRecord(record);
    }
    
    public void handleDeleteRecord(Packet packet)
    {
    	if (packet.getCode() == Packet.DENIED) {
    		state.error(packet.getString());
    		return;
    	}
    	
    	state.setRecord(new Record.None());
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
        
        System.out.println("Socket disconnected. Killing application");
        System.exit(-1);
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
            ks.load(new FileInputStream(KEYSTORE),   password); // keystore password (storepass)
            ts.load(new FileInputStream(TRUSTSTORE), password); // truststore password (storepass);
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
