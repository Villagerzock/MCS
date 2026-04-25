package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public class PopStackFrame implements ICommandPart {
    @Override
    public String apply() {
        return "data remove storage %1$s:memory stack[0]".formatted("mcs");
    }
}
