package GUI;

import javax.swing.JTextField;

public class InfoBar extends JTextField
{
    private String currentUser;

    public InfoBar(){
        
    }
    
    public void setUser(String username){
        currentUser = username;
    }
}
