package net.villagerzock.compiler.semantic;

import java.util.List;
import java.util.stream.Collectors;

public final class SemanticException extends RuntimeException {
	private final List<SemanticDiagnostic> diagnostics;

	public SemanticException(List<SemanticDiagnostic> diagnostics) {
		super(diagnostics.stream().map(SemanticDiagnostic::toString).collect(Collectors.joining(System.lineSeparator())));
		this.diagnostics = List.copyOf(diagnostics);
	}

	public List<SemanticDiagnostic> diagnostics() {
		return diagnostics;
	}
}
