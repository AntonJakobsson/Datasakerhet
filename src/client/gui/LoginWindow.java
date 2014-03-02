package client.gui;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class LoginWindow extends JPanel
{
	private static final long serialVersionUID = 2801223783826767139L;
	
	protected JPasswordField passwordField;
	
	public LoginWindow()
	{
		setup();
	}
	
	protected void setup()
	{
		passwordField = new JPasswordField(20);
		this.add(passwordField);
	}
	
	protected void reset()
	{
		passwordField.setText("");
	}
	
	public String getPassword()
	{
		return new String(passwordField.getPassword());
	}
	
	public int showDialog() 
	{
		reset();
	    return JOptionPane.showOptionDialog(null, this, "Login",
	           JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
	           new String[] { "Ok", "Quit" }, "Ok");
	}
}
