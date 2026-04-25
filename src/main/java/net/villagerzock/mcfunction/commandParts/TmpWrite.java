package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public class TmpWrite implements ICommandPart {
    private final String write;

    public TmpWrite(String write) {
        this.write = write;
    }

    @Override
    public String apply() {
        return "data modify storage mcs:memory tmp set value %1$s".formatted(write);
    }
}