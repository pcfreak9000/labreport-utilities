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

import de.pcfreak9000.main.DataTablet;
import de.pcfreak9000.main.FunctionTablet;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "print", description = "A simple command to print tablets.")
public class PrintTabletCommand implements Runnable {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Parameters(index = "0", paramLabel = "<TABLET>", description = "The tablet to print.")
    private String tabletName;
    
    @Override
    public void run() {
        if (!Main.data.exists(tabletName)) {
            System.out.println("Tablet '" + tabletName + "' does not exist.");
            return;
        }
        Tablet tablet = Main.data.getTablet(tabletName);
        if (tablet instanceof DataTablet) {
            DataTablet dt = (DataTablet) tablet;
            System.out.println("Data '" + tabletName + "':");
            System.out.println(dt.stringRepresentation());
        } else if (tablet instanceof FunctionTablet) {
            FunctionTablet ft = (FunctionTablet) tablet;
            System.out.println("Function '" + tabletName + "': " + ft.getFunction());
        } else {
            System.out.println(tablet.toString());
        }
    }
    
}
