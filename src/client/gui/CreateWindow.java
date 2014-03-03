package client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import common.Record;
import common.User;

public class CreateWindow extends JPanel
{

    private static final long serialVersionUID = -152185454850047281L;
    private User              currentUser;
    private ArrayList<User>   nurseList;
    private JTextField        division;
    private JTextArea         recordInformation;
    private User              selectedNurse;
    private User              patient;
    private selectNurseButton selectNurse;

    public CreateWindow(User currentUser, User patient, ArrayList<User> nurseList)
    {
        this.currentUser = currentUser;
        this.nurseList = nurseList;
        this.patient = patient;
        setup();
    }

    protected void setup()
    {
        JLabel divisionLabel = new JLabel("Division:");
        division = new JTextField();
        division.setEditable(true);

        this.add(divisionLabel);
        this.add(division);

        JLabel recordLabel = new JLabel("Record:");
        recordInformation = new JTextArea();
        recordInformation.setEditable(true);

        this.add(recordLabel);
        this.add(recordInformation);
        
        selectNurse = new selectNurseButton();
        this.add(selectNurse);
    }

    public Record getCreatedRecord()
    {
        return new Record(patient.getId(), selectedNurse.getId(), currentUser.getId(), division.getText(),
                recordInformation.getText());
    }

    protected void reset()
    {

    }

    public int showDialog()
    {
        reset();
        return JOptionPane.showOptionDialog(null, this, "Create record",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[] { "Create", "Cancel" }, "Create");
    }
    
    private class selectNurseButton extends JButton implements ActionListener{
        
        private static final long serialVersionUID = -7980331329742864631L;
        
        public selectNurseButton(){
            super("Select nurse");
        }

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
           SelectUserWindow selectWindow = new SelectUserWindow(currentUser,nurseList);
           if(selectWindow.showDialog() == 0){
               selectedNurse = selectWindow.getSelectedUser();
           }
           else{
               //no nurse selected, cancel was pressed
           }
        }
        
    }
}
