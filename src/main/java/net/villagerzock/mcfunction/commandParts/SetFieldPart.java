package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public record SetFieldPart(String name) implements ICommandPart {
    @Override
    public String apply() {
        return "$data modify storage mcs:memory heap[$(index)].%s set from storage mcs:memory tmp".formatted(name);
    }
}
