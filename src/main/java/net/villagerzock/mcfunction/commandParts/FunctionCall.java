package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;
import net.villagerzock.mcfunction.MCFunction;

public class FunctionCall implements ICommandPart {
    private final MCFunction function;

    public FunctionCall(MCFunction function) {
        this.function = function;
    }

    @Override
    public String apply(String namespace) {
        return "function %2$s with storage %1$s:memory stack[0].macro".formatted(namespace,function.getName());
    }
}
