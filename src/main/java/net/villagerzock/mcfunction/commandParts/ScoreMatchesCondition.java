package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICondition;

public class ScoreMatchesCondition implements ICondition {
    private final String left;
    private final String right;

    public ScoreMatchesCondition(String left, String right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String applyCondition() {
        return "score #%s mcs_tmp matches %s".formatted(left, right);
    }
}
