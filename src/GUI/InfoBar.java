package GUI;

import javax.swing.JLabel;

public class InfoBar extends JLabel
{
    private String              currentUser;
    private static final String prefix = "Currently logged in as: ";

    public InfoBar(String currentUser)
    {
        super(prefix + currentUser);
        this.currentUser = currentUser;
        this.setSize(500, 10);
    }

    public void setUser(String username)
    {
        currentUser = username;
        this.setText(prefix + currentUser);
    }
}
