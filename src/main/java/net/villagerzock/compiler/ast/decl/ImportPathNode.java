package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;

import java.util.List;

public final class ImportPathNode extends AstNode {
    private final String namespace;
    private final List<String> segments;
    private final List<String> clazz;

    public ImportPathNode(String namespace, List<String> segments, List<String> clazz, SourceRange sourceRange) {
        super(sourceRange);
        this.namespace = namespace;
        this.segments = List.copyOf(segments);
        this.clazz = List.copyOf(clazz);
    }

    public String namespace() {
        return namespace;
    }

    public List<String> segments() {
        return segments;
    }

    public String asImportString() {
        return namespace + ":" + String.join("/", segments);
    }
    public String path(){
        return String.join("/",segments);
    }


    @Override
    public String getString() {
        return "Path(" + asImportString() + ")";
    }
}

