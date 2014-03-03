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

/**
 * Represents a client connection
 */
public class Fork implements Runnable
{
    protected Daemon server;
    protected SSLSocket socket;
    protected SSLSession session;
    protected X509Certificate cert;
    
    protected PacketReader input;
    protected PacketWriter output;
    protected Database db;
    protected Gson gson;
    
    protected User user;
    protected boolean authenticated;
    
    /**
     * Initializes a new client connection
     * @param daemon Reference to the parent daemon
     * @param socket SSL Socket
     * @throws SSLPeerUnverifiedException
     */
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
            
            /* Grab the user the current certificate belongs to */
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
        finally {
            this.close();
        }
    }
    
    public void message(String message)
    {
    	write(new Packet(Packet.MESSAGE, 0, message));
    }
    
    public void ban(int minutes, String reason)
    {
    	message(String.format("You have been banned for %d minutes\nReason: %s", minutes, reason));
    	server.ban(socket.getInetAddress(), minutes, reason);
		this.close();
    } 
    
    /* Packet handlers */
    
    private void handleQueryUsers(int type)
	{
    	if (!this.authenticated) {
			write(new Packet(Packet.QUERY_USER, Packet.DENIED, "You must be authenticated to query users"));
			Log.write(String.format("%s attempted to query users without being authenticated", this.user));
			return;
		}
    	
		ArrayList<User> results = new ArrayList<User>();;
	    try 
	    {
	    	switch(this.user.getType())
	    	{
		    	case User.PATIENT:
		    		if (type != User.PATIENT) {
		    			write(new Packet(Packet.QUERY_USER, Packet.DENIED, "Patients may not query any other user types"));
		    			Log.write(String.format("%s attempted to query users of type %s", this.user, User.typeString(type)));
		    			return;
		    		}
		    		results.add(this.user);
		    		break;
		    	
		    	case User.NURSE:
		    	case User.DOCTOR:
		    	case User.GOVERNMENT:
		    		results = db.users().findByType(type);
		    		break;
	    	}
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
		if (!this.authenticated) {
			write(new Packet(Packet.QUERY_REC, Packet.DENIED, "You must be authenticated to query records"));
			Log.write(String.format("%s attempted to query records without being authenticated", this.user));
			return;
		}
		
		ArrayList<Record> results = new ArrayList<Record>();
		try {
			User user = db.users().findById(p.getCode());
			switch(this.user.getType()) 
			{
				case User.PATIENT:
	            	results = db.records().findByPatient(this.user);
	            	break;
				case User.NURSE:
					results = db.records().findByNurse(user, this.user);
					break;
				case User.DOCTOR:
					results = db.records().findByDoctor(user, this.user);
					break;
				case User.GOVERNMENT:
					results = db.records().findByPatient(user);
					break;
			}
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
		if (!this.authenticated) {
			write(new Packet(Packet.POST, Packet.DENIED, "You must be authenticated to add/edit records"));
			Log.write(String.format("%s attempted to create a record without being authenticated", this.user));
			return;
		}
		
		try {
			if (this.user.getType() != User.DOCTOR) {
				/* Endast doctor har rätt att ändra i records */
				write(new Packet(Packet.POST, Packet.DENIED, "Only doctors may create/edit new records"));
				Log.write(String.format("%s attempted to create a record without permission", this.user));
				return;
			}
			if (record.getDoctorId() != this.user.getId()) {
				/* Doctor kan endast ändra sina egna records */
				write(new Packet(Packet.POST, Packet.DENIED, "You may not create/edit records on behalf of other doctors"));
				Log.write(String.format("%s attempted to edit/create record owned by another user", this.user));
				return;
			}
			
			if (db.records().exists(record.getId())) {
				db.records().update(record);
			}
			else {
				db.records().insert(record);
				/* Re-query record to find user names */
				record = db.records().findById(record.getId());
			}
			
			/* men svara för fan */
			write(new Packet(Packet.POST, 0, gson.toJson(record)));
		}
		catch(SQLException ex) {
			System.out.println("addPost SQL exception:");
			ex.printStackTrace();
		}
	}

	private void deletePost(Record record)
	{
		if (!this.authenticated) {
			write(new Packet(Packet.DELETE, Packet.DENIED, "You must be authenticated to delete records"));
			Log.write(String.format("%s attempted to delete record %d without authentication", this.user, record.getId()));
			return;
		}
		try {
			if (this.user.getType() != User.GOVERNMENT) {
				write(new Packet(Packet.DELETE, Packet.DENIED, "Only government agencies may delete records"));
				Log.write(String.format("%s attempted to delete record %d", this.user, record.getId()));
				return;
			}
			db.records().delete(record);
		}
		catch(SQLException ex) {
			System.out.println("SQL Exception from deletePost():");
			ex.printStackTrace();
		}
	}

	private void handleAuthPacket(String password) throws SQLException, IOException
    {
	    /* Skip packet if a user is already authed */
	    if (this.authenticated) return;

    	String hash = Security.hash(password, user.getSalt());
    	String message;
    	int    code;
    	
    	/* Compare the computed hash to the one stored in the database */
    	if (hash.equals(user.getPassword())) 
    	{
    		message = gson.toJson(user);
    		code = Packet.SUCCESS;
    		this.authenticated = true;
    		Log.write(String.format("%s authenticated from %s", this.user, socket.getInetAddress()));
    	} 
    	else /* Invalid password */
    	{
    		message = "Invalid password";
    		code = Packet.ERROR;
    		this.authenticated = false;
    		Log.write(String.format("Failed login attempt: %s from %s", this.user, socket.getInetAddress()));
    		if (!server.attempt(socket.getInetAddress()))
    			socket.close();
    		try { Thread.sleep(500); } catch (InterruptedException ex) { }
    	}
    	Packet p = new Packet(Packet.AUTH, code, message);
    	output.write(p);
    }
    
	/**
	 * Closes the client connection
	 */
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
    
    /**
     * Writes a packet to the client
     * @param packet Packet object
     */
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
