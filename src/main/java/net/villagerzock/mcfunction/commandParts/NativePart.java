package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public class NativePart implements ICommandPart {
    private final String nativePart;

    public NativePart(String nativePart) {
        this.nativePart = nativePart;
    }
    @Override
    public String apply() {
        return nativePart;
    }
}
