package client;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import client.gui.LoginWindow;

import common.Record;
import common.User;

public class Main
{
	Client client;
	NetworkState state;
	
	public Main(String host, int port)
	{
		client = new Client(host, port);
	}
	
	public void run()
	{
		Thread networkThread = new Thread(client);
        networkThread.start();
	    try {
	        while(!client.isConnected())
	            Thread.sleep(50);
        }
        catch(InterruptedException ex) {
            ex.printStackTrace();
        }
		this.state = client.getState();
		
		/* Application loop */
		
		LoginWindow loginWindow = new LoginWindow();
		while(loginWindow.showDialog() == 0)
		{
			String password = loginWindow.getPassword();
			
			User currentUser;
			try { currentUser = state.auth(password); }
			catch(AccessDeniedException ex) 
			{
				JOptionPane.showMessageDialog(null, ex.getMessage(), "Authentication failed", JOptionPane.ERROR_MESSAGE);
				/* Hoppa tillbaks till login */
				continue;
			}
			
			System.out.println("Auth ok");
			System.out.println("You are " + currentUser.toString());
			
			System.out.println("\nPatients:");
			ArrayList<User> patients = state.queryUsers(User.PATIENT);
			for(User p : patients)
				System.out.println(p);
			if (patients.size() == 0)
				System.out.println("-- none --");
			else
			{
				// DEBUG SHIT, find the maddafackin records
				User patient = patients.get(0);
				ArrayList<Record> records = state.queryRecords(patient);
				
				System.out.println("Records:");
				for(Record r : records)
					System.out.println(String.format("%d: %s", r.getId(), r.getData()));
			}
			
			
			System.out.println("");
			
			
		}
		
		/* die */
		client.close();
		System.exit(0);
	}
	
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
        
        /* Go go */
        new Main(host, port).run();
    }
}
