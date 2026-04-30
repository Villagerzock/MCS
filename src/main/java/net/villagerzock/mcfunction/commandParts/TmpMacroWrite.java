package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public class TmpMacroWrite implements ICommandPart {
    private final String write;

    public TmpMacroWrite(String write) {
        this.write = write;
    }

    @Override
    public String apply() {
        return "data modify storage mcs:memory tmp_macro set value %1$s".formatted(write);
    }
}