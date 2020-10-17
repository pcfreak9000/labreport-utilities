package de.pcfreak9000.main;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.matheclipse.core.eval.TeXUtilities;

import de.pcfreak9000.command.Argument;
import de.pcfreak9000.command.Arguments;
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
                        Map<String, String> map = Arguments.mapArguments(args.subList(2, args.size()), fargs);//[fVariable]=[TabletName]
                        if (map == null) {
                            System.out.println("Malformed function arguments");
                            return;
                        }
                        ErrorPropagation eprop = funct.createErrorPropagation();
                        boolean statistical = false;
                        for (int i = 0; i < fargs.length; i++) {
                            DataTablet dt = (DataTablet) Main.data.getTablet(map.get(fargs[i]));
                            Main.evaluator().defineVariable(fargs[i], Main.evaluator().eval(dt.getValue()));
                            Main.evaluator().defineVariable("D" + fargs[i], Main.evaluator().eval(dt.getError(0)));
                            statistical |= dt.isStatistical();
                        }
                        System.out.println("V: " + Main.evaluator().eval("N(" + funct.getFunction() + ")"));
                        System.out.println("E: " + Main.evaluator()
                                .eval("N(" + eprop.getErrorPropFunction(PropagationType.get(statistical)) + ")"));
                        Main.evaluator().clearVariables();
                        String tmp = eprop.getErrorPropFunction(PropagationType.get(statistical));
                        System.out.println(tmp);
//                        TeXUtilities tex = new TeXUtilities(Main.evaluator().getEvalEngine(), true);
//                        StringWriter wr = new StringWriter();
//                        tex.toTeX(tmp, wr);
//                        String texString = wr.toString();
//                        for (int i = 0; i < fargs.length; i++) {
//                            texString = texString.replace("d" + fargs[i], "\\Delta " + fargs[i]);
//                        }
//                        System.out.println(texString.replace("\\cdot", "").replace("{t}", "{\\omega}").replace("{x}", "{\\Omega}"));
                    }
                } else {
                    System.out.println("Oi cunt");
                }
            }
        }
    }
    
}
