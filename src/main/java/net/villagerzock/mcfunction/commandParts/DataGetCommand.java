package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public record DataGetCommand(String path) implements ICommandPart {
    @Override
    public String apply() {
        return "data get " + path;
    }
}
