package client.gui;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import common.Record;

public class EditWindow extends JPanel
{

    private static final long serialVersionUID = -1333609331074804543L;

    protected Record record;
    protected JTextArea textArea;
    
    public EditWindow(Record record)
    {
        this.record = record;
        setup();
    }

    protected void setup()
    {
        textArea = new JTextArea(record.getData());
        JScrollPane scroll = new JScrollPane(textArea);
        textArea.setEditable(true);
        textArea.setSize(500,300);
        
        this.add(scroll);
        this.add(textArea);
    }

    protected void save()
    {
        record.setData(textArea.getText());
    }

    protected void reset()
    {
        
    }

    public int showDialog()
    {
        reset();
        
        int option = JOptionPane.showOptionDialog(null, this, "Edit record",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[] { "Save", "Cancel" }, "Save");
        if(option==0){
            save();
            return option;
        }
        else return option;
    }
}
