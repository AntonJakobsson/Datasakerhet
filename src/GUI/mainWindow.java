package GUI;

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
        newButton = new NewButton();
        deleteButton = new DeleteButton();
        editButton = new EditButton();
        textArea = new TextArea();
        infoBar = new InfoBar();
        
        
        this.setSize(1024, 768);
    }

}
