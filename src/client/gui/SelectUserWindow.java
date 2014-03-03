package client.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
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
    private String title;
    
    public SelectUserWindow(User currentUser, ArrayList<User> userList, String title)
    {
        this.currentUser = currentUser;
        this.userList = userList;
        this.title = title;
        setup();
    }
    
    protected void setup()
    {
        userLabel = new JLabel("Logged in as: " + currentUser.getName());
        Object[][] data = new Object[userList.size()][1];
        for(int i =0; i<userList.size();i++){
            data[i][0] = userList.get(i).getName();
        }
        table = new JTable(data, new String[] {"Name"});
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(100,90));
        table.setEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fillTable(table, userList);
        this.setLayout(new GridLayout(2,1));
        this.add(userLabel);
        this.add(scroll);
        
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
    
    public User getSelectedUser() throws RuntimeException{
        if(table.getSelectedRow() == -1){
            throw new RuntimeException("No user selected!");
        }
        return userList.get(table.getSelectedRow());
    }
    
    protected void reset()
    {
        table.removeAll();
    }
    
    public int showDialog() 
    {
        reset();
        return JOptionPane.showOptionDialog(null, this, title,
               JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
               new String[] { "Ok", "Logout" }, "Ok");
    }
}
