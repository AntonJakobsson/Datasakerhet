package GUI;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        infoBar = new InfoBar(getUser());

        JFrame frame = new JFrame("Awesome medical journal system");
        frame.setSize(1024, 768);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);

        frame.pack();
        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel)
    {
        panel.setLayout(new BorderLayout());
        panel.add(infoBar, BorderLayout.PAGE_START);
        JPanel textAndButtonPanel = new JPanel(new BorderLayout());
        panel.add(textAndButtonPanel, BorderLayout.PAGE_END);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(newButton, BorderLayout.NORTH);
        buttonPanel.add(deleteButton, BorderLayout.EAST);
        buttonPanel.add(editButton, BorderLayout.SOUTH);
        textAndButtonPanel.add(buttonPanel, BorderLayout.WEST);
        textAndButtonPanel.add(textArea, BorderLayout.EAST);

    }

    public String getUser()
    {
        // return getcurrentuserfromloginwindow();
        return null;
    }
}
