package mylang.ast;

public record SourceRange(int startLine, int startColumn, int endLine, int endColumn) {
	public static final SourceRange UNKNOWN = new SourceRange(-1, -1, -1, -1);
}
