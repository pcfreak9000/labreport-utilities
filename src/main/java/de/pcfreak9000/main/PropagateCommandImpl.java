package de.pcfreak9000.main;

import java.util.List;

import de.pcfreak9000.command.Argument;
import de.pcfreak9000.command.ICommand;
import de.pcfreak9000.main.ErrorPropagation.PropagationType;

public class PropagateCommandImpl implements ICommand {
    @Override
    public void execute(List<Argument> args) {
        if (args.size() < 3) {
            System.out.println("Malformed arguments");
        } else {
            String a0 = args.get(0).getArgument();//function
            String a1 = args.get(1).getArgument();//target
            if (!Main.data.exists(a0) || !Main.data.exists(a1)) {
                System.out.println("Wroooong. Tchina!");
            } else {
                Tablet ta = Main.data.getTablet(a0);
                if (ta instanceof FunctionTablet) {
                    FunctionTablet funct = (FunctionTablet) ta;
                    String[] fargs = funct.getArgs();
                    if (args.size() - 2 != fargs.length) {
                        System.out.println("Number of function arguments and tablets not matching");
                    } else {
                        ErrorPropagation eprop = funct.createErrorPropagation();
                        boolean useGaus = false;
                        for (int i = 0; i < fargs.length; i++) {
                            DataTablet dt = (DataTablet) Main.data.getTablet(args.get(i + 2).getArgument());//Just crashes if wrong arg lol
                            Main.evaluator().defineVariable(fargs[i], Main.evaluator().eval(dt.getValue()));
                            Main.evaluator().defineVariable("D" + fargs[i], Main.evaluator().eval(dt.getError(0)));
                            useGaus |= dt.isStatistical();
                        }
                        System.out.println("V: " + Main.evaluator().eval("N(" + funct.getFunction() + ")"));
                        System.out
                                .println(
                                        "E: " + Main.evaluator()
                                                .eval("N(" + eprop.getErrorPropFunction(
                                                        useGaus ? PropagationType.Gaussian : PropagationType.Linear)
                                                        + ")"));
                    }
                } else {
                    System.out.println("Oi cunt");
                }
            }
        }
    }
}
