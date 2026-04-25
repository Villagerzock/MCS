package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;
import net.villagerzock.mcfunction.MCFunction;

public class FunctionCall implements ICommandPart {
    private final MCFunction function;
    private final boolean ignoreMacros;

    public FunctionCall(MCFunction function) {
        this.function = function;
        this.ignoreMacros = false;
    }
    public FunctionCall(MCFunction function, boolean ignoreMacros) {
        this.function = function;
        this.ignoreMacros = ignoreMacros;
    }

    @Override
    public String apply() {
        return "function %2$s %3$s".formatted("mcs",function.getFullPath(),ignoreMacros ? "" : "with storage mcs:memory stack[0].macro");
    }
}
