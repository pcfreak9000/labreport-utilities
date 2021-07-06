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
package de.pcfreak9000.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

import de.pcfreak9000.main.DataTablet;
import de.pcfreak9000.main.DataTablet.DataUsage;
import de.pcfreak9000.main.FunctionTablet;
import de.pcfreak9000.main.FunctionTablet.PropagationType;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

//TODO DOCument support of mathematical expressions in sete
@Command(name = "propagate", aliases = "p", description = "Computes values and errors with the given function- and data tablets.")
public class PropagateCommand implements Callable<Integer> {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Option(names = { "-f",
            "--forceprop" }, paramLabel = "<propagation type>", description = "Use this option to force a certain type of error propagation. Valid values: ${COMPLETION-CANDIDATES}")
    private PropagationType forcePropagation;
    
    @Option(names = { "-r",
            "--result" }, paramLabel = "<data tablet>", description = "Use this option to set a data tablet to store the result of the computation in.")
    private String resultTablet;
    
    @Option(names = { "-p",
            "--print" }, description = "Specify this flag to immediately print the result to the output.")
    private boolean printresult;
    
    @Parameters(index = "0", paramLabel = "<FUNCTION_TABLET>", description = "A function tablet to perform the calculations on.")
    private String functionTablet;
    
    @Parameters(index = "1..*", paramLabel = "<variable>=<data tablet>", description = "Map variables used in the function to data tablets. Values for the variables will be pulled from the specified data tablet.") //TODO better description concerning datausage stuff with the tablets
    private Map<String, String> tabletmap;
    
    @Option(names = { "-n",
            "--precision" }, defaultValue = "6", paramLabel = "<precision>", description = "floating point precision usedto calculate values")
    private int precision;
    
    //TODO create "direct" mappings so constants without error dont need a dedicated tablet
    
    @Override
    public Integer call() {
        if (!Main.data.exists(functionTablet)) {
            System.err.println("Tablet '" + functionTablet + "' does not exist.");
            return Main.CODE_ERROR;
        }
        Tablet ta = Main.data.getTablet(functionTablet);
        if (!(ta instanceof FunctionTablet)) {
            System.err.println("Tablet '" + functionTablet + "' is not a function tablet.");
            return Main.CODE_ERROR;
        }
        DataTablet resultTabletT = null;
        if (resultTablet != null) {
            if (!Main.data.exists(resultTablet)) {
                System.out.println("Created the data tablet '" + resultTablet + "'.");
                Main.data.createDataTablet(resultTablet);
            }
            Tablet rt = Main.data.getTablet(resultTablet);
            if (!(rt instanceof DataTablet)) {
                System.err.println("Tablet '" + resultTablet + "' is not a data tablet.");
                return Main.CODE_ERROR;
            }
            resultTabletT = (DataTablet) rt;
        }
        FunctionTablet funct = (FunctionTablet) ta;
        String[] fargs = funct.getInternalArgs();
        if (tabletmap == null) {
            tabletmap = new HashMap<>();//TODO move that stuff in special function
        }
        //        if (fargs.length != 0 && (tabletmap == null || tabletmap.size() != fargs.length)) {
        //            System.err.println("Number of function arguments and tablets not matching or the arguments are malformed");
        //            return Main.CODE_ERROR;
        //        }
        //Prepare arguments, determine the propagation type
        PropagationType propagationtype = PropagationType.Linear;
        String countName = null;
        DataTablet elementCountT = null;
        List<String> nonstatargs = new ArrayList<>();
        ExprEvaluator evaluator = new ExprEvaluator();
        Set<String> haserror = new HashSet<>();
        for (int i = 0; i < fargs.length; i++) {
            String varFromInternal = funct.getVarFromInternal(fargs[i]);
            String tabletmapping = tabletmap.get(varFromInternal);
            String tabletName = tabletmapping == null ? varFromInternal : tabletmapping;
            DataTablet dt = (DataTablet) Main.data.getTablet(tabletName);//Check if existing
            if (dt == null) {
                System.err.println("Invalid tablet '" + tabletName + "' for function argument '" + fargs[i] + "'.");
                return Main.CODE_ERROR;
            }
            if (dt.getLength() > 1 && dt.getDataUsage() == DataUsage.Raw) {
                if (elementCountT != null && elementCountT.getLength() != dt.getLength()) {
                    System.err.println("Amount of rows not matching! Expected: " + elementCountT.getLength() + "('"
                            + countName + "'), actual: " + dt.getLength() + "('" + tabletmap.get(fargs[i]) + "')");
                    return Main.CODE_ERROR;
                }
                elementCountT = dt;
                countName = fargs[i];
                nonstatargs.add(fargs[i]);
            } else {
                evaluator.defineVariable(fargs[i], dt.getValue(0));
                evaluator.defineVariable("D" + fargs[i], dt.getError(0));//Parse was used...
            }
            if (dt.hasError()) {
                haserror.add(fargs[i]);
            }
            propagationtype = propagationtype.compare(dt.getPreferredPropagation());
        }
        if (this.forcePropagation != null) {
            propagationtype = this.forcePropagation;
        }
        //Finally calculate the stuff
        String errorprop = funct.getErrorPropFunction(propagationtype, haserror.toArray(String[]::new));
        int iterationCount = (elementCountT == null ? 1 : elementCountT.getLength());
        IExpr[] results = new IExpr[iterationCount];
        IExpr[] errors = new IExpr[iterationCount];
        System.out.println("Using error propagation: " + propagationtype);
        System.out.println("Value-Error pairs to be computed: " + iterationCount);
        for (int i = 0; i < iterationCount; i++) {
            for (int j = 0; j < nonstatargs.size(); j++) {
                String varfrominternal = funct.getVarFromInternal(nonstatargs.get(j));
                String tabletmapping = tabletmap.get(varfrominternal);
                String tabletname = tabletmapping == null ? varfrominternal : tabletmapping;
                DataTablet dt = (DataTablet) Main.data.getTablet(tabletname);
                evaluator.defineVariable(nonstatargs.get(j), dt.getValue(i));
                evaluator.defineVariable("D" + nonstatargs.get(j), dt.getError(i));//parse was used
            }
            boolean err = false;
            try {
                IExpr resultExpr = evaluator.eval("N[" + funct.getFunctionInternal() + ", " + precision + "]");
                results[i] = resultExpr;
            } catch (Exception e) {
                err = true;
                results[i] = F.eval("NaN");
            }
            try {
                IExpr errorExpr = evaluator.eval("N[" + errorprop + ", " + precision + "]");
                errors[i] = errorExpr;
            } catch (Exception e) {
                err = true;
                errors[i] = F.eval("NaN");
            }
            if (err) {
                System.out.println("Error. Some Values are invalid.");
            }
            if (printresult) {
                System.out.println("f = " + results[i] + ", " + "Df = " + errors[i]);
            }
        }
        //evaluator.clearVariables();//This is important, otherwise stuff might act weird -> local evaluator
        if (resultTabletT != null) {
            resultTabletT.setValues(results);
            resultTabletT.setErrors(errors);
            resultTabletT.setPreferredPropagation(propagationtype);
            resultTabletT.setDataUsage(DataUsage.Raw);//TODO make this toggable 
            System.out.println("Wrote results into the tablet '" + resultTablet + "'.");
        }
        return Main.CODE_NORMAL;
    }
    
    private IExpr getPr(IExpr in, int prec) {
        return in;
        //        IExpr e = EvalEngine.get().parse("N[]").apply(in, F.num(prec));
        //        System.out.println(F.eval(e));
        //        return F.eval(e);
    }
    
}
