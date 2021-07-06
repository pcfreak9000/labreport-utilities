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

import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

import de.pcfreak9000.main.FunctionTablet.PropagationType;

public class DataTablet implements Tablet {
    
    public static enum DataUsage {
        Raw, MSD/* Mean and standard deviation */
    }
    
    private IExpr[] values;
    private IExpr[] errors;
    
    private PropagationType preferredPropagation = PropagationType.Linear;
    private DataUsage dataUsage;
    
    public void setValues(IExpr... values) {
        this.values = values;
    }
    
    public void setErrors(IExpr... errors) {
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
    
    public int getLengthRaw() {
        return values == null ? 0 : values.length;
    }
    
    public DataUsage getDataUsage() {
        return dataUsage;
    }
    
    public void setDataUsage(DataUsage dataUsage) {
        this.dataUsage = dataUsage;
    }
    
    private IExpr createStandardDeviationEvalString() {
        IExpr e = F.eval("{}");
        for (int i = 0; i < values.length; i++) {
            e = F.eval(F.Append(e, values[i]));
        }
        return F.eval(F.StandardDeviation(e));
    }
    
    private IExpr createMeanEvalString() {
        IExpr e = F.eval("{}");
        for (int i = 0; i < values.length; i++) {
            e = F.eval(F.Append(e, values[i]));
        }
        return F.eval(F.Mean(e));
    }
    
    public IExpr getError(int index) {
        if (errors != null) {
            if (getDataUsage() == DataUsage.MSD) {
                return createStandardDeviationEvalString();
            } else {
                return errors.length == 1 ? errors[0] : errors[index];
            }
        } else {
            return null;
        }
    }
    
    public IExpr getValue(int index) {
        if (values != null) {
            if (getDataUsage() == DataUsage.MSD) {
                return createMeanEvalString();
            } else {
                return values[index];
            }
        } else {
            return null;
        }
    }
    
    public IExpr getErrorRaw(int index) {
        if (errors != null) {
            return errors[index];
        }
        return null;
    }
    
    public IExpr getValueRaw(int index) {
        if (values != null) {
            return values[index];
        }
        return null;
    }
    
    public boolean hasError() {
        if (errors != null) {
            for (IExpr e : errors) {
                if (!F.eval(F.Equal(F.num(0), e)).isTrue()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private String stringRepresentation() {
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
