package de.pcfreak9000.main;

public class DataTablet implements Tablet {
    
    public static boolean isStatistical(DataTablet... dataTablets) {
        for (int i = 0; i < dataTablets.length; i++) {
            if (dataTablets[i].isStatistical()) {
                return true;
            }
        }
        return false;
    }
    
    private String[] values;
    private String[] errors;
    
    private boolean statistical;
    
    public void setValues(String... values) {
        this.values = values;
    }
    
    public void setErrors(String... errors) {
        this.errors = errors;
    }
    
    public void setStatistical(boolean b) {
        this.statistical = b;
    }
    
    public void clear() {
        this.values = null;
        this.errors = null;
    }
    
    public boolean isStatistical() {
        return statistical && (errors == null || errors.length > 1);
    }
    
    public int getLength() {
        return isStatistical() ? 1 : (values == null ? 0 : values.length);
    }
    
    private String createStandardDeviationEvalString() {
        StringBuilder b = new StringBuilder();
        b.append("1/sqrt(" + values.length + ") * ");
        b.append("StandardDeviation({");
        for (int i = 0; i < values.length; i++) {
            b.append(values[i]);
            if (i < values.length - 1) {
                b.append(", ");
            }
        }
        b.append("})");
        return b.toString();
    }
    
    private String createMeanEvalString() {
        StringBuilder b = new StringBuilder();
        b.append("Mean({");
        for (int i = 0; i < values.length; i++) {
            b.append(values[i]);
            if (i < values.length - 1) {
                b.append(", ");
            }
        }
        b.append("})");
        return b.toString();
    }
    
    public String getError(int index) {
        if (errors != null) {
            if (isStatistical()) {
                return Main.evaluator().eval(createStandardDeviationEvalString()).toString();
            } else {
                return errors[index];
            }
        } else {
            return null;
        }
    }
    
    public String getValue(int index) {
        if (values != null) {
            if (isStatistical()) {
                return Main.evaluator().eval(createMeanEvalString()).toString();
            } else {
                return values[index];
            }
        } else {
            return null;
        }
    }
    
    public String getValue() {
        return values[0];
    }
    
    //Read files
    //Read from console
    //Calculate errors and statistical stuff
    //One measurement vs many
    
}
