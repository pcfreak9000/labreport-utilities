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

import de.pcfreak9000.main.Main;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "create", aliases = "c", description = "Creates a tablet of the specified type.")
public class CreateCommand implements Runnable {
    
    private static enum TabletType {
        Data, Func
    }
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Parameters(index = "0", paramLabel = "<TABLET TYPE>", description = "The type of the tablet to create. Valid values: ${COMPLETION-CANDIDATES}")
    private TabletType tabletType;
    
    @Parameters(paramLabel = "<TABLET NAME>", description = "A name for the new tablet", index = "1")
    private String name;
    
    @Override
    public void run() {
        if (Main.data.exists(name)) {
            System.out.println("Cannot create tablet '" + name + "': Name already taken");
            return;
        }
        switch (tabletType) {
        case Data:
            Main.data.createDataTablet(name);
            System.out.println("Created the data tablet '" + name + "'.");
            break;
        case Func:
            Main.data.createFunctionTablet(name);
            System.out.println("Created the function tablet '" + name + "'.");
            break;
        default:
            throw new IllegalStateException("" + tabletType);
        }
    }
}
