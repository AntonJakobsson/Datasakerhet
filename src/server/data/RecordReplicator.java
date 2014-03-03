package server.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import common.Record;
import common.User;


public class RecordReplicator
{
    /* Prepared Statements */
    private PreparedStatement createTable;
    private PreparedStatement insertRecord;
    private PreparedStatement findById;
    private PreparedStatement findByPatient;
    private PreparedStatement findAll;
    private PreparedStatement findByNurse;
    private PreparedStatement findByDoctor;
    private PreparedStatement deleteRecord;
    private PreparedStatement updateRecord;
    
    public RecordReplicator(Connection connection) throws SQLException
    {
        this.createTable = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS record (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "patient INTEGER, nurse INTEGER, doctor INTEGER, " +
            "data TEXT, division VARCHAR(32), active INTEGER);"
        );
        
        /* Make sure table exists */
        this.createTable.execute();
        
        this.insertRecord = connection.prepareStatement(
            "INSERT INTO `record` (patient, nurse, doctor, data, division, active) "+
            "VALUES (?, ?, ?, ?, ?, 1)",
            Statement.RETURN_GENERATED_KEYS
        );
        this.findById     = connection.prepareStatement(whereQuery("r.id=?"));
        this.findByPatient = connection.prepareStatement(whereQuery("r.patient=?"));
        this.findAll      = connection.prepareStatement(whereQuery("1=1"));
        this.deleteRecord = connection.prepareStatement(
            "UPDATE `record` SET active=0 WHERE id=?"
        );
        this.updateRecord = connection.prepareStatement(
        	"UPDATE `record` SET " +
        	"data=? " +
        	"WHERE id=?"
        );
        
        this.findByNurse = connection.prepareStatement(whereQuery("r.patient=? AND (r.nurse=? OR r.division=?)"));
        this.findByDoctor = connection.prepareStatement(whereQuery("r.patient=? AND (r.doctor=? OR r.division=?)"));
    }
    
    public void insert(Record record) throws SQLException
    {
    	if (record.getId() > 0) {
    		/* Om recordet har ett id, uppdatera istället */
    		update(record);
    		return;
    	}
    	
        insertRecord.setInt(1,    record.getPatientId());
        insertRecord.setInt(2,    record.getNurseId());
        insertRecord.setInt(3,    record.getDoctorId());
        insertRecord.setString(4, record.getData());
        insertRecord.setString(5, record.getDivision());
        insertRecord.execute();
        
        ResultSet generatedKeys = insertRecord.getGeneratedKeys();
        if (generatedKeys.next()) {
            record.setId(generatedKeys.getInt(1));
        } else {
            throw new SQLException("Creating record failed, no ID obtained.");
        }
    }
    
    public void update(Record record) throws SQLException
    {
    	if (record.getId() == 0) {
    		/* Om id saknas, gör insert istället */
    		insert(record);
    		return;
    	}
    	
    	updateRecord.setString(1, record.getData());
    	updateRecord.setInt(2,    record.getId());
    	updateRecord.execute();
    }
    
    public boolean exists(int id)
    {
    	try {
    		findById(id);
    		return true;
    	}
    	catch(SQLException ex) {
    		return false;
    	}
    }
    
    public void delete(Record record) throws SQLException
    {
        deleteRecord.setInt(1, record.getId());
        deleteRecord.execute();
    }
    
    public Record findById(int id) throws SQLException
    {
        findById.setInt(1, id);
        ResultSet results = findById.executeQuery();
        ArrayList<Record> records = getRecords(results);
        if (records.size() == 0) throw new SQLException(String.format("Record %d not found", id));
        return records.get(0);
    }
    
    public ArrayList<Record> findByPatient(User user) throws SQLException
    {
        findByPatient.setInt(1, user.getId());
        ResultSet results = findByPatient.executeQuery();
        return getRecords(results);
    }
    
    public ArrayList<Record> findByNurse(User user, User nurse) throws SQLException
    {
        findByNurse.setInt(1, user.getId());
        findByNurse.setInt(2, nurse.getId());
        findByNurse.setString(3, nurse.getDivision());
        ResultSet results = findByNurse.executeQuery();
        return getRecords(results);
    }
    
    public ArrayList<Record> findByDoctor(User user, User doctor) throws SQLException
    {
        findByDoctor.setInt(1, user.getId());
        findByDoctor.setInt(2, doctor.getId());
        findByDoctor.setString(3, doctor.getDivision());
        ResultSet results = findByDoctor.executeQuery();
        return getRecords(results);
    }
    
    public ArrayList<Record> findAll() throws SQLException
    {
        ResultSet results = findAll.executeQuery();
        return getRecords(results);
    }
    
    private String whereQuery(String where)
    {
        return String.format(
            "SELECT r.id as `id`, r.data as `data`, r.division as `division`, "+
            "p.id as `patient_id`, p.name as `patient_name`, "+
            "n.id as `nurse_id`, n.name as `nurse_name`, " +
            "d.id as `doctor_id`, d.name as `doctor_name` " +
            "FROM (((`record` r LEFT JOIN `user` p ON p.id=r.patient) "+
            "LEFT JOIN `user` n ON n.id=r.nurse) "+
            "LEFT JOIN `user` d ON d.id=r.doctor) "+
            "WHERE %s AND r.active=1 ORDER BY r.id ASC",
            where
        );
    }
    
    private ArrayList<Record> getRecords(ResultSet results) throws SQLException
    {
        ArrayList<Record> records = new ArrayList<Record>();
        while(results.next()) {
            Record record = new Record(
                results.getInt("id"),
                results.getInt("patient_id"),
                results.getInt("nurse_id"),
                results.getInt("doctor_id"),
                results.getString("patient_name"),
                results.getString("nurse_name"),
                results.getString("doctor_name"),
                results.getString("division"),
                results.getString("data")
            );
            records.add(record);
        }
        return records;
    }
}