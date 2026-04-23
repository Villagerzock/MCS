package mylang.ast.decl;

import mylang.ast.AstNode;
import mylang.ast.SourceRange;

import java.util.List;

public final class QualifiedPathNode extends AstNode {
	private final String namespace;
	private final List<String> segments;

	public QualifiedPathNode(String namespace, List<String> segments) {
		this(namespace, segments, SourceRange.UNKNOWN);
	}

	public QualifiedPathNode(String namespace, List<String> segments, SourceRange sourceRange) {
		super(sourceRange);
		this.namespace = namespace;
		this.segments = List.copyOf(segments);
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
}
