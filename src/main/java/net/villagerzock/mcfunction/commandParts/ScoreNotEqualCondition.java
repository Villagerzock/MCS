package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICondition;

public class ScoreNotEqualCondition implements ICondition {
    private final String left;
    private final String right;

    public ScoreNotEqualCondition(String left, String right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String applyCondition() {
        return "score #%s mcs_tmp = #%s mcs_tmp".formatted(left, right);
    }
}