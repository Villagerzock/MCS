package net.villagerzock.mcfunction;

public class LightMCFunction extends MCFunction {
    private final String refName;

    public LightMCFunction(String refName) {
        super("", "", "");
        this.refName = refName;
    }

    private String ref() {
        return refName;
    }

    @Override public String getOriginalName() { return ref(); }
    @Override public String getName() { return ref(); }
    @Override public String getFullPath() { return ref(); }
    @Override public String getNamespace() { return ref(); }
    @Override public String getOriginalFullPath() { return ref(); }
    @Override public String getPath() { return ref(); }
}