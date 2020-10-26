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

public class ErrorPropagation {
    
    public static enum PropagationType {
        Linear, Gaussian;
        
        public static PropagationType get(boolean stat) {
            return stat ? Gaussian : Linear;
        }
    }
    
    private String function;
    private String fheader;
    private String[] vars;
    
    private String[] partialDerivatives;
    
    public ErrorPropagation(String function, String... args) {
        this.function = prepareFunction(function);
        this.fheader = getHeader(function);
        this.vars = args;
        this.partialDerivatives = new String[args.length];
        calculatePartialDerivatives();
    }
    
    public String getErrorPropFunction(PropagationType type) {
        return "D" + fheader + " = " + calculateErrorPropagation(type);
    }
    
    private String calculateErrorPropagation(PropagationType type) {
        StringBuilder b = new StringBuilder();
        switch (type) {
        case Gaussian:
            b.append("sqrt(");
            for (int i = 0; i < this.vars.length; i++) {
                b.append("(" + partialDerivatives[i] + ")^2 * (\"D" + vars[i] + "\")^2"
                        + (i == vars.length - 1 ? ")" : " + "));
            }
            break;
        case Linear:
            for (int i = 0; i < this.vars.length; i++) {
                b.append("abs(" + partialDerivatives[i] + ")" + " * \"D" + vars[i] + "\""
                        + (i == vars.length - 1 ? "" : " + "));
            }
            break;
        default:
            throw new IllegalStateException(type + "");
        }
        return Main.evaluator().eval(b.toString()).toString();
    }
    
    public String getHeader() {
        return fheader;
    }
    
    private String getHeader(String function) {
        String f = function.split("=")[0].trim();//FIXME headerless functions
        if (f.contains("(")) {
            f = f.split("(")[0].trim();
        }
        return f;
    }
    
    private String prepareFunction(String function, String... args) {
        if (!function.contains("=")) {
            return "f = " + function;
        }
        return function;
    }
    
    private void calculatePartialDerivatives() {
        for (int i = 0; i < partialDerivatives.length; i++) {
            IExpr jv = Main.evaluator().eval("D(" + function + ", " + vars[i] + ")");
            partialDerivatives[i] = jv.toString();
        }
    }
}
