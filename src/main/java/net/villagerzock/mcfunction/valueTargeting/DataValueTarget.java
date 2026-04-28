package net.villagerzock.mcfunction.valueTargeting;

import net.villagerzock.compiler.gen.Generator;
import net.villagerzock.mcfunction.ICommandPart;
import net.villagerzock.mcfunction.commandParts.DataGetCommand;
import net.villagerzock.mcfunction.commandParts.DataModifyFrom;
import net.villagerzock.mcfunction.commandParts.ExecuteStoreResultData;

public class DataValueTarget extends AbstractValueTarget {

    private final String path;
    // z.B. "storage mcs:memory stack[0].locals.x"

    public DataValueTarget(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

    @Override
    protected ICommandPart iStoreFrom(AbstractValueTarget source) {
        if (source instanceof ScoreboardValueTarget scoreSource) {
            return new ExecuteStoreResultData(
                    path,
                    scoreSource.scoreName()
            );
        }

        return null;
    }

    @Override
    protected ICommandPart iStoreTo(AbstractValueTarget target) {
        if (target instanceof ScoreboardValueTarget scoreTarget) {
            return new Generator.ExecuteStoreResultScore(
                    scoreTarget.scoreName(),
                    new DataGetCommand(path)
            );
        }

        if (target instanceof DataValueTarget dataTarget) {
            return new DataModifyFrom(dataTarget.path(), this.path);
        }

        return null;
    }
}
