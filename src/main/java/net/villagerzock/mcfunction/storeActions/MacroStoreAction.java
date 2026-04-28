package net.villagerzock.mcfunction.storeActions;

import net.villagerzock.mcfunction.IStoreAction;

public record MacroStoreAction(String macroName) implements IStoreAction {
    @Override
    public String applyStoreAction() {
        return "storage mcs:memory stack[0].macros.%1$s int 1".formatted(macroName);
    }
}
