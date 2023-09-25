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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.matheclipse.core.eval.ExprEvaluator;

import de.pcfreak9000.main.FunctionTablet;
import de.pcfreak9000.main.FunctionTablet.PropagationType;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "tex", description = "Prints the tex expression of either a function or its error propagation.")
public class TexCommand implements Callable<Integer> {
    private static final String TEX_PARTIAL_ERROR_PROP_SYMBOL = "E";
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Option(names = { "-p",
            "--propagation" }, paramLabel = "<propagation type>", description = "Prints the texform of the error propagation as specified. If the option is left out, the texform of the function will be printed instead. Valid values (if specified): ${COMPLETION-CANDIDATES}")
    private PropagationType propType;
    
    @Option(names = { "-v",
            "--variable" }, paramLabel = "<variable>", description = "Specify the variables to perform the propagation on. If no propagation is specified, these options are ignored. If these options are left empty, the result will contain all function arguments.", split = ";")
    private String[] variables;
    
    @Parameters(index = "0", paramLabel = "<FUNCTION_TABLET>", description = "A function tablet to perform the operations on.")
    private String functionTablet;
    
    @Parameters(index = "1..*")
    private Map<String, String> varMap;
    
    @Option(names = { "-s", "--split" }, defaultValue = "0")
    private int split;//0 -> no split, otherwise group size
    
    @Override
    public Integer call() {
        if (!Main.data.exists(functionTablet)) {
            System.err.println("Function tablet '" + functionTablet + "' does not exist.");
            return Main.CODE_ERROR;
        }
        Tablet ta = Main.data.getTablet(functionTablet);
        if (!(ta instanceof FunctionTablet)) {
            System.err.println("Tablet '" + functionTablet + "' is not a function tablet.");
            return Main.CODE_ERROR;
        }
        FunctionTablet function = (FunctionTablet) ta;
        if (propType == null) {
            System.out.println("TeXForm of the function '" + function.getFunctionOriginal() + "':\n "
                    + prepareRawTexString(
                            Main.evaluator().eval("TeXForm[" + function.getFunctionInternal() + "]").toString(),
                            function));
        } else {
            if (variables == null) {
                variables = function.getInternalArgs();
            } else {
                for (int i = 0; i < variables.length; i++) {
                    variables[i] = function.getInternalFromVar(variables[i]);
                }
            }
            String prop = function.getErrorPropFunction(propType, variables);
            String stt = prop;
            for (String in : function.getInternalArgs()) {
                stt = stt.replace(in, function.getVarFromInternal(in));
            }
            System.out.println("TeXForm of the error propagation of the function '" + function.getFunctionOriginal()
                    + "' with respect to the variables " + Arrays.toString(function.getVarArgs()) + ":");
            System.out.println(stt);//TODO make this better
            if (split == 0) {
                ExprEvaluator eval = Main.getNewEval();
                String texString = eval.eval("TeXForm[" + prop + "]").toString().replaceAll(prop, stt);
                texString = prepareDeltaTexString(texString, variables);
                texString = prepareRawTexString(texString, function);
                System.out.println(" " + texString);
            } else {
                String[] groups = function.getErrorPropFunctionSplit(propType, split, variables);
                String[] res = toSplitTex(propType, groups, variables, function);
                for (String s : res) {
                    System.out.println(" " + s);
                }
            }
        }
        return Main.CODE_NORMAL;
    }
    
    private String prepareDeltaTexString(String in, String[] variables) {
        for (int i = 0; i < variables.length; i++) {
            in = in.replace("d" + variables[i].toLowerCase(), "\\Delta " + variables[i]);//Well well well, oof.
        }
        return in;
    }
    
    private String prepareRawTexString(String in, FunctionTablet ft) {
        in = in.replace("^{1}", "");//The fuck
        in = in.replace("\\left(", "(").replace("\\right)", ")").replace("(", "\\left(").replace(")", "\\right)");
        for (String var : ft.getVarArgs()) {
            String texRepl = varMap == null ? null : varMap.get(var);
            if (texRepl == null) {
                texRepl = var;
            }
            String internal = ft.getInternalFromVar(var);
            if (internal != null) {
                in = in.replace(internal, texRepl);
            }
        }
        //TODO remove unnecessary () in \sqrt{}
        //TODO somehow turn | into \left| and \right|
        return in;
    }
    
    private String[] toSplitTex(PropagationType type, String[] groups, String[] vars, FunctionTablet ft) {
        String[] res = new String[groups.length + 1];
        StringBuilder mainBuilder = new StringBuilder();
        switch (type) {
        case Gaussian:
            mainBuilder.append("\\sqrt{");
            break;
        case Linear:
            break;
        default:
            throw new IllegalArgumentException(Objects.toString(type));
        }
        ExprEvaluator eval = Main.getNewEval();
        for (int i = 1; i < res.length; i++) {
            String symb = "{" + TEX_PARTIAL_ERROR_PROP_SYMBOL + "}_{" + i + "}";
            String groupTex = eval.eval("TeXForm[" + groups[i - 1] + "]").toString();
            groupTex = prepareDeltaTexString(groupTex, vars);
            groupTex = prepareRawTexString(groupTex, ft);
            res[i] = symb + " = " + groupTex;
            if (i != 1) {
                mainBuilder.append(" + ");
            }
            mainBuilder.append(symb);
        }
        switch (type) {
        case Gaussian:
            mainBuilder.append("}");
            break;
        case Linear:
            break;
        default:
            throw new IllegalArgumentException(Objects.toString(type));
        }
        res[0] = mainBuilder.toString();
        return res;
    }
    
}
