package GUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class mainWindow
{
    private NewButton    newButton;
    private DeleteButton deleteButton;
    private EditButton   editButton;
    private TextArea     textArea;
    private InfoBar      infoBar;

    /**
     * Initializes GUI Window
     */
    public mainWindow()
    {
        newButton = new NewButton();
        deleteButton = new DeleteButton();
        editButton = new EditButton();
        textArea = new TextArea();
        infoBar = new InfoBar(getUser());
        
        JFrame frame = new JFrame("Awesome medical journal system");
        frame.setSize(1024,768);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);
        
        frame.pack();
        frame.setVisible(true);
    }
    
    private void placeComponents(JPanel panel) {
        panel.setLayout(new BorderLayout());
        
        panel.add(newButton,BorderLayout.WEST);
        panel.add(deleteButton,BorderLayout.WEST);
        panel.add(editButton,BorderLayout.WEST);
        panel.add(textArea,BorderLayout.EAST);
        panel.add(infoBar,BorderLayout.NORTH);
        
        
    }
    public String getUser(){
      //return getcurrentuserfromloginwindow();
        return null;
    }
}
