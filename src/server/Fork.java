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
        finally {
            close();
        }
    }
    
    private void handleQueryUsers(int type)
	{
		ensureAuth();
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
		ensureAuth();
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
		ensureAuth();
		try {
			// TODO maddafackin security
			if (this.user.getType() != User.DOCTOR) {
				write(new Packet(Packet.POST, Packet.DENIED, "Only doctors may create new records"));
				Log.write(String.format("%s attempted to create a record without permission", this.user));
				return;
			}
			if (record.getDoctorId() != this.user.getId()) {
				write(new Packet(Packet.POST, Packet.DENIED, "You may not create records on behalf of other doctors"));
				Log.write(String.format("%s attempted to edit/create record owned by another user", this.user));
				return;
			}
			
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

    	String hash = Security.hash(password, user.getSalt());
    	String message;
    	int    code;
    	
    	if (hash.equals(user.getPassword())) {
    		message = gson.toJson(user);
    		code = Packet.SUCCESS;
    		this.authenticated = true;
    		Log.write(String.format("%s authenticated from %s", this.user, socket.getInetAddress()));
    	} 
    	else {
    		message = "Invalid username or password";
    		code = Packet.ERROR;
    		this.authenticated = false;
    		Log.write(String.format("Failed login attempt: %s from %s", this.user, socket.getInetAddress()));
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
