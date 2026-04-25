package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

import java.util.ArrayList;
import java.util.List;

public class BooleanExpressionPart implements ICommandPart {
    private final List<ICommandPart> setup = new ArrayList<>();
    private final ICommandPart resultCommand;

    public BooleanExpressionPart(ICommandPart resultCommand) {
        this.resultCommand = resultCommand;
    }

    public BooleanExpressionPart addSetup(ICommandPart part) {
        setup.add(part);
        return this;
    }

    @Override
    public String apply() {
        StringBuilder builder = new StringBuilder();

        for (ICommandPart part : setup) {
            builder.append(part.apply()).append('\n');
        }

        builder.append(resultCommand.apply());
        return builder.toString();
    }
}