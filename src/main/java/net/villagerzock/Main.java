package net.villagerzock;

import net.villagerzock.compiler.ast.AstBuilder;
import net.villagerzock.compiler.ast.decl.ProgramNode;
import net.villagerzock.compiler.gen.Generator;
import net.villagerzock.compiler.parser.MCSLexer;
import net.villagerzock.compiler.parser.MCSParser;
import net.villagerzock.compiler.semantic.SemanticAnalyzer;
import net.villagerzock.mcfunction.MCFunction;
import net.villagerzock.mcfunction.MCFunctionUnit;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            Path path = Path.of("testScripts/");

            File[] filesToCompile = new File[0];

            if (Files.isDirectory(path)){
                filesToCompile = path.toFile().listFiles();
            }else {
                filesToCompile = new File[]{path.toFile()};
            }

            AstBuilder astBuilder = new AstBuilder();
            List<ProgramNode> nodes = new ArrayList<>();


            for (File file : filesToCompile){
                if (!file.exists()) {
                    System.err.println("Datei nicht gefunden: " +file.getAbsolutePath());
                    continue;
                }

                CharStream input = CharStreams.fromStream(new FileInputStream(file));

                MCSLexer lexer = new MCSLexer(input);

                CommonTokenStream tokens = new CommonTokenStream(lexer);
                MCSParser parser = new MCSParser(tokens);

                MCSParser.ProgramContext programContext = parser.program();

                if (parser.getNumberOfSyntaxErrors() > 0) {
                    System.err.println("Parsing fehlgeschlagen. Syntaxfehler: " + parser.getNumberOfSyntaxErrors());
                    return;
                }


                ProgramNode ast = (ProgramNode) astBuilder.visit(programContext);
                nodes.add(ast);
                System.out.println(ast);
            }

            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            analyzer.analyzeOrThrow(nodes);
            Generator generator = new Generator();
            MCFunctionUnit unit = generator.generate(nodes);

            Path out = Path.of("dpOut/");
            deleteRecursive(out);
            Files.createDirectory(out);

            for (MCFunction function : unit.getFunctions()){
                Path p = out.resolve(function.getNamespace()).resolve("functions").resolve(function.getPath()).resolve(function.getName() + ".mcfunction");
                File f = p.toFile();
                if (!f.exists()){
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                }
                try (FileOutputStream os = new FileOutputStream(f)) {
                    os.write(function.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            System.out.println(unit);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteRecursive(Path path) throws IOException {
        if (!Files.exists(path)) return;

        Files.walk(path)
                .sorted(Comparator.reverseOrder()) // wichtig: erst Kinder, dann Parent
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}