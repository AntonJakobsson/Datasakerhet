package client.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import common.Record;
import common.User;

public class RecordChooseWindow extends JPanel
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	protected JTable			table;
	protected ArrayList<Record>	records;
	protected User				user;
	public static final int MESSAGE_VIEW = 3;
	public static final int MESSAGE_DELETE = 2;
	public static final int MESSAGE_NEW = 1;
	public static final int MESSAGE_CANCEL = 0;
	private final String[] columnames = { "Record ID", "Doctor", "Nurse", "Division" };

	public RecordChooseWindow(User user, ArrayList<Record> records)
	{
		this.records = records;
		this.user = user;
		table = fillMatrix(this.records);
		setup();
	}

	private void setup()
	{
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setLayout(new GridLayout(2, 1));
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(10, 80));
		scrollpane.setColumnHeaderView(new JLabel("HEj"));
		this.add(new JLabel("Patient: " + user.getName()));
		this.add(scrollpane);
	}

	/**
	 * Ritar om table naer nya records tillkommit
	 * @param newRecords updated recordslist
	 */
	public void updateWindow(ArrayList<Record> newRecords){
		reset();
		table = fillMatrix(newRecords);
		setup();
	}
	
	private JTable fillMatrix(ArrayList<Record> newRecords)
	{
		Object[][] data = new Object[newRecords.size()][4];
		for (int i = 0; i < newRecords.size(); i++)
		{
			Record record = newRecords.get(i);
			data[i][0] = record.getId();
			data[i][1] = record.getDoctorName();
			data[i][2] = record.getNurseName();
			data[i][3] = record.getDivision();
		}
		return new JTable(data, columnames);
	}

	private void reset(){
	    DefaultTableModel model = (DefaultTableModel) table.getModel();
	    model.setRowCount(0);
	}
	
	public Record getSelectedRecord()
	{
	    if(table.getSelectedRow() == -1){
	        throw new RuntimeException("No record selected!");
	    }
		return records.get(table.getSelectedRow());
	}

	public int showDialog()
	{
		return JOptionPane.showOptionDialog(null, this, "Records", JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, new String[] {"Close", "New", "Delete", "View" }, "Ok");
	}
}
