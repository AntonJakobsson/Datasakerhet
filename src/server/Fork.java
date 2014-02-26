package server;

import java.io.IOException;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;

import common.PacketReader;
import common.PacketWriter;

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
    }

    @Override
    public void run()
    {
        try {
            this.input  = new PacketReader(this.socket.getInputStream());
            this.output = new PacketWriter(this.socket.getOutputStream());
            
            
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Client disconnected! Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
