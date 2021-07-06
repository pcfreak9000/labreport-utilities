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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.pcfreak9000.main.DataTablet.DataUsage;

public class FunctionTablet implements Tablet {
    
    public static enum PropagationType {
        Linear, Gaussian;
        
        public static PropagationType get(DataUsage du) {
            switch (du) {
            case MSD:
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
    private String varfunc;
    private BiMap<String, String> varMap = HashBiMap.create();
    
    public String getFunctionInternal() {
        return function;
    }
    
    public String getFunctionOriginal() {
        return varfunc;
    }
    
    public void setFunction(String funct) {
        this.varfunc = funct;
        IExpr e = F.eval(funct);
        Set<String> vars = new HashSet<>();
        e = e.replace((p) -> p.isAtom() && !p.isBuiltInSymbol() && !p.isNumber(), (in) -> {
            String repl = "\"variable" + in + "\"";
            IExpr replexpr = F.eval(repl);
            vars.add(replexpr.toString());
            varMap.put(in.toString(), replexpr.toString());
            return replexpr;
        });
        this.function = e.toString();
    }
    
    public String[] getVarArgs() {
        return varMap.keySet().toArray(String[]::new);
    }
    
    public String[] getInternalArgs() {
        return varMap.values().toArray(String[]::new);
    }
    
    public String getVarFromInternal(String internal) {
        return varMap.inverse().get(internal);
    }
    
    public String getInternalFromVar(String var) {
        return varMap.get(var);
    }
    
    public void setArgs(String... args) {
        // this.args = args;
    }
    
    public String getErrorPropFunction(PropagationType type, String... evalVars) {
        return calculateErrorPropagation(type, evalVars).toString();
    }
    
    public String[] getErrorPropFunctionSplit(PropagationType type, int groupSize, String... evalVars) {
        int groupCount = (int) Math.ceil(evalVars.length / (double) groupSize);
        String[] groups = new String[groupCount];
        String[] partials = getErrorPropPartials(type, evalVars);
        ExprEvaluator eval = new ExprEvaluator();
        for (int i = 0; i < groups.length; i++) {
            int baseIndex = i * groupSize;
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < groupSize && baseIndex + j < partials.length; j++) {
                int partialIndex = baseIndex + j;
                String partial = partials[partialIndex];
                if (j != 0) {
                    builder.append("+");
                }
                builder.append(partial);
            }
            groups[i] = eval.eval("simplify [" + builder.toString() + "]").toString();
        }
        return groups;
    }
    
    public String[] getErrorPropPartials(PropagationType type, String... evalVars) {
        String[] res = new String[evalVars.length];
        for (int i = 0; i < res.length; i++) {
            switch (type) {
            case Gaussian:
                res[i] = "(" + getPartial(evalVars[i]) + ")^2 * (\"D" + evalVars[i] + "\")^2";
                break;
            case Linear:
                res[i] = "Abs(" + getPartial(evalVars[i]) + ")" + " * \"D" + evalVars[i] + "\"";
                break;
            default:
                throw new IllegalArgumentException(Objects.toString(type));
            }
        }
        return res;
    }
    
    public IExpr calculateErrorPropagation(PropagationType type, String... evalVars) {
        if (evalVars == null || evalVars.length == 0) {
            evalVars = getInternalArgs();
        }
        String[] partials = getErrorPropPartials(type, evalVars);
        StringBuilder b = new StringBuilder();
        ExprEvaluator eval = new ExprEvaluator();
        IExpr ex = null;
        switch (type) {
        case Gaussian:
            IExpr e = F.num(0);
            b.append("Sqrt(");
            for (int i = 0; i < evalVars.length; i++) {
                e = e.add(eval.eval(partials[i]));
                b.append(partials[i] + (i == evalVars.length - 1 ? ")" : " + "));
            }
            ex = F.Sqrt(e);
            break;
        case Linear:
            ex = F.num(0);
            for (int i = 0; i < evalVars.length; i++) {
                ex = ex.add(eval.eval(partials[i]));
                b.append(partials[i] + (i == evalVars.length - 1 ? "" : " + "));
            }
            break;
        default:
            throw new IllegalArgumentException(Objects.toString(type));
        }
        return eval.eval(F.Simplify(ex));
        //FIXME fix too long(?) expressions for the simplify...
    }
    
    private String getPartial(String v) {
        return "D(" + function + ", " + v + ")";
    }
    
    @Override
    public String toString() {
        return function;
    }
}
