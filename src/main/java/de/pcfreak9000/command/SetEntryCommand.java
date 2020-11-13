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

import de.pcfreak9000.main.DataTablet;
import de.pcfreak9000.main.FunctionTablet;
import de.pcfreak9000.main.FunctionTablet.PropagationType;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import de.pcfreak9000.main.DataTablet.DataUsage;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "sete")
public class SetEntryCommand implements Runnable {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Parameters(index = "0")
    private String tabletName;
    
    @Parameters(index = "1")
    private String value;
    
    @Parameters(index = "2..*")
    private String[] params;
    
    @Override
    public void run() {
        if (!Main.data.exists(tabletName)) {
            System.out.println("Cannot set entry: No such tablet");
            return;
        }
        Tablet ta = Main.data.getTablet(tabletName);
        if (ta instanceof DataTablet) {
            DataTablet tab = (DataTablet) ta;
            setDataTablet(tab);
        } else if (ta instanceof FunctionTablet) {
            FunctionTablet form = (FunctionTablet) ta;
            setFunctionTablet(form);
        }
    }
    
    private void setDataTablet(DataTablet tab) {
        String err = params.length == 0 ? "0" : params[0];
        tab.setValues(value);
        tab.setErrors(err);
        tab.setDataUsage(DataUsage.Raw);
        tab.setPreferredPropagation(PropagationType.Linear);
        System.out.println(
                "Set the value of tablet '" + tabletName + "' to '" + value + "' and the error to '" + err + "'.");
    }
    
    private void setFunctionTablet(FunctionTablet form) {
        form.setFunction(value);
        form.setArgs(params);
        System.out.println("Set the function of tablet '" + tabletName + "' to '" + value
                + "' and the arguments of the function are " + Arrays.toString(params));
    }
}
