package de.pcfreak9000.main;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

public class MathStuff {
    
    public static final ExprEvaluator EXPRESSION_EVALUATOR = new ExprEvaluator();
    
    public static String linearErrorPropagation(String function, String... vars) {
        String result = "";
        for (int i = 0; i < vars.length; i++) {
            IExpr jv = EXPRESSION_EVALUATOR.eval("D(" + function + ", " + vars[i] + ")");
            result += "abs(" + jv.toString() + ")" + " * D" + vars[i] + (i == vars.length - 1 ? "" : " + ");
        }
        return result;
    }
    
    public static String gaussianErrorPropagation(String function, String... vars) {
        String result = "sqrt(";
        for (int i = 0; i < vars.length; i++) {
            IExpr jv = EXPRESSION_EVALUATOR.eval("D(" + function + ", " + vars[i] + ")");
            result += "(" + jv.toString() + " * D" + vars[i] + ")^2" + (i == vars.length - 1 ? ")" : " + ");
        }
        return result;
    }
}
