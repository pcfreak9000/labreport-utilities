package de.pcfreak9000.main;

import java.util.List;

import de.pcfreak9000.command.Argument;
import de.pcfreak9000.command.ICommand;

public class SetFileCommandImpl implements ICommand {
    
    @Override
    public boolean checkArguments(List<Argument> args) {
        if (args.size() < 2) {
            System.out.println("Cannot execute: Malformed arguments");
            return false;
        } else if (!Main.data.exists(args.get(0).getArgument())) {
            System.out.println("Cannot set: No such tablet");
            return false;
        }
        return true;
    }
    
    @Override
    public void execute(List<Argument> args) {
        Tablet ta = Main.data.getTablet(args.get(0).getArgument());
        if (!(ta instanceof DataTablet)) {
            System.out.println("Cannot execute: Tablet '" + args.get(0).getArgument() + "' is not a data tablet");
            return;
        }
        DataTablet dt = (DataTablet) ta;
        
    }
}
