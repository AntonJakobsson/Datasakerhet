package GUI;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class mainWindow extends JPanel
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
        JFrame frame = new JFrame("Awesome medical journal system");
        frame.setSize(1024,768);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        placeComponents(this);
        
    }
    private void placeComponents(JPanel panel) {
        this.setLayout(new GridLayout());
        newButton = new NewButton();
        deleteButton = new DeleteButton();
        editButton = new EditButton();
        textArea = new TextArea();
        infoBar = new InfoBar(getUser());
        
    }
    public String getUser(){
        return getcurrentuserfromloginwindow();
    }
}
