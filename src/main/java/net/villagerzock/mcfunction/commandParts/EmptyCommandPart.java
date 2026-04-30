package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public class EmptyCommandPart implements ICommandPart {
    public static final EmptyCommandPart INSTANCE = new EmptyCommandPart();

    @Override
    public String apply() {
        return "";
    }
}
