package net.villagerzock.mcfunction.valueTargeting;

import net.villagerzock.mcfunction.ICommandPart;
import net.villagerzock.mcfunction.commandParts.EmptyCommandPart;

public abstract class AbstractValueTarget {
    public final ICommandPart storeFrom(AbstractValueTarget target){
        if (this.equals(target)) return EmptyCommandPart.INSTANCE;
        ICommandPart commandPart = iStoreFrom(target);
        if (commandPart != null){
            return commandPart;
        }
        return target.storeTo(this);
    }
    public final ICommandPart storeTo(AbstractValueTarget target){
        ICommandPart commandPart = iStoreTo(target);
        if (commandPart != null){
            return commandPart;
        }
        throw new IllegalStateException("Value Target Type %1$s is incompatible with Value Target Type %2$s".formatted(getClass().getSimpleName(),target.getClass().getSimpleName()));
    }

    protected abstract ICommandPart iStoreFrom(AbstractValueTarget target);
    protected abstract ICommandPart iStoreTo(AbstractValueTarget target);
}
