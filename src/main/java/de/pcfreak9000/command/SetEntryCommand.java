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
import java.util.concurrent.Callable;

import de.pcfreak9000.main.DataTablet;
import de.pcfreak9000.main.DataTablet.DataUsage;
import de.pcfreak9000.main.FunctionTablet;
import de.pcfreak9000.main.FunctionTablet.PropagationType;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

//TODO properly read csv files, and also write them
@Deprecated
@Command(name = "sete", description = "Sets the content of a tablet (function or data) directly from the input (i.e. manually).")
public class SetEntryCommand implements Callable<Integer> {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Parameters(index = "0", paramLabel = "<TABLET NAME>", description = "The name of the tablet to set.")
    private String tabletName;
    
    @Parameters(index = "1", paramLabel = "<VALUE>", description = "The value to set. For a function tablet this is a function, for data tablets thats a value or a mathematical expression that evaluates to a value.")
    private String value;
    
    @Parameters(index = "2..*", paramLabel = "<PARAM>", description = "For a function tablet, the functions arguments are required. For a data tablet, an error in the form of a value or a mathematical expression that evaluates to a value can be stated, but is optional.")
    private String[] params;
    
    @Override
    public Integer call() {
        if (!Main.data.exists(tabletName)) {
            System.err.println("Cannot set entry: No such tablet: '" + tabletName + "'");
            return Main.CODE_ERROR;
        }
        Tablet ta = Main.data.getTablet(tabletName);
        if (ta instanceof DataTablet) {
            DataTablet tab = (DataTablet) ta;
            return setDataTablet(tab);
        } else if (ta instanceof FunctionTablet) {
            FunctionTablet form = (FunctionTablet) ta;
            return setFunctionTablet(form);
        }
        return Main.CODE_ERROR;
    }
    
    private int setDataTablet(DataTablet tab) {
        String err = params.length == 0 ? "0" : params[0];
        tab.setValues(value);
        tab.setErrors(err);
        tab.setDataUsage(DataUsage.Raw);
        tab.setPreferredPropagation(PropagationType.Linear);
        System.out.println(
                "Set the value of tablet '" + tabletName + "' to '" + value + "' and the error to '" + err + "'.");
        return Main.CODE_NORMAL;
    }
    
    private int setFunctionTablet(FunctionTablet form) {
        form.setFunction(value);
        form.setArgs(params);
        System.out.println("Set the function of tablet '" + tabletName + "' to '" + value
                + "' and the arguments of the function are " + Arrays.toString(params));
        return Main.CODE_NORMAL;
    }
}
