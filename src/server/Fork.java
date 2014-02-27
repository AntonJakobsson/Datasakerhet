package server;

import java.io.IOException;
import java.sql.SQLException;

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
import common.packets.AuthPacket;

public class Fork implements Runnable
{
    Daemon server;
    SSLSocket socket;
    SSLSession session;
    X509Certificate cert;
    
    PacketReader input;
    PacketWriter output;
    Database db;
    
    public Fork(Daemon daemon, SSLSocket socket) throws SSLPeerUnverifiedException
    {
        this.server = daemon;
        this.socket = socket;
        this.session = socket.getSession();
        this.cert = (X509Certificate)session.getPeerCertificateChain()[0];
        db = daemon.db;
        System.out.println(String.format("Accepted connection from %s", socket.getInetAddress()));
    }

    @Override
    public void run()
    {
        Gson gson = new Gson();
        try {
            this.input  = new PacketReader(this.socket.getInputStream());
            this.output = new PacketWriter(this.socket.getOutputStream());
            
            Packet packet;
            while((packet = input.read()) != null) {
                switch(packet.getType()) {
                    case Packet.AUTH:
                        AuthPacket ap = gson.fromJson(packet.getString(), AuthPacket.class);
                        handleAuthPacket(ap);
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
                    	Packet p = gson.fromJson(packet.getString(), Packet.class);
                    	break;
                    }
                }
            }
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Client disconnected! Error: " + e.getMessage());
            e.printStackTrace();
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

	private void addPost(User user)
	{
		// TODO Auto-generated method stub
		
	}

	private void deletePost(User u)
	{
    	// remove post from database i.e. db.delete(p.getID());
    
	}

	private void handleAuthPacket(AuthPacket packet) throws SQLException, IOException
    {
    	User u = db.users().findById(packet.getId());
    	String hash = Security.hash(packet.getPassword(), u.getSalt());
    	String message = "";
    	int code = -1;
    	if (hash.equals(u.getPassword())) {
    		message = "Authentication successful";
    		code = 0; //Not defined
    	} else {
    		message = "Invalid username or password";
    		code = 1; //Not defined
    	}
    	Packet p = new Packet(Packet.AUTH, code, message);
    	output.write(p);
    }
    
    public void close()
    {
        System.out.println("Connection lost");
    }
}
