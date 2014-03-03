package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import common.Record;

public class EditWindow extends JPanel
{

    private static final long serialVersionUID = -1333609331074804543L;

    protected Record          record;
    protected JTextArea       textArea;
    protected JLabel          userLabel;

    public EditWindow(Record record)
    {
        this.record = record;
        setup();
    }

    protected void setup()
    {
        this.setLayout(new BorderLayout());
        userLabel = new JLabel("Record of Patient: " + record.getPatientName());
        textArea = new JTextArea(record.getData());
        textArea.setEditable(true);
        textArea.setLineWrap(true);
        textArea.setPreferredSize(new Dimension(300, 500));
        JScrollPane scroll = new JScrollPane(textArea);

        this.add(userLabel, BorderLayout.NORTH);
        this.add(scroll);
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
                new String[] { "Save", "Close" }, "Save");
        if (option == 0) {
            save();
            return option;
        }
        else
            return option;
    }
}
