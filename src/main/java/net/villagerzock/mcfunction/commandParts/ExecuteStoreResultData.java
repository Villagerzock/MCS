package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public record ExecuteStoreResultData(String path, String scoreName) implements ICommandPart {
    @Override
    public String apply() {
        return "execute store result " + path + " int 1 run scoreboard players get #"
                + scoreName + " mcs_tmp";
    }
}
