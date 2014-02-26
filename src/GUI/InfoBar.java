package GUI;

import javax.swing.JLabel;

public class InfoBar extends JLabel
{
    private String currentUser;

    public InfoBar(String currentUser){
        super(currentUser);
        this.currentUser = currentUser;
    }
    
    public void setUser(String username){
        currentUser = username;
    }
}
