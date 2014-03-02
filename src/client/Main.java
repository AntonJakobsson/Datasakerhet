package client;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import client.gui.LoginWindow;
import client.gui.SelectUserWindow;

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
			
			ArrayList<User> patients = state.queryUsers(User.PATIENT);
			SelectUserWindow selectUser = new SelectUserWindow(currentUser, patients);
			while(selectUser.showDialog() == 0) 
			{
				User selectedPatient = selectUser.getSelectedUser();
				
				System.out.println("You have selected " + selectedPatient);
			}			
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
