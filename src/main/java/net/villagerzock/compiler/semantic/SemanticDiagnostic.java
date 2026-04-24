package net.villagerzock.compiler.semantic;

import net.villagerzock.compiler.ast.SourceRange;

public record SemanticDiagnostic(SourceRange range, String message) {
	public SemanticDiagnostic(String message) {
		this(SourceRange.UNKNOWN, message);
	}

	@Override
	public String toString() {
		if (range == null || range.equals(SourceRange.UNKNOWN)) {
			return message;
		}
		return range.startLine() + ":" + range.startColumn() + " " + message;
	}
}
