package common;

/** Represents a journal record */
public class Record
{
    private int    id;
    private int    patientId;
    private int    nurseId;
    private int    doctorId;
    private String patientName;
    private String nurseName;
    private String doctorName;
    private String division;
    private String data;

    public Record(int patientId, int nurseId, int doctorId, String division, String data)
    {
        this(0, patientId, nurseId, doctorId, "", "", "", division, data);
    }

    public Record(int id, int patientId, int nurseId, int doctorId, String patientName,
            String nurseName, String doctorName, String division, String data)
    {
        this.id = id;
        this.patientId = patientId;
        this.nurseId = nurseId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.nurseName = nurseName;
        this.doctorName = doctorName;
        this.division = division;
        this.data = data;
    }
    
    public boolean equals(Object other) 
    {
    	if (other instanceof Record) {
    		return ((Record)other).getId() == this.id;
    	}
    	return false;
    }
    
    public int getId()
    {
        return id;
    }
    
    public void setId(int id)
    {
    	this.id = id;
    }

    public String getPatientName()
    {
        return patientName;
    }
    
    public int getPatientId()
    {
        return patientId;
    }

    public String getNurseName()
    {
        return nurseName;
    }
    
    public int getNurseId()
    {
        return nurseId;
    }

    public String getDoctorName()
    {
        return doctorName;
    }
    
    public int getDoctorId()
    {
        return doctorId;
    }
    
    public String getDivision()
    {
        return division;
    }
    
    public void setData(String newData)
    {
    	this.data = newData;
    }
    
    public String getData()
    {
        return data;
    }
    
    /* Null object */
    public static class None extends Record
    {
    	public None() {
    		super(0, 0, 0, "", "");
    	}
    }
}
