package de.pcfreak9000.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.matheclipse.core.eval.ExprEvaluator;

import de.pcfreak9000.command.Argument;
import de.pcfreak9000.command.Command;
import de.pcfreak9000.command.ICommand;
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
    
    public static final Tablets data = new Tablets();
    
    public static void main(String[] args) {
        evaluator();//Initializing the evaluator takes a second or two
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
        parser.createCommand("exit", new ICommand() {
            @Override
            public void execute(List<Argument> args) {
                System.exit(0);
            }
            
            @Override
            public void help() {
                System.out.println("Exits the program.");
            }
        });
        //        parser.createCommand(">", new ICommand() {
        //            @Override
        //            public void execute(List<Argument> args) {
        //                System.out.println(evaluator().eval(args.get(0).getArgument()));
        //            }
        //        });
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
    
    private static void registerCommands(Parser p) {
        Command tablets = p.createCommand("tablets");
        p.createAlias("tablets", "t");
        Command t_create = tablets.createSubCommand("create");
        tablets.createAlias("create", "c");
        t_create.createSubCommand("data", new ICommand() {
            @Override
            public void execute(List<Argument> args) {
                if (args.size() != 1) {
                    System.out.println("Cannot execute: Malformed arguments");
                } else if (data.exists(args.get(0).getArgument())) {
                    System.out.println("Cannot create: Name already taken");
                } else {
                    data.createDataTablet(args.get(0).getArgument());
                    System.out.println("Created the data tablet '" + args.get(0).getArgument() + "'.");
                }
            }
        });
        t_create.createSubCommand("func", new ICommand() {
            @Override
            public void execute(List<Argument> args) {
                if (args.size() != 1) {
                    System.out.println("Cannot execute: Malformed arguments");
                } else if (data.exists(args.get(0).getArgument())) {
                    System.out.println("Cannot create: Name already taken");
                } else {
                    data.createFormulaTablet(args.get(0).getArgument());
                    System.out.println("Created the function tablet '" + args.get(0).getArgument() + "'.");
                }
            }
        });
        tablets.createSubCommand("delete", new ICommand() {
            public void execute(List<Argument> args) {
                if (args.size() != 1) {
                    System.out.println("Cannot execute: Malformed arguments");
                } else if (!data.exists(args.get(0).getArgument())) {
                    System.out.println("Cannot delete: No such tablet");
                } else {
                    data.deleteTablet(args.get(0).getArgument());
                    System.out.println("Deleted the tablet '" + args.get(0).getArgument() + "'.");
                }
            }
        });
        tablets.createSubCommand("sete", new SetEntryCommandImpl());
        tablets.createSubCommand("setf", new SetFileCommandImpl());
        p.createCommand("prop", new PropagateCommandImpl());
        p.createCommand("printerr", new ICommand() {
            @Override
            public void execute(List<Argument> args) {
                p.checkCommandException().printStackTrace();
            }
        });
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
