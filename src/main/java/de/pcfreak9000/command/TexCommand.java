package de.pcfreak9000.command;

import java.io.StringWriter;

import org.matheclipse.core.eval.TeXUtilities;

import de.pcfreak9000.main.FunctionTablet;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import de.pcfreak9000.main.FunctionTablet.PropagationType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "tex")
public class TexCommand implements Runnable {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Parameters(index = "0")
    private String functionTablet;
    
    @Parameters(index = "1")
    private PropagationType propType;
    
    @Parameters(index = "2..*")
    private String[] variables;
    
    @Override
    public void run() {
        if (!Main.data.exists(functionTablet)) {
            System.out.println("Function tablet '" + functionTablet + "' does not exist.");
            return;
        }
        Tablet ta = Main.data.getTablet(functionTablet);
        if (!(ta instanceof FunctionTablet)) {
            System.out.println("Tablet '" + functionTablet + "' is not a function tablet.");
            return;
        }
        FunctionTablet function = (FunctionTablet) ta;
        String prop = function.getErrorPropFunction(propType);
        String texString = Main.evaluator().eval("TeXForm[" + prop + "]").toString();
        for (int i = 0; i < variables.length; i++) {
            texString = texString.replace("d" + variables[i].toLowerCase(), "\\Delta " + variables[i]);
        }
        System.out.println(texString);
    }
    
}
