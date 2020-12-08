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
    
    public static enum DataUsage {
        Raw, MSD/* Mean and standard deviation */
    }
    
    private String[] values;
    private String[] errors;
    
    private PropagationType preferredPropagation = PropagationType.Linear;
    private DataUsage dataUsage;
    
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
    
    public void setPreferredPropagation(PropagationType pt) {
        this.preferredPropagation = pt;
    }
    
    public PropagationType getPreferredPropagation() {
        return preferredPropagation;
    }
    
    public int getLength() {
        return getDataUsage() == DataUsage.Raw ? (values == null ? 0 : values.length) : 1;//Well well well...
    }
    
    public DataUsage getDataUsage() {
        return dataUsage;
    }
    
    public void setDataUsage(DataUsage dataUsage) {
        this.dataUsage = dataUsage;
    }
    
    private String createStandardDeviationEvalString() {
        StringBuilder b = new StringBuilder();
        b.append("1/Sqrt(" + values.length + ") * ");
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
            if (getDataUsage() == DataUsage.MSD) {
                return Main.evaluator().eval(createStandardDeviationEvalString()).toString();
            } else {
                return errors.length == 1 ? errors[0] : errors[index];
            }
        } else {
            return "0";
        }
    }
    
    public String getValue(int index) {
        if (values != null) {
            if (getDataUsage() == DataUsage.MSD) {
                return Main.evaluator().eval(createMeanEvalString()).toString();
            } else {
                return values[index];
            }
        } else {
            return null;
        }
    }
    
    public String stringRepresentation() {
        String s = "Format: Value; Error\n";
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                s += values[i] + "; " + errors[i] + "\n";
            }
        }
        return s.trim();
    }
    
    @Override
    public String toString() {
        return stringRepresentation();
    }
    
}
