package net.villagerzock.mcfunction.commandParts;

import net.villagerzock.mcfunction.ICommandPart;

public class TmpScoreQuery implements ICommandPart {
    private final String tmpName;
    private final ICommandPart tmpQuery;

    public TmpScoreQuery(String tmpName, ICommandPart tmpQuery) {
        this.tmpName = tmpName;
        this.tmpQuery = tmpQuery;
    }

    @Override
    public String apply() {
        return "execute store result score #%1$s mcs_tmp run %2$s".formatted(tmpName,tmpQuery.apply());
    }
}
