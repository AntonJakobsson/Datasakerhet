package GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class EditButton extends JButton implements ActionListener
{

    public EditButton()
    {
        super("Edit");
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
       //textArea.setEditable(true);

    }

}
