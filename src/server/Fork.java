package server;

import java.io.IOException;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;

import com.google.gson.Gson;
import common.Packet;
import common.PacketReader;
import common.PacketWriter;
import common.packets.AuthPacket;

public class Fork implements Runnable
{
    Daemon server;
    SSLSocket socket;
    SSLSession session;
    X509Certificate cert;
    
    PacketReader input;
    PacketWriter output;
    
    public Fork(Daemon daemon, SSLSocket socket) throws SSLPeerUnverifiedException
    {
        this.server = daemon;
        this.socket = socket;
        this.session = socket.getSession();
        this.cert = (X509Certificate)session.getPeerCertificateChain()[0];
        
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
                        break;
                }
            }
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Client disconnected! Error: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            close();
        }
    }
    
    public void close()
    {
        System.out.println("Connection lost");
    }
}
