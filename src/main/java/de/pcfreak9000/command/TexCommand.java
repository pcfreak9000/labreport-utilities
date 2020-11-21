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

import de.pcfreak9000.main.FunctionTablet;
import de.pcfreak9000.main.FunctionTablet.PropagationType;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "tex", description = "Prints the tex expression of either a function or its error propagation.")
public class TexCommand implements Runnable {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Option(names = { "-p",
            "--propagation" }, paramLabel = "<propagation type>", description = "Prints the texform of the error propagation as specified. If the option is left out, the texform of the function will be printed instead. Valid values (if specified): ${COMPLETION-CANDIDATES}")
    private PropagationType propType;
    
    @Option(names = { "-v",
            "--variable" }, paramLabel = "<variable>", description = "Specify the variables to perform the propagation on. If no propagation is specified, these options are ignored.")
    private String[] variables;
    
    @Parameters(index = "0", paramLabel = "<FUNCTION_TABLET>", description = "A function tablet to perform the operations on.")
    private String functionTablet;
    
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
        FunctionTablet function = (FunctionTablet) ta;
        if (propType == null) {
            System.out.println("TeXForm of the function " + function.getFunction() + ": "
                    + Main.evaluator().eval("TeXForm[" + function.getFunction() + "]").toString().replace("^{1}", ""));//The fuck
        } else {
            if (variables == null) {
                variables = function.getArgs();
            }
            String prop = function.getErrorPropFunction(propType, variables);
            String texString = Main.evaluator().eval("TeXForm[" + prop + "]").toString();
            for (int i = 0; i < variables.length; i++) {
                texString = texString.replace("d" + variables[i].toLowerCase(), "\\Delta " + variables[i]);//Well well well, oof.
            }
            System.out.println("TeXForm of the error propagation of the function " + function.getFunction()
                    + " concerning the variables " + Arrays.toString(variables) + ": " + texString.replace("^{1}", ""));//The fuck
        }
    }
    
}
