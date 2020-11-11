/*******************************************************************************
 * Copyright (C) 2020 Roman Borris
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.pcfreak9000.main;

import de.pcfreak9000.main.FunctionTablet.PropagationType;

public class DataTablet implements Tablet {
    
    public static enum DataType {//TODO Meh....
        RAW_STATISTICAL, RAW_MULTI, RESULT_STATISTICAL, RESULT_MULTI;
        
        public static DataType ofPropagation(PropagationType t) {
            switch (t) {
            case Gaussian:
                return RESULT_STATISTICAL;
            case Linear:
                return RESULT_MULTI;
            default:
                throw new IllegalStateException(t + "");
            }
        }
    }
    
    private String[] values;
    private String[] errors;
    
    private DataType type;
    
    public void setValues(String... values) {
        this.values = values;
    }
    
    public void setErrors(String... errors) {
        this.errors = errors;
    }
    
    public void setType(DataType type) {
        this.type = type;
    }
    
    public void clear() {
        this.values = null;
        this.errors = null;
    }
    
    public boolean isStatistical() {//Meh... kinda ugly
        return this.type == DataType.RAW_STATISTICAL && (errors == null || errors.length > 1);
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
    
    public String stringRepresentation() {
        String s = "Value  |  Error\n";
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                s += values[i] + "  |  " + errors[i] + "\n";
            }
        }
        return s;
    }
    
    //Read files
    //Read from console
    //Calculate errors and statistical stuff
    //One measurement vs many
    
}
