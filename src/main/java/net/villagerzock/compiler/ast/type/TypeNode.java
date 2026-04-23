package mylang.ast.type;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;

public final class TypeNode extends AstNode {
	private final String name;

	public TypeNode(String name) {
		this(name, SourceRange.UNKNOWN);
	}

	public TypeNode(String name, SourceRange sourceRange) {
		super(sourceRange);
		this.name = name;
	}

	public String name() {
		return name;
	}
}
