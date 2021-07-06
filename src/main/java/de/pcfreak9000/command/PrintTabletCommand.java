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
import de.pcfreak9000.main.FunctionTablet;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "print", description = "A simple command to print tablets.")
public class PrintTabletCommand implements Callable<Integer> {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Parameters(index = "0", paramLabel = "<TABLET>", description = "The tablet to print.")
    private String tabletName;
    
    @Option(names = { "-r",
            "--raw" }, defaultValue = "false", description = "Useful to see the raw contents of a MASD data tablet.")
    private boolean raw = false;
    
    @Option(names = { "-f",
            "--format" }, defaultValue = "%v; %e", description = "Specify the format in which the value-error-pairs will be printed. For each value-error-pair, %v in the format will be replaced with the corresponding value and %e with the corresponding error. Other characters will be printed for each pair. Printing only values or only the errors is possible. The default is \\\"%v; %e\\\"")
    private String format = "%v; %e";
    
    @Override
    public Integer call() {
        if (!Main.data.exists(tabletName)) {
            System.err.println("Tablet '" + tabletName + "' does not exist.");
            return Main.CODE_ERROR;
        }
        Tablet tablet = Main.data.getTablet(tabletName);
        if (tablet instanceof DataTablet) {
            DataTablet dt = (DataTablet) tablet;
            System.out.println("Data '" + tabletName + "', Format: '" + format.toLowerCase() + "' Raw: " + raw);
            printData(dt);
        } else if (tablet instanceof FunctionTablet) {
            FunctionTablet ft = (FunctionTablet) tablet;
            System.out.println("Function '" + tabletName + "': " + ft.getFunctionInternal());
            System.out.println("with args: " + Arrays.toString(ft.getVarArgs()));
        } else {
            System.out.println(tablet.toString());
        }
        return Main.CODE_NORMAL;
    }
    
    private void printData(DataTablet dt) {
        format = format.toLowerCase();
        int len = raw ? dt.getLengthRaw() : dt.getLength();
        for (int i = 0; i < len; i++) {
            String s = format.replace("%v", raw ? dt.getValueRaw(i).toString() : dt.getValue(i).toString());
            s = s.replace("%e", raw ? dt.getErrorRaw(i).toString() : dt.getError(i).toString());
            System.out.println(s);
        }
    }
    
}
