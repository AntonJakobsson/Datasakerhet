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
import common.PacketFactory;
import common.PacketReader;
import common.PacketWriter;
import common.Record;
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
        try 
        {
            this.input  = new PacketReader(this.socket.getInputStream());
            this.output = new PacketWriter(this.socket.getOutputStream());
            
            output.write(new Packet(Packet.MESSAGE, 0, "Welcome lol"));
            
            int user_id = this.getUserIdFromCert(cert);
            this.user = db.users().findById(user_id);
            
            System.out.println("Certificate holder: " + user.toString());
            
            while(socket.isConnected()) 
            {
                Packet packet = input.read();
                switch(packet.getType()) 
                {
                    case Packet.AUTH: {
                        handleAuthPacket(packet.getString());
                        break;
                    }
                    case Packet.DELETE: {
                    	Record record = gson.fromJson(packet.getString(), Record.class);
                    	deletePost(record);
                    	break;
                    }                  	
                    case Packet.POST: {
                    	Record record = gson.fromJson(packet.getString(), Record.class);
                    	addPost(record);
                    	break;
                    }
                    case Packet.QUERY_REC: {                    	
                    	handleQueryRecord(packet);
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
            System.out.println("Client disconnected");
        }
		catch (SQLException e)
		{
			System.out.println("Caught SQL Exception in network loop :S");
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			System.out.println("Thread interrupted during failed authentication");
			e.printStackTrace();
		}
        finally {
            close();
        }
    }
    
    private void handleQueryUsers(int type)
	{
		ensureAuth();
		ArrayList<User> results;
	    try {
	        // TODO Security levelz
            results = db.users().findByType(type);
        }
        catch (SQLException e) {
        	System.out.println("handleQueryUsers SQL Exception:");
        	e.printStackTrace();
            results = new ArrayList<User>();
        }	
	    write(PacketFactory.queryUsersReply(results));
	}

	private void handleQueryRecord(Packet p)
	{
		ensureAuth();
		ArrayList<Record> results;
		try {
	        // TODO Security levelz
			User user = db.users().findById(p.getCode());
            results = db.records().findByUser(user);
        }
        catch (SQLException e) {
        	System.out.println("handleQueryRecord SQL Exception:");
        	e.printStackTrace();
            results = new ArrayList<Record>();
        }	
		write(PacketFactory.queryRecordsReply(results));
	}

	private void addPost(Record record)
	{
		ensureAuth();
		try {
			// TODO maddafackin security
			if (db.records().exists(record.getId())) {
				db.records().update(record);
			}
			else {
				db.records().insert(record);
			}
		}
		catch(SQLException ex) {
			System.out.println("addPost SQL exception:");
			ex.printStackTrace();
		}
	}

	private void deletePost(Record record)
	{
		ensureAuth();
		try {
			db.records().delete(record);
		}
		catch(SQLException ex) {
			System.out.println("SQL Exception from deletePost():");
			ex.printStackTrace();
		}
	}

	private void handleAuthPacket(String password) throws SQLException, IOException, InterruptedException
    {

    	String hash = Security.hash(password, user.getSalt());
    	String message;
    	int    code;
    	
    	if (hash.equals(user.getPassword())) {
    		message = gson.toJson(user);
    		code = Packet.SUCCESS;
    		this.authenticated = true;
    	} 
    	else {
    		message = "Invalid username or password";
    		code = Packet.ERROR;
    		this.authenticated = false;
    		Thread.sleep(500);
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
    
    protected boolean ensureAuth() 
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
