package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICondition;

public class ScoreCompareCondition implements ICondition {
    private final String left;
    private final String operator;
    private final String right;

    public ScoreCompareCondition(String left, String operator, String right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String applyCondition() {
        return "score #%s mcs_tmp %s #%s mcs_tmp".formatted(left, operator, right);
    }
}
