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
package de.pcfreak9000.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.matheclipse.core.eval.ExprEvaluator;

import de.pcfreak9000.command.BaseCommand;
import picocli.CommandLine;

public class Main {
    
    public static final String SUPPORTED_NUMBER_FORMAT_REGEX = "\\d+(?:[,.]\\d+)|\\d+";
    private static ExprEvaluator EXPRESSION_EVALUATOR;
    
    public static ExprEvaluator ev2 = new ExprEvaluator();
    
    public static ExprEvaluator evaluator() {
        if (EXPRESSION_EVALUATOR == null) {
            System.out.println("Initializing...");
            EXPRESSION_EVALUATOR = new ExprEvaluator();
            //EXPRESSION_EVALUATOR.getEvalEngine().setRelaxedSyntax(false);
            //EXPRESSION_EVALUATOR.getEvalEngine().eval
            //EXPRESSION_EVALUATOR.getEvalEngine().setNumericPrecision(20000); //?!?!?
            //EXPRESSION_EVALUATOR.getEvalEngine().setSignificantFigures(100); //?!?!?
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
            System.out.println("Ready. Type -h for help.");
            System.out.print(">> ");
        } else {
            System.out.println("Reading instructions from the file \"" + args[0] + "\"");
        }
        CommandLine commandline = new CommandLine(new BaseCommand());
        commandline.setCaseInsensitiveEnumValuesAllowed(true);
        commandline.setErr(new PrintWriter(System.out));
        try (Scanner scan = new Scanner(in)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine().trim();
                String[] parseResult = parse(line);
                if (parseResult == null || parseResult.length == 0) {
                    if (consoleInput) {
                        System.out.print(">> ");
                    }
                    continue;
                }
                commandline.execute(parseResult);
                if (consoleInput) {
                    System.out.print(">> ");
                }
            }
        }
    }
    
    private static String[] parse(String in) {
        List<String> parts = new ArrayList<>();
        if (in.startsWith("%")) {//Comment
            return parts.toArray(String[]::new);
        }
        char[] chars = in.toCharArray();
        StringBuilder builder = new StringBuilder();
        boolean inside = false;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '"' && (i <= 1 || chars[i - 1] != '\\')) {
                inside = !inside;
                if (!inside) {
                    String s = builder.toString();
                    if (!s.isEmpty()) {
                        s = s.replace("\\\"", "\"");
                        parts.add(s);
                        builder = new StringBuilder();
                    }
                }
                continue;
            }
            if ((c == ' ' && !inside) || i == chars.length - 1) {
                if (i == chars.length - 1) {
                    builder.append(c);
                }
                String s = builder.toString();
                if (!s.isEmpty()) {
                    s = s.trim();
                    parts.add(s);
                    builder = new StringBuilder();
                }
            } else {
                builder.append(c);
            }
        }
        if (inside) {
            System.out.println("Unclosed '\"'");
            return null;
        }
        return parts.toArray(String[]::new);
    }
    
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
