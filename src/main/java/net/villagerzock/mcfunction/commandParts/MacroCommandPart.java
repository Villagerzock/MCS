package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public class MacroCommandPart implements ICommandPart {
    private final ICommandPart part;

    public MacroCommandPart(ICommandPart part) {
        this.part = part;
    }

    @Override
    public String apply() {
        String result = part.apply();
        return "%s%s".formatted(result.startsWith("$") ? "" : "$",result);
    }
}
