package de.pcfreak9000.main;

import java.io.StringWriter;
import java.util.Scanner;

import org.matheclipse.core.eval.TeXUtilities;

public class Main {
    
    public static void main(String[] args) {
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
        System.out.print(">> ");
        Parser parser = new Parser();
        
        Command exit = new Command() {
            @Override
            protected String execute(String... strings) {
                System.exit(0);
                return "Exiting...";
            }
        };
        Command linear = new Command() {
            @Override
            protected String execute(String... strings) {
                StringBuilder result = new StringBuilder();
                result.append(strings[0]).append('\n');
                String[] args = new String[strings.length - 1];
                System.arraycopy(strings, 1, args, 0, args.length);
                result.append(MathStuff.linearErrorPropagation(strings[0], args));
                return result.toString();
            }
        };
        parser.getBaseCommand().register("exit", exit);
        parser.getBaseCommand().register("linear", linear);
        
        try (Scanner scan = new Scanner(System.in)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine().trim();
                System.out.println(parser.parse(line));
                System.out.print(">> ");
            }
        }
    }
}
