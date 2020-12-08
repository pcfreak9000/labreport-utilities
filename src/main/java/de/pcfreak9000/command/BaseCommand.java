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

@Command(name = "", subcommands = { CreateCommand.class, SetFromFileCommand.class, SetEntryCommand.class,
        PropagateCommand.class, TexCommand.class, PrintTabletCommand.class, SetCommand.class })
public class BaseCommand {
    
    public static final String HELP_DESC = "display this help message";
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = HELP_DESC)
    private boolean help;
    
    @Command(name = "exit", description = "Exits the program")
    int exitCommand() {
        return Main.CODE_EXIT;//Tell the parser to leave the current Scanner loop
    }
    
    @Command(name = "delete", description = "Deletes a tablet")
    int deleteTablet(
            @Parameters(paramLabel = "<NAME>", description = "The name of the tablet that is to be deleted", index = "0") String name,
            @Option(names = { "-h", "--help" }, usageHelp = true, description = HELP_DESC) boolean help) {
        if (!Main.data.exists(name)) {
            System.err.println("Cannot delete: No such tablet");
            return Main.CODE_ERROR;
        }
        Main.data.deleteTablet(name);
        System.out.println("Deleted the tablet '" + name + "'.");
        return Main.CODE_NORMAL;
    }
    
    @Command(name = "exec", description = "Reads instructions from a file")
    int readFile(
            @Parameters(paramLabel = "<FILE_NAME>", description = "Executes the instructions from a file", index = "0") String fileName,
            @Option(names = { "-h", "--help" }, usageHelp = true, description = HELP_DESC) boolean help) {
        return Main.parseFile(fileName);
    }
    
}
