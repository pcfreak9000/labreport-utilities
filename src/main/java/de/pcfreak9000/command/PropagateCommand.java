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
import java.util.List;
import java.util.Map;

import com.ibm.icu.impl.LocaleDisplayNamesImpl.DataTableType;

import de.pcfreak9000.main.DataTablet;
import de.pcfreak9000.main.FunctionTablet;
import de.pcfreak9000.main.FunctionTablet.PropagationType;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "propagate", aliases = "p")
public class PropagateCommand implements Runnable {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Option(names = { "-f", "--forceprop" })
    private PropagationType forcePropagation;
    
    @Option(names = { "-r", "--result" })
    private String resultTablet;
    
    @Option(names = { "-t", "--tex" })
    private boolean texify;//TODO do this here, or another command?
    
    @Option(names = { "-p", "--print" })
    private boolean printresult;
    
    @Parameters(index = "0")
    private String functionTablet;
    
    @Parameters(index = "1..*")
    private Map<String, String> tabletmap;
    //TODO create "direct" mappings so constants without error dont need a dedicated tablet
    
    //TODO only propagate errors from vars who actually have an error
    
    @Override
    public void run() {
        if (!Main.data.exists(functionTablet)) {
            System.out.println("Function tablet '" + functionTablet + "' does not exist.");
            return;
        }
        Tablet ta = Main.data.getTablet(functionTablet);
        if (!(ta instanceof FunctionTablet)) {
            System.out.println("Tablet '" + functionTablet + "' is not a function tablet.");
            return;
        }
        DataTablet resultTabletT = null;
        if (resultTablet != null) {
            if (!Main.data.exists(resultTablet)) {
                System.out.println("Data tablet '" + resultTablet + "' does not exist.");
                return;
            }
            Tablet rt = Main.data.getTablet(resultTablet);
            if (!(rt instanceof DataTablet)) {
                System.out.println("Tablet '" + resultTablet + "' is not a data tablet.");
                return;
            }
            resultTabletT = (DataTablet) rt;
        }
        FunctionTablet funct = (FunctionTablet) ta;
        String[] fargs = funct.getArgs();
        if (fargs.length != 0 && (tabletmap == null || tabletmap.size() != fargs.length)) {
            System.out.println("Number of function arguments and tablets not matching or the arguments are malformed");
            return;
        }
        //Prepare arguments, determine the propagation type
        PropagationType propagationtype = PropagationType.Linear;
        String countName = null;
        DataTablet elementCountT = null;
        List<String> nonstatargs = new ArrayList<>();
        for (int i = 0; i < fargs.length; i++) {
            DataTablet dt = (DataTablet) Main.data.getTablet(tabletmap.get(fargs[i]));
            if (!dt.isStatistical()) {
                if (elementCountT != null && elementCountT.getLength() != dt.getLength()) {
                    System.out.println("Amount of rows not matching! Expected: " + elementCountT.getLength() + "('"
                            + countName + "'), actual: " + dt.getLength() + "('" + tabletmap.get(fargs[i]) + "')");
                    return;
                }
                elementCountT = dt;
                countName = fargs[i];
                nonstatargs.add(fargs[i]);
            } else {
                Main.evaluator().defineVariable(fargs[i], Main.evaluator().eval(dt.getValue(0)));
                Main.evaluator().defineVariable("D" + fargs[i], Main.evaluator().eval(dt.getError(0)));
                propagationtype = PropagationType.Gaussian;
            }
        }
        if (this.forcePropagation != null) {
            propagationtype = this.forcePropagation;
        }
        //Finally calculate the stuff
        String errorprop = funct.getErrorPropFunction(propagationtype);
        int iterationCount = (elementCountT == null ? 1 : elementCountT.getLength());
        String[] results = new String[iterationCount];
        String[] errors = new String[iterationCount];
        System.out.println("Using error propagation: " + propagationtype);
        System.out.println("Value-Error pairs to be computed: " + iterationCount);
        for (int i = 0; i < iterationCount; i++) {
            for (int j = 0; j < nonstatargs.size(); j++) {
                DataTablet dt = (DataTablet) Main.data.getTablet(tabletmap.get(nonstatargs.get(j)));
                Main.evaluator().defineVariable(nonstatargs.get(j), Main.evaluator().eval(dt.getValue(i)));
                Main.evaluator().defineVariable("D" + nonstatargs.get(j), Main.evaluator().eval(dt.getError(i)));
            }
            results[i] = Main.evaluator().eval("N(" + funct.getFunction() + ")").toString();
            errors[i] = Main.evaluator().eval("N(" + errorprop + ")").toString();
            if (printresult) {
                System.out.println(
                        funct.getHeader() + " = " + results[i] + ", " + "D" + funct.getHeader() + " = " + errors[i]);
            }
        }
        Main.evaluator().clearVariables();//This is important, otherwise stuff might act weird
        if (resultTabletT != null) {
            resultTabletT.setValues(results);
            resultTabletT.setErrors(errors);
            //TODO resultTabletT.setType(type); 
            System.out.println("Wrote results into the tablet '" + resultTablet + "'.");
        }
        String tmp = funct.getErrorPropFunction(propagationtype);
        System.out.println(tmp);
        //                        TeXUtilities tex = new TeXUtilities(Main.evaluator().getEvalEngine(), true);
        //                        StringWriter wr = new StringWriter();
        //                        tex.toTeX(tmp, wr);
        //                        String texString = wr.toString();
        //                        for (int i = 0; i < fargs.length; i++) {
        //                            texString = texString.replace("d" + fargs[i], "\\Delta " + fargs[i]);
        //                        }
        //                        System.out.println(texString.replace("\\cdot", "").replace("{t}", "{\\omega}").replace("{x}",
        //                                "{\\Omega}"));
        
    }
    
}
