package de.pcfreak9000.main;

import java.util.List;

import de.pcfreak9000.command.Argument;
import de.pcfreak9000.command.ICommand;

public class SetCommandImpl implements ICommand {
    
    @Override
    public boolean checkArguments(List<Argument> args) {
        if (args.size() == 0) {
            System.out.println("Cannot execute: Malformed arguments");
            return false;
        } else if (!Main.data.exists(args.get(0).getArgument())) {
            System.out.println("Cannot set entry: No such tablet");
            return false;
        }
        return true;
    }
    
    @Override
    public void execute(List<Argument> args) {
        Tablet ta = Main.data.getTablet(args.get(0).getArgument());
        if (ta instanceof DataTablet) {
            if (args.size() < 2 || args.size() > 3) {
                System.out.println("Cannot execute: Malformed arguments");
                return;
            }
            DataTablet tab = (DataTablet) ta;
            String v = args.get(1).getArgument();
            String err = args.size() == 3 ? args.get(2).getArgument() : "0";
            if (!v.matches(Main.SUPPORTED_NUMBER_FORMAT_REGEX) || !err.matches(Main.SUPPORTED_NUMBER_FORMAT_REGEX)) {
                System.out.println("Cannot set entry: Malformed number format");
            } else {
                tab.setValues(v.replace(',', '.'));
                tab.setErrors(err.replace(',', '.'));
                System.out.println("Set the value of tablet '" + args.get(0).getArgument() + "' to '" + v
                        + "' and the error to '" + err + "'.");
            }
        } else if (ta instanceof FunctionTablet) {
            FunctionTablet form = (FunctionTablet) ta;
            if (args.size() < 3) {
                System.out.println("Cannot execute: Malformed arguments");
                return;
            } else {
                form.setFunction(args.get(1).getArgument());
                if (args.size() > 2) {
                    String[] array = args.subList(2, args.size()).stream().map(Argument::getArgument)
                            .toArray(String[]::new);
                    form.setArgs(array);
                }
            }
        }
    }
}
