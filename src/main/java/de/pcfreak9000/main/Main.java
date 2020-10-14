package de.pcfreak9000.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.matheclipse.core.eval.ExprEvaluator;

import de.pcfreak9000.command.Argument;
import de.pcfreak9000.command.Command;
import de.pcfreak9000.command.Parser;
import de.pcfreak9000.main.ErrorPropagation.PropagationType;

public class Main {
    
    public static final String SUPPORTED_NUMBER_FORMAT_REGEX = "\\d+(?:[,.]\\d+)|\\d+";
    private static ExprEvaluator EXPRESSION_EVALUATOR;
    
    public static ExprEvaluator evaluator() {
        if (EXPRESSION_EVALUATOR == null) {
            System.out.println("Initializing...");
            EXPRESSION_EVALUATOR = new ExprEvaluator();
            //EXPRESSION_EVALUATOR.getEvalEngine().setNumericPrecision(20000);
            //EXPRESSION_EVALUATOR.getEvalEngine().setSignificantFigures(100);
        }
        return EXPRESSION_EVALUATOR;
    }
    
    private static Tablets data = new Tablets();
    
    public static void main(String[] args) {
        evaluator();
        boolean consoleInput = true;
        InputStream in = System.in;
        if (args.length > 0) {
            try {
                in = new FileInputStream(args[0]);
                consoleInput = false;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (consoleInput) {
            System.out.print(">> ");
        } else {
            System.out.println("Reading instructions from the file \"" + args[0] + "\"");
        }
        Parser parser = new Parser();
        Command exit = new Command("exit") {
            @Override
            protected void execute(List<Argument> args) {
                System.exit(0);
            }
            
            @Override
            protected void help() {
                System.out.println("Exits the program.");
            }
        };
        parser.getBaseCommand().register(exit);
        registerCommands(parser);
        try (Scanner scan = new Scanner(in)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine().trim();
                parser.parseAndResolve(line);
                if (consoleInput) {
                    System.out.print(">> ");
                }
            }
        }
    }
    
    //TODO this sucks. make it better.
    
    private static void registerCommands(Parser p) {
        Command tablets = new Command("tablets");
        Command t_create = new Command("create");
        tablets.register(t_create);
        Command t_c_data = new Command("data") {
            @Override
            protected void execute(List<Argument> args) {
                if (args.size() != 1) {
                    System.out.println("Cannot execute: Malformed arguments");
                } else if (data.exists(args.get(0).getArgument())) {
                    System.out.println("Cannot create: Name already taken");
                } else {
                    data.createDataTablet(args.get(0).getArgument());
                    System.out.println("Created the data tablet '" + args.get(0).getArgument() + "'.");
                }
            }
        };
        t_create.register(t_c_data);
        Command t_c_func = new Command("func") {
            @Override
            protected void execute(List<Argument> args) {
                if (args.size() != 1) {
                    System.out.println("Cannot execute: Malformed arguments");
                } else if (data.exists(args.get(0).getArgument())) {
                    System.out.println("Cannot create: Name already taken");
                } else {
                    data.createFormulaTablet(args.get(0).getArgument());
                    System.out.println("Created the function tablet '" + args.get(0).getArgument() + "'.");
                }
            }
        };
        t_create.register(t_c_func);
        Command t_delete = new Command("delete") {
            protected void execute(List<Argument> args) {
                if (args.size() != 1) {
                    System.out.println("Cannot execute: Malformed arguments");
                } else if (!data.exists(args.get(0).getArgument())) {
                    System.out.println("Cannot delete: No such tablet");
                } else {
                    data.deleteTablet(args.get(0).getArgument());
                    System.out.println("Deleted the tablet '" + args.get(0).getArgument() + "'.");
                }
            };
        };
        tablets.register(t_delete);
        Command t_setentry = new Command("setentry") {
            @Override
            protected void execute(List<Argument> args) {
                if (args.size() == 0) {
                    System.out.println("Cannot execute: Malformed arguments");
                } else if (!data.exists(args.get(0).getArgument())) {
                    System.out.println("Cannot set entry: No such tablet");
                } else {
                    Tablet ta = data.getTablet(args.get(0).getArgument());
                    if (ta instanceof DataTablet) {
                        if (args.size() < 2 || args.size() > 3) {
                            System.out.println("Cannot execute: Malformed arguments");
                            return;
                        }
                        DataTablet tab = (DataTablet) ta;
                        String v = args.get(1).getArgument();
                        String err = args.size() == 3 ? args.get(2).getArgument() : "0";
                        if (!v.matches(SUPPORTED_NUMBER_FORMAT_REGEX) || !err.matches(SUPPORTED_NUMBER_FORMAT_REGEX)) {
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
        };
        tablets.register(t_setentry);
        Command propagate = new Command("prop") {
            @Override
            protected void execute(List<Argument> args) {
                if (args.size() < 3) {
                    System.out.println("Malformed arguments");
                } else {
                    String a0 = args.get(0).getArgument();//function
                    String a1 = args.get(1).getArgument();//target
                    if (!data.exists(a0) || !data.exists(a1)) {
                        System.out.println("Wroooong. Tchina!");
                    } else {
                        Tablet ta = data.getTablet(a0);
                        if (ta instanceof FunctionTablet) {
                            FunctionTablet funct = (FunctionTablet) ta;
                            String[] fargs = funct.getArgs();
                            if (args.size() - 2 != fargs.length) {
                                System.out.println("Number of function arguments and tablets not matching");
                            } else {
                                ErrorPropagation eprop = funct.createErrorPropagation();
                                for (int i = 0; i < fargs.length; i++) {
                                    DataTablet dt = (DataTablet) data.getTablet(args.get(i + 2).getArgument());//Just crashes if wrong arg lol
                                    evaluator().defineVariable(fargs[i], evaluator().eval(dt.getValue()));
                                    evaluator().defineVariable("D" + fargs[i], evaluator().eval(dt.getError()));
                                }
                                System.out.println("V: " + evaluator().eval("N(" + funct.getFunction() + ")"));
                                System.out.println("E: " + evaluator()
                                        .eval("N(" + eprop.getErrorPropFunction(PropagationType.Gaussian) + ")"));
                            }
                        } else {
                            System.out.println("Oi cunt");
                        }
                    }
                }
            }
        };
        
        p.getBaseCommand().register(propagate);
        p.getBaseCommand().register(tablets);
    }
    
    //        Parser p = new Parser();
    //        p.parse("test1 \"test2 \\\"gurke  \" test3  \"test4\"   \" \"");
    //        String tmp = null;
    //        String[] vars = { "x", "t" };
    //        System.out.println(tmp = MathStuff.linearErrorPropagation("F = (x^2-t^2)/(x^2+t^2)", vars));
    //        tmp = MathStuff.EXPRESSION_EVALUATOR.eval(tmp).toString();
    //        TeXUtilities tex = new TeXUtilities(MathStuff.EXPRESSION_EVALUATOR.getEvalEngine(), true);
    //        StringWriter wr = new StringWriter();
    //        tex.toTeX(tmp, wr);
    //        String texString = wr.toString();
    //        for (int i = 0; i < vars.length; i++) {
    //            texString = texString.replace("d" + vars[i], "\\Delta " + vars[i]);
    //        }
    //        System.out.println(texString.replace("\\cdot", ""));
}
