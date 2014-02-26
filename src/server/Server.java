package server;

public class Server
{
    public static void main(String args[]) 
    {
        System.out.println("\nServer Started\n");
        int port = -1;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        
        Daemon d = new  Daemon();
        d.listen(port);
    }
}
