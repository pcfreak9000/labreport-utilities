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
import de.pcfreak9000.main.Tablet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "print")
public class PrintTabletCommand implements Runnable {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Parameters(index = "0", paramLabel = "<TABLET>")
    private String tabletName;
    
    @Override
    public void run() {
        if (!Main.data.exists(tabletName)) {
            System.out.println("Tablet '" + tabletName + "' does not exist.");
            return;
        }
        Tablet tablet = Main.data.getTablet(tabletName);
        System.out.println(tablet.toString());//Lol this is useless right now
    }
    
}
