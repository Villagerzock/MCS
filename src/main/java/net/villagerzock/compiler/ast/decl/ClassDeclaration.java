package net.villagerzock.compiler.ast.decl;

import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.SourceRange;
import net.villagerzock.mcfunction.MCFunction;
import net.villagerzock.mcfunction.MCFunctionUnit;

import java.util.List;
import java.util.function.Supplier;

public class ClassDeclaration extends AstNode implements Declaration {
	private final String name;
	private final List<Declaration> members;
	private MCFunction init = null;

	public ClassDeclaration(String name, List<Declaration> members) {
		this(name, members, SourceRange.UNKNOWN);
	}

	public ClassDeclaration(String name, List<Declaration> members, SourceRange sourceRange) {
		super(sourceRange);
		this.name = name;
		this.members = List.copyOf(members);
	}

	public String name() {
		return name;
	}

	public List<Declaration> members() {
		return members;
	}

	public MCFunction getInit(Supplier<MCFunction> createIfNotExists) {
		if (init == null){
			init = createIfNotExists.get();
			if (init == null) return null;
			init.setUsesMacros(true);
		}
		return init;
	}

	@Override
	public String getString() {
		return "Class(" + name() + ")";
	}

    public String getCanonnicalName() {
        return "";
    }
}
