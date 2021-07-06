package de.pcfreak9000.command;

import java.util.Arrays;
import java.util.concurrent.Callable;

import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.interfaces.IExpr;

import de.pcfreak9000.main.DataTablet;
import de.pcfreak9000.main.DataTablet.DataUsage;
import de.pcfreak9000.main.FunctionTablet;
import de.pcfreak9000.main.FunctionTablet.PropagationType;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "set", description = "Sets the content of a tablet and creates the tablet if neccessary.")
public class SetCommand implements Callable<Integer> {
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Option(names = { "-p",
            "--prop" }, paramLabel = "<propagation type>", description = "Use this option to set the preferred type of error propagation for this data. Valid values: ${COMPLETION-CANDIDATES}. The default is linear.")
    private PropagationType preferredPropagation = PropagationType.Linear;
    
    @Option(names = { "-u",
            "--usage" }, paramLabel = "<data usage>", description = "Use this option to set how this data is to be intepreted. Valid values: ${COMPLETION-CANDIDATES}. The default is raw.")
    private DataUsage dataUsage = DataUsage.Raw;
    
    @Parameters(paramLabel = "<TABLET_NAME>", description = "The tablet that is to be filled. Non-existing tablets will be created automatically.", index = "0")
    private String tabletName;
    
    @Parameters(index = "1..*", paramLabel = "<PARAM>", description = { "Function mode: The function.",
            "Data mode: One or more values." }, arity = "1..*")
    private String[] params;
    
    @Option(names = { "-a", "--function" })
    private boolean isFunction;
    
    @Option(names = { "-e", "--error" }, split = ";", defaultValue = "0", description = {
            "Error(s) of the specified values. Only in value mode.", "The default error is 0." })
    private String[] errors;
    
    @Override
    public Integer call() throws Exception {
        if (isFunction) {
            if (!Main.data.exists(tabletName)) {
                System.out.println("Created the function tablet '" + tabletName + "'.");
                Main.data.createFunctionTablet(tabletName);
            }
            Tablet t = Main.data.getTablet(tabletName);
            if (!(t instanceof FunctionTablet)) {
                System.err.println("Tablet '" + tabletName + "' is not a function tablet");
                return Main.CODE_ERROR;
            }
            FunctionTablet ft = (FunctionTablet) t;
            if (params.length > 1) {
                System.err.println("Only one parameter is accepted in function-mode");
                return Main.CODE_ERROR;
            }
            ft.setFunction(params[0]);
            System.out.println("Set the function of tablet '" + tabletName + "' to '" + ft.getFunctionOriginal()
                    + "' and the arguments of the function are " + Arrays.toString(ft.getVarArgs()));
        } else {
            if (!Main.data.exists(tabletName)) {
                System.out.println("Created the data tablet '" + tabletName + "'.");
                Main.data.createDataTablet(tabletName);
            }
            Tablet t = Main.data.getTablet(tabletName);
            if (!(t instanceof DataTablet)) {
                System.err.println("Tablet '" + tabletName + "' is not a data tablet");
                return Main.CODE_ERROR;
            }
            if (params.length != errors.length && errors.length > 1) {
                System.err.printf("Mismatching count of values (%d) and errors (%d)%n", params.length, errors.length);
                return Main.CODE_ERROR;
            }
            DataTablet dt = (DataTablet) t;
            dt.setValues(Arrays.stream(params).map((s) -> EvalEngine.get().parse(s)).toArray(IExpr[]::new));
            dt.setErrors(Arrays.stream(errors).map((s) -> EvalEngine.get().parse(s)).toArray(IExpr[]::new));
            dt.setPreferredPropagation(preferredPropagation);
            dt.setDataUsage(dataUsage);
            System.out.println("Set the value(s) of tablet '" + tabletName + "' to '" + Arrays.toString(params)
                    + "' and the error(s) to '" + Arrays.toString(errors) + "'.");
        }
        return Main.CODE_NORMAL;
    }
}
