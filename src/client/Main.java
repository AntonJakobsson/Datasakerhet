package client;

public class Main
{
    public static void main(String[] args)
    {
        String host = null;
        int    port = -1;
        
        if (args.length < 2) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }
        try { /* get input parameters */
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }
        
        Client client = new Client(host, port);
        Thread networkThread = new Thread(client);
        networkThread.start();
    }
}
