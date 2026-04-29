package net.villagerzock.compiler.ast;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class AstNode implements Node {
	private final SourceRange sourceRange;

	protected AstNode() {
		this(SourceRange.UNKNOWN);
	}

	protected AstNode(SourceRange sourceRange) {
		this.sourceRange = sourceRange == null ? SourceRange.UNKNOWN : sourceRange;
	}

	public SourceRange sourceRange() {
		return sourceRange;
	}



	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder();
		appendTree(builder, "", true);
		return builder.toString();
	}

    private void appendTree(StringBuilder builder, String prefix, boolean isTail) {
        if (prefix.isEmpty()) {
            builder.append(getString()).append('\n');
        } else {
            builder.append(prefix)
                    .append(isTail ? "┗━━ " : "┣━━ ")
                    .append(getString())
                    .append('\n');
        }

        List<AstNode> children = getChildren();

        // nulls rausfiltern, damit "last" stimmt
        List<AstNode> validChildren = new ArrayList<>();
        for (AstNode child : children) {
            if (child != null) validChildren.add(child);
        }

        for (int i = 0; i < validChildren.size(); i++) {
            AstNode child = validChildren.get(i);
            boolean last = i == validChildren.size() - 1;

            String childPrefix = prefix + (isTail ? "\t" : "┃\t");

            child.appendTree(builder, childPrefix, last);
        }
    }

	protected List<AstNode> getChildren() {
		List<AstNode> children = new ArrayList<>();
		Class<?> current = getClass();

		while (current != null && AstNode.class.isAssignableFrom(current)) {
			for (Field field : current.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				if ("sourceRange".equals(field.getName())) {
					continue;
				}

				field.setAccessible(true);
				Object value;
				try {
					value = field.get(this);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Failed to inspect AST field: " + field.getName(), e);
				}

				if (value instanceof AstNode node) {
					children.add(node);
				} else if (value instanceof Iterable<?> iterable) {
					for (Object item : iterable) {
						if (item instanceof AstNode node) {
							children.add(node);
						}
					}
				}
			}
			current = current.getSuperclass();
		}
		return children;
	}
}
