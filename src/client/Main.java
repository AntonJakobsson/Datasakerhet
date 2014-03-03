package client;

import java.util.ArrayList;

import javax.swing.JFrame;
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

        LoginWindow loginWindow = new LoginWindow();
        while (loginWindow.showDialog() == 0)
        {
            String password = loginWindow.getPassword();

            User currentUser;
            try {
                currentUser = state.auth(password);
            }
            catch (AccessDeniedException ex)
            {
                JOptionPane
                        .showMessageDialog(null, ex.getMessage(), "Authentication failed", JOptionPane.ERROR_MESSAGE);
                /* Hoppa tillbaks till login */
                continue;
            }

            ArrayList<User> patients = state.queryUsers(User.PATIENT);
            SelectUserWindow selectUser = new SelectUserWindow(currentUser, patients);
            while (selectUser.showDialog() == 0)
            {
                User selectedPatient = null;
                try {
                    selectedPatient = selectUser.getSelectedUser();
                    System.out.println("You have selected " + selectedPatient);
                }
                catch (RuntimeException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Patient retrieval failed",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                ArrayList<Record> records = state.queryRecords(selectedPatient);
                RecordChooseWindow chooseRecord = new RecordChooseWindow(selectedPatient, records);

                int button = 0;
                while ((button = chooseRecord.showDialog()) != RecordChooseWindow.MESSAGE_CANCEL) {
                    Record record = null;
                    try {
                        record = chooseRecord.getSelectedRecord();
                    }
                    catch (RuntimeException e) {
                        JOptionPane.showMessageDialog(null, e.getMessage(), "Record retrieval failed",
                                JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    switch (button) {
                        case RecordChooseWindow.MESSAGE_VIEW:
                            EditWindow editWindow = new EditWindow(record);
                            if (editWindow.showDialog() == 0) {
                                // record is saved;
                            }
                            break;
                        case RecordChooseWindow.MESSAGE_NEW:
                          //CreateWindow createWindow = new CreateWindow(currentUser, selectedPatient, nurseList);
                          //if(createWindow.showDialog()==0){
                                //Record newRecord = createWindow.getCreatedRecord();
                                //check record for special cases
                                //update database
                          //}
                            break;
                        case RecordChooseWindow.MESSAGE_DELETE:
                            state.deleteRecord(record);
                            ;
                    }
                }
            }
        }
        /* die */
        client.close();
        System.exit(0);
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
