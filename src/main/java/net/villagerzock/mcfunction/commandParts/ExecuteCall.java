package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;
import net.villagerzock.mcfunction.ICondition;
import net.villagerzock.mcfunction.IStoreAction;

import java.util.ArrayList;
import java.util.List;

public final class ExecuteCall implements ICommandPart {
    private final List<String> subcommands = new ArrayList<>();
    private final ICommandPart run;

    public ExecuteCall(ICommandPart run) {
        this.run = run;
    }

    public ExecuteCall addCondition(ICondition condition) {
        subcommands.add("if " + condition.applyCondition());
        return this;
    }

    public ExecuteCall addStore(IStoreAction store) {
        subcommands.add("store result " + store.applyStoreAction());
        return this;
    }

    @Override
    public String apply(String namespace) {
        if (subcommands.isEmpty()) {
            return "execute run %s".formatted(run.apply(namespace));
        }

        return "execute %s run %s".formatted(
                String.join(" ", subcommands),
                run.apply(namespace)
        );
    }
}
