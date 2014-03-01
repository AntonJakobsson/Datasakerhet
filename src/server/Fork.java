package server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;

import server.data.Database;

import com.google.gson.Gson;
import common.Packet;
import common.PacketReader;
import common.PacketWriter;
import common.Security;
import common.User;

public class Fork implements Runnable
{
    Daemon server;
    SSLSocket socket;
    SSLSession session;
    X509Certificate cert;
    
    PacketReader input;
    PacketWriter output;
    Database db;
    Gson gson;
    
    User user;
    boolean authenticated;
    
    public Fork(Daemon daemon, SSLSocket socket) throws SSLPeerUnverifiedException
    {
        this.server = daemon;
        this.socket = socket;
        this.session = socket.getSession();
        this.cert = (X509Certificate)session.getPeerCertificateChain()[0];
        this.db = daemon.db;
        this.gson = new Gson();
        System.out.println(String.format("Accepted connection from %s", socket.getInetAddress()));
    }

    @Override
    public void run()
    {
        try {
            this.input  = new PacketReader(this.socket.getInputStream());
            this.output = new PacketWriter(this.socket.getOutputStream());
            
            output.write(new Packet(Packet.MESSAGE, 0, "Welcome lol"));
            
            int user_id = this.getUserIdFromCert(cert);
            this.user = db.users().findById(user_id);
            
            System.out.println("Certificate holder: " + user.toString());
            
            while(socket.isConnected()) 
            {
                Packet packet = input.read();
                switch(packet.getType()) {
                    case Packet.AUTH:
                        handleAuthPacket(packet.getString());
                        break;
                    case Packet.DELETE: {
                    	User u = gson.fromJson(packet.getString(), User.class);
                    	deletePost(u);
                    	break;
                    }
                    case Packet.MESSAGE: {
                    	handleMessage(packet.getString());
                    	break;
                    }                    	
                    case Packet.POST: {
                    	User u = gson.fromJson(packet.getString(), User.class);
                    	addPost(u);
                    	break;
                    }
                    case Packet.QUERY_REC: {                    	
                    	Packet p = gson.fromJson(packet.getString(), Packet.class);
                    	queryRecord(p);
                    	break;
                    }
                    case Packet.QUERY_USER: {
                    	handleQueryUsers(packet.getCode());
                    	break;
                    }
                }
            }
        }
        catch (IOException e) {
            System.out.println("Client disconnected! Error: " + e.getMessage());
        }
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally {
            close();
        }
    }
    
    private void handleMessage(String string)
	{
		// TODO Auto-generated method stub
		
	}

	private void queryRecord(Packet p)
	{
		// TODO Auto-generated method stub
		
	}
	
	private void handleQueryUsers(int type)
	{
	    try {
	        // TODO Security levelz
            ArrayList<User> results = db.users().findByType(type);
            write(new Packet(Packet.QUERY_USER, results.size(), gson.toJson(results)));
        }
        catch (SQLException e) {
            
        }
	}

	private void addPost(User user)
	{
		// TODO Auto-generated method stub
		
	}

	private void deletePost(User u)
	{
    	// remove post from database i.e. db.delete(p.getID());
    
	}

	private void handleAuthPacket(String password) throws SQLException, IOException
    {
    	String hash = Security.hash(password, user.getSalt());
    	String message;
    	int    code;
    	
    	if (hash.equals(user.getPassword())) {
    		message = gson.toJson(user);
    		code = Packet.SUCCESS;
    		this.authenticated = true;
    	} else {
    		message = "Invalid username or password";
    		code = Packet.ERROR;
    		this.authenticated = false;
    	}
    	
    	Packet p = new Packet(Packet.AUTH, code, message);
    	output.write(p);
    }
    
    public void close()
    {
        if (this.socket == null) return;
        try {
            socket.close();
        }
        catch(IOException ex) {
            /* silence */
        }
    }
    
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
    
    protected boolean ensureAuth(int userType) 
    {
        if (!authenticated) {
            write(new Packet(Packet.MESSAGE, 0, "Not authenticated"));
            return false;
        }
        return true;
    }
    
    protected int getUserIdFromCert(X509Certificate cert) 
    {
        try {
            String user_string = cert.getSubjectDN().getName();
            return Integer.parseInt(user_string.substring(user_string.indexOf("=") + 1));
        }
        catch(NumberFormatException ex) {
            return -1;
        }
    }
}
