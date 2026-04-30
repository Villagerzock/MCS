package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;
import net.villagerzock.mcfunction.MCFunction;

public class FunctionCall implements ICommandPart {
    private final MCFunction function;
    private final boolean ignoreMacros;
    private final boolean useTempMacros;

    public FunctionCall(MCFunction function) {
        this(function,0);
    }
    public FunctionCall(MCFunction function, int macroMode) {
        this.function = function;
        this.ignoreMacros = macroMode == 1;
        this.useTempMacros = macroMode == 2;
    }

    @Override
    public String apply() {
        return "function %1$s %2$s".formatted(function.getFullPath(),ignoreMacros || !function.usesMacros() ? "" : "with storage mcs:memory %s".formatted(useTempMacros ? "tmp_macro" : "stack[0].macro"));
    }
}
