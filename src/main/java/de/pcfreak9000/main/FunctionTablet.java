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

import org.matheclipse.core.interfaces.IExpr;

import de.pcfreak9000.main.DataTablet.DataUsage;

public class FunctionTablet implements Tablet {
    
    public static enum PropagationType {
        Linear, Gaussian;
        
        public static PropagationType get(DataUsage du) {
            switch (du) {
            case MeanAndStandardDeviation:
                return Gaussian;
            case Raw:
                return Linear;
            default:
                throw new IllegalStateException(du + "");
            }
        }
        
        public PropagationType compare(PropagationType pt) {
            switch (pt) {
            case Gaussian:
                return Gaussian;
            case Linear:
                return this;
            default:
                throw new IllegalStateException(pt + "");
            
            }
        }
    }
    
    private String function;
    private String[] args;
    
    public String getFunction() {
        return function;
    }
    
    public void setFunction(String function) {
        this.function = prepareFunction(function);
    }
    
    public String[] getArgs() {
        return args;
    }
    
    public void setArgs(String... args) {
        this.args = args;
    }
    
    public String getErrorPropFunction(PropagationType type, String... evalVars) {
        return "D" + getHeader(function) + " = " + calculateErrorPropagation(type, evalVars);
    }
    
    private IExpr calculateErrorPropagation(PropagationType type, String... evalVars) {
        if (evalVars == null || evalVars.length == 0) {
            evalVars = args;
        }
        StringBuilder b = new StringBuilder();
        b.append("D" + getHeader(function) + " = ");
        switch (type) {
        case Gaussian:
            b.append("Sqrt[");
            for (int i = 0; i < evalVars.length; i++) {
                b.append("(" + getPartial(evalVars[i]) + ")^2 * (\"D" + evalVars[i] + "\")^2"
                        + (i == evalVars.length - 1 ? "]" : " + "));
            }
            break;
        case Linear:
            for (int i = 0; i < evalVars.length; i++) {
                b.append("Abs[" + getPartial(evalVars[i]) + "]" + " * \"D" + evalVars[i] + "\""
                        + (i == evalVars.length - 1 ? "" : " + "));
            }
            break;
        default:
            throw new IllegalStateException(type + "");
        }
        return Main.evaluator().eval(b.toString());
    }
    
    private String getPartial(String v) {
        return "D[" + function + ", " + v + "]";
    }
    
    public String getHeader() {
        return getHeader(function);
    }
    
    private String getHeader(String function) {
        String f = function.split("=")[0].trim();
        if (f.contains("(")) {
            f = f.split("(")[0].trim();
        }
        return f;
    }
    
    private String prepareFunction(String function) {
        if (!function.contains("=")) {
            return "f = " + function;
        }
        return function;
    }
    
    @Override
    public String toString() {
        return function;
    }
}
