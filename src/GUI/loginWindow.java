package GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import client.NetworkState;

/**
 * Login Window
 * 
 */
public class loginWindow
{

    private NetworkState network;
    public loginWindow(NetworkState network){
        this.network = network;
        JFrame frame = new JFrame("Login");
        frame.setSize(300, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);

        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel)
    {

        panel.setLayout(null);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 40, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 40, 160, 25);
        panel.add(passwordText);
        passwordText.getPassword();

        JButton loginButton = new JButton("login");
        loginButton.setBounds(10, 80, 80, 25);
        panel.add(loginButton);
        loginButton.addActionListener(new ButtonListener(passwordText));
        
    }

    private class ButtonListener implements ActionListener
    {
        private JPasswordField pwfield;
        ButtonListener(JPasswordField pwfield){
            this.pwfield = pwfield;
        }
        public void actionPerformed(ActionEvent e)
        {
            try {
                network.auth(pwfield.getPassword().toString());
            }
            catch (InterruptedException e1) {
            }
        }
    }
}