package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public record GetFieldPart(String name) implements ICommandPart {
    @Override
    public String apply() {
        return "$data modify storage mcs:memory tmp set from storage mcs:memory heap[$(index)].%s".formatted(name);
    }
}
