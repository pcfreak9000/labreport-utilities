package de.pcfreak9000.command;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ICommand {
    
    //    public static ICommand of(Function<List<Argument>, Boolean> checkArgsFun, Consumer<List<Argument>> executeFunc,
    //            Runnable helpFunc) {
    //        return new ICommand() {
    //            @Override
    //            public boolean checkArguments(List<Argument> args) {
    //                return checkArgsFun.apply(args);
    //            }
    //            
    //            @Override
    //            public void execute(List<Argument> args) {
    //                executeFunc.accept(args);
    //            }
    //            
    //            @Override
    //            public void help() {
    //                helpFunc.run();
    //            }
    //        };
    //    }
    
    default boolean checkArguments(List<Argument> args) {
        return true;
    }
    
    default void execute(List<Argument> args) {
    }
    
    default void help() {
        System.out.println("No further help.");
    }
}
