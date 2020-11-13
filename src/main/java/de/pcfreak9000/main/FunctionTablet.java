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

public class FunctionTablet implements Tablet {
    
    public static enum PropagationType {
        Linear, Gaussian;
        
        public static PropagationType get(boolean stat) {
            return stat ? Gaussian : Linear;
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
    
    public String getErrorPropFunction(PropagationType type) {
        return "D" + getHeader(function) + " = " + calculateErrorPropagation(type);
    }
    
    private String calculateErrorPropagation(PropagationType type, String... evalVars) {
        if (evalVars == null || evalVars.length == 0) {
            evalVars = args;
        }
        StringBuilder b = new StringBuilder();
        switch (type) {
        case Gaussian:
            b.append("sqrt(");
            for (int i = 0; i < evalVars.length; i++) {
                b.append("(" + evalPartial(evalVars[i]) + ")^2 * (\"D" + evalVars[i] + "\")^2"
                        + (i == evalVars.length - 1 ? ")" : " + "));
            }
            break;
        case Linear:
            for (int i = 0; i < evalVars.length; i++) {
                b.append("abs(" + evalPartial(evalVars[i]) + ")" + " * \"D" + evalVars[i] + "\""
                        + (i == evalVars.length - 1 ? "" : " + "));
            }
            break;
        default:
            throw new IllegalStateException(type + "");
        }
        return Main.evaluator().eval(b.toString()).toString();
    }
    
    private String evalPartial(String v) {
        IExpr jv = Main.evaluator().eval("D(" + function + ", " + v + ")");
        return jv.toString();
    }
    
    @Deprecated
    public String getHeader() {
        return getHeader(function);
    }
    
    private String getHeader(String function) {
        String f = function.split("=")[0].trim();//FIXME headerless functions
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
}
