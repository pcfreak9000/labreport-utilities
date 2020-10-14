package de.pcfreak9000.main;

public class DataTablet implements Tablet {
    
    private String[] values;
    private String[] errors;
    
    private boolean sameMeasurement;
    
    public DataTablet() {
        //this.sameMeasurement = sameMeasurement;
    }
    
    public void setValues(String... values) {
        this.values = values;
    }
    
    public void setErrors(String... errors) {
        this.errors = errors;
    }
    
    public void clear() {
        this.values = null;
        this.errors = null;
    }
    
    //TODO temporary
    public String getError() {
        return errors[0];
    }
    
    public String getValue() {
        return values[0];
    }
    
    //Read files
    //Read from console
    //Calculate errors and statistical stuff
    //One measurement vs many
    
}
