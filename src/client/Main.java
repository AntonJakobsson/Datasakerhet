package client;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import client.gui.CreateWindow;
import client.gui.EditWindow;
import client.gui.LoginWindow;
import client.gui.RecordChooseWindow;
import client.gui.SelectUserWindow;
import common.Record;
import common.User;

public class Main
{
    Client       client;
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
            while (!client.isConnected())
                Thread.sleep(50);
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        this.state = client.getState();

        /* Application loop */
        login();
        
        /* Die */
        client.close();
        System.exit(0);
    }
    
    protected void login()
    {
        LoginWindow loginWindow = new LoginWindow();
        while (loginWindow.showDialog() == 0)
        {
            String password = loginWindow.getPassword();

            User currentUser;
            try {
                /* Authenticate user */
                currentUser = state.auth(password);
            }
            catch (AccessDeniedException ex)
            {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Authentication failed", JOptionPane.ERROR_MESSAGE);
                /* Hoppa tillbaks till login */
                continue;
            }

            selectPatient(currentUser);
        }
    }
    
    protected void selectPatient(User currentUser)
    {
        /* Query patients from server */
        ArrayList<User> patients = state.queryUsers(User.PATIENT);
        
        SelectUserWindow selectUser = new SelectUserWindow(currentUser, patients, "Select Patient");
        while (selectUser.showDialog() == 0)
        {
            User selectedPatient = null;
            try {
                selectedPatient = selectUser.getSelectedUser();
                System.out.println("You have selected " + selectedPatient);
            }
            catch (RuntimeException e) {
                JOptionPane.showMessageDialog(
                    null, 
                    e.getMessage(), 
                    "Patient retrieval failed",
                    JOptionPane.ERROR_MESSAGE
                );
                continue;
            }
            
            manipulateRecords(currentUser, selectedPatient);
        }
    }
    
    protected void manipulateRecords(User currentUser, User selectedPatient)
    {
        /* Query records from server */
        ArrayList<Record> records = state.queryRecords(selectedPatient);
        
        RecordChooseWindow chooseRecord = new RecordChooseWindow(selectedPatient, records);
        int button = 0;
        while ((button = chooseRecord.showDialog()) != RecordChooseWindow.MESSAGE_CANCEL) 
        {
            Record record = null;
            if (button != RecordChooseWindow.MESSAGE_NEW) {
                try {
                    record = chooseRecord.getSelectedRecord();
                }
                catch (RuntimeException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Record retrieval failed", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            }
            
            try {
	            switch (button) 
	            {
	                case RecordChooseWindow.MESSAGE_VIEW: {
	                    editRecord(record);
	                    break;
	                }
	                case RecordChooseWindow.MESSAGE_NEW: {
	                    Record newrecord = newRecord(currentUser, selectedPatient);
	                    records.add(newrecord);
	                    chooseRecord.updateWindow(records);
	                    break;
	                }
	                case RecordChooseWindow.MESSAGE_DELETE: {
	                	if(JOptionPane.showOptionDialog(null, "Are you sure you wish to delete this record?", "Confirm delete",
	                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
	                            new String[] { "Confirm delete", "Cancel" }, "Confirm delete") == 0) {
	                		state.deleteRecord(record);
	                		records.remove(record);
	                		chooseRecord.updateWindow(records);
	                    }
	                    break;
	                }
	            }
            }
            catch(AccessDeniedException ex) {
            	JOptionPane.showMessageDialog(null, ex.getMessage(), "Access denied", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    protected void editRecord(Record record)
    {
        EditWindow editWindow = new EditWindow(record);
        if (editWindow.showDialog() == 0) {
            /* Post record to server */
            state.postRecord(record);
        }
    }
    
    protected Record newRecord(User currentUser, User selectedPatient)
    {
        /* Query nurses from server */
        ArrayList<User> nurseList = state.queryUsers(User.NURSE);
        
        CreateWindow createWindow = new CreateWindow(currentUser, selectedPatient, nurseList);
        if(createWindow.showDialog() == 0) {
            Record newRecord = createWindow.getCreatedRecord();
            
            /* Post record to server */
            return state.postRecord(newRecord);
        }
        return null;
    }

    public static void main(String[] args)
    {
        String host = null;
        int port = -1;

        if (args.length < 2) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }
        try { /* get input parameters */
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        catch (IllegalArgumentException e) {
            System.out.println("USAGE: java client host port");
            System.exit(-1);
        }

        /* Go go */
        new Main(host, port).run();
    }
}
