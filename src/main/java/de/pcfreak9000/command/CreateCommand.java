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

@Command(name = "create", aliases = "c", description = "Creates a tablet of the specified type")
public class CreateCommand {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Command(name = "function", aliases = { "func", "f" }, description = "Creates a new function tablet")
    void createFunctionCommand(
            @Parameters(paramLabel = "NAME", description = "A name for the new function tablet", index = "0") String name,
            @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC) boolean help) {
        if (Main.data.exists(name)) {
            System.out.println("Cannot create: Name already taken");
            return;
        }
        Main.data.createFunctionTablet(name);
        System.out.println("Created the function tablet '" + name + "'.");
    }
    
    @Command(name = "data", aliases = { "d" }, description = "Creates a new data tablet")
    void createDataCommand(
            @Parameters(paramLabel = "NAME", description = "A name for the new data tablet", index = "0") String name,
            @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC) boolean help) {
        if (Main.data.exists(name)) {
            System.out.println("Cannot create: Name already taken");
            return;
        }
        Main.data.createDataTablet(name);
        System.out.println("Created the function tablet '" + name + "'.");
    }
}
