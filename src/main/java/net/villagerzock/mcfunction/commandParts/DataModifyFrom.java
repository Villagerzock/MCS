package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public record DataModifyFrom(String targetPath, String sourcePath) implements ICommandPart {
    @Override
    public String apply() {
        return "data modify " + targetPath + " set from " + sourcePath;
    }
}
