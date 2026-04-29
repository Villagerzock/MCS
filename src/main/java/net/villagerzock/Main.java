package net.villagerzock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.villagerzock.compiler.ast.AstBuilder;
import net.villagerzock.compiler.ast.decl.ProgramNode;
import net.villagerzock.compiler.gen.Generator;
import net.villagerzock.compiler.gen.LibGenerator;
import net.villagerzock.compiler.gen.PathStack;
import net.villagerzock.compiler.parser.MCSLexer;
import net.villagerzock.compiler.parser.MCSParser;
import net.villagerzock.compiler.semantic.SemanticAnalyzer;
import net.villagerzock.mcfunction.MCFunction;
import net.villagerzock.mcfunction.MCFunctionUnit;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import picocli.CommandLine;

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

import static picocli.CommandLine.*;

public class Main {

    public static class RuntimeData {
        @Parameters(index = "0", description = "Path to project")
        public Path projectPath;

        @Option(names = {"-o","--output"}, description = "Output Directory", defaultValue = "./dpOut/")
        public Path outputDirectory;

        @Option(names = {"--obfuscate"}, description = "Obfuscate File Names")
        public boolean obfuscate;

        @Option(names = {"--ast"}, description = "Outputs the AST into the Console")
        public boolean ast;

        @Option(names = {"-v","--verbose"},description = "Outputs some Debug Info")
        public boolean verbose;

        @Option(names = {"-cp","--classpath"},split = ";", description = "Specify the Classpath, Can be Split with ';'")
        public String[] classpath;
    }

    public static final String YELLOW = "\u001B[33m";
    public static final String RESET = "\u001B[0m";


    public static final RuntimeData runtimeData = new RuntimeData();

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(runtimeData);
        cmd.parseArgs(args);

        if (runtimeData.obfuscate){
            System.out.println(YELLOW + "WARN: --obfuscate disables library metadata generation. IDE support (IntelliJ/VSC) will not have method/class information." + RESET);
        }

        try {
            Path path = runtimeData.projectPath.resolve("scripts/");

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
                if (runtimeData.ast){
                    System.out.println(ast);
                }
            }

            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            analyzer.analyzeOrThrow(nodes);
            Generator generator = new Generator();
            MCFunctionUnit unit = generator.generate(nodes);

            Path out = runtimeData.outputDirectory.resolve("data/");
            deleteRecursive(out);
            Files.createDirectory(out);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            LibGenerator libGenerator = new LibGenerator();
            LibGenerator.JsonFile[] jsonFiles = libGenerator.generate(nodes);
            for (LibGenerator.JsonFile jsonFile : jsonFiles) {
                Path p = out.getParent().resolve(jsonFile.path());
                File f = p.toFile();
                if (!f.exists()){
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                }
                try (FileOutputStream os = new FileOutputStream(f)) {
                    os.write(gson.toJson(jsonFile.jsonElement()).getBytes(StandardCharsets.UTF_8));
                }
            }
            for (MCFunction function : unit.getFunctions()){
                Path p = out.resolve(function.getNamespace()).resolve("function").resolve(function.getPath()).resolve(function.getName() + ".mcfunction");
                File f = p.toFile();
                if (!f.exists()){
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                }
                try (FileOutputStream os = new FileOutputStream(f)) {
                    os.write(function.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            if (runtimeData.verbose){
                System.out.println(unit);
            }

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