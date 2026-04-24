package net.villagerzock;

import net.villagerzock.compiler.ast.AstBuilder;
import net.villagerzock.compiler.ast.AstNode;
import net.villagerzock.compiler.ast.Node;
import net.villagerzock.compiler.parser.MCSLexer;
import net.villagerzock.compiler.parser.MCSParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        try {
            Path path = Path.of("examplescript.mcs");

            if (!Files.exists(path)) {
                System.err.println("Datei nicht gefunden: " + path.toAbsolutePath());
                return;
            }

            CharStream input = CharStreams.fromPath(path);

            MCSLexer lexer = new MCSLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MCSParser parser = new MCSParser(tokens);

            MCSParser.ProgramContext programContext = parser.program();

            if (parser.getNumberOfSyntaxErrors() > 0) {
                System.err.println("Parsing fehlgeschlagen. Syntaxfehler: " + parser.getNumberOfSyntaxErrors());
                return;
            }

            AstBuilder astBuilder = new AstBuilder();
            Node ast = astBuilder.visit(programContext);

            System.out.println(ast);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}