package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public class TmpScoreWrite implements ICommandPart {
    private final String tmpName;
    private final String tmpVal;

    public TmpScoreWrite(String tmpName, String tmpVal) {
        this.tmpName = tmpName;
        this.tmpVal = tmpVal;
    }

    @Override
    public String apply() {
        return "scoreboard players set #%1$s mcs_tmp %2$s".formatted(tmpName,tmpVal);
    }
}
