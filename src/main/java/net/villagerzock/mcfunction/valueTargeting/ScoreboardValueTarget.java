package net.villagerzock.mcfunction.valueTargeting;


import net.villagerzock.compiler.gen.Generator;
import net.villagerzock.mcfunction.ICommandPart;
import net.villagerzock.mcfunction.commandParts.ExecuteStoreResultScoreToData;

public class ScoreboardValueTarget extends AbstractValueTarget {

    private final String scoreName;

    public ScoreboardValueTarget(String scoreName) {
        this.scoreName = scoreName;
    }

    public String scoreName() {
        return scoreName;
    }

    @Override
    protected ICommandPart iStoreFrom(AbstractValueTarget source) {
        // nichts direkt bekannt → fallback zu source.storeTo(this)
        return null;
    }

    @Override
    protected ICommandPart iStoreTo(AbstractValueTarget target) {
        if (target instanceof ScoreboardValueTarget scoreTarget) {
            return new Generator.ScoreCopy(scoreTarget.scoreName, this.scoreName);
        }

        if (target instanceof DataValueTarget dataTarget) {
            return new ExecuteStoreResultScoreToData(
                    dataTarget.path(),
                    this.scoreName
            );
        }

        return null;
    }
}
