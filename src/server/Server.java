package server;

import java.io.IOException;

public class Server
{	
    public static void main(String args[]) 
    {
        System.out.println("\nStarting server...");
        
        int port = -1;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        
        Daemon d = new  Daemon();
        
        try
		{
			d.listen(port);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    }
}
