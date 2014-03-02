package client.gui;

import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;

import common.User;

public class SelectUserWindow extends JPanel
{
    private static final long serialVersionUID = -82329104815111890L;
    
    private JTable table;
    private ArrayList<User> userList;
    private User currentUser;
    private JLabel userLabel;
    
    public SelectUserWindow(User currentUser, ArrayList<User> userList)
    {
        this.currentUser = currentUser;
        this.userList = userList;
        setup();
    }
    
    protected void setup()
    {
        userLabel = new JLabel("Logged in as: " + currentUser.toString());
        this.add(userLabel);
        table = new JTable(userList.size(), 1);
        JScrollPane scroll = new JScrollPane(table);
        table.setEnabled(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fillTable(table, userList);
        this.add(scroll);
        this.add(table);
        
    }
    protected void fillTable(JTable table, ArrayList<User> userlist)
    {
        String name;
        int index=0;
        for(User user: userlist){
            name = user.getName();
            table.getModel().setValueAt(name,index,0);
            index++;
        }
    }
    
    public User getSelectedUser(){
        return userList.get(table.getSelectedRow());
    }
    
    protected void reset()
    {
        table.removeAll();
    }
    
    public int showDialog() 
    {
        reset();
        return JOptionPane.showOptionDialog(null, this, "Select patient",
               JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
               new String[] { "Ok", "Logout" }, "Ok");
    }
}
