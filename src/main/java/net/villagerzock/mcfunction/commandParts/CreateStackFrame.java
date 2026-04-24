package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public class CreateStackFrame implements ICommandPart {

    @Override
    public String apply(String namespace) {
        return "data modify %1$s:memory stack insert 0 from storage %1$s:memory tmp".formatted(namespace);
    }
}
