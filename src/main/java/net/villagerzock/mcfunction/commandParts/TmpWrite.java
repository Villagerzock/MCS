package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public class TmpWrite implements ICommandPart {
    private final String write;

    public TmpWrite(String write) {
        this.write = write;
    }

    @Override
    public String apply(String namespace) {
        return "data modify %1$s:memory tmp set value %2$s".formatted(namespace,write);
    }
}
