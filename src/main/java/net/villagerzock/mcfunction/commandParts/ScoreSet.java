package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public record ScoreSet(String name, int value) implements ICommandPart {
    @Override
    public String apply() {
        return "scoreboard players set #" + name + " mcs_tmp " + value;
    }
}
