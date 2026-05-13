package net.villagerzock;

import com.google.gson.*;
import net.villagerzock.compiler.ast.AstBuilder;
import net.villagerzock.compiler.ast.CompilationUnit;
import net.villagerzock.compiler.ast.decl.*;
import net.villagerzock.compiler.ast.type.TypeNode;
import net.villagerzock.compiler.gen.Generator;
import net.villagerzock.compiler.gen.LibGenerator;
import net.villagerzock.compiler.optimization.Optimizer;
import net.villagerzock.compiler.parser.MCSLexer;
import net.villagerzock.compiler.parser.MCSParser;
import net.villagerzock.compiler.semantic.SemanticAnalyzer;
import net.villagerzock.mcfunction.LightMCFunction;
import net.villagerzock.mcfunction.MCFunction;
import net.villagerzock.mcfunction.MCFunctionUnit;
import net.villagerzock.plugin.PluginSystem;
import net.villagerzock.plugin.TaskType;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import picocli.CommandLine;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static picocli.CommandLine.*;

public class Main {

    public static class RuntimeData {
        @Parameters(index = "0", description = "Path to project")
        public Path projectPath;

        @Option(names = {"-o", "--output"}, description = "Output Directory", defaultValue = "./dpOut/")
        public Path outputDirectory;

        @Option(names = {"--obfuscate"}, description = "Obfuscate File Names")
        public boolean obfuscate;

        @Option(names = {"--ast"}, description = "Outputs the AST into the Console")
        public boolean ast;

        @Option(names = {"-v", "--verbose"}, description = "Outputs some Debug Info")
        public boolean verbose;

        @Option(names = {"-cp", "--classpath"}, split = ";", description = "Specify the Classpath, Can be Split with ';'")
        public String[] classpath = new String[0];

        @Option(names = {"-p","--plugins"}, split = ";",description = "A List of Plugins that will be loaded")
        public String[] plugins = new String[0];
    }

    public static final String YELLOW = "\u001B[33m";
    public static final String RESET = "\u001B[0m";

    private static final Gson gson = new Gson();
    public static final RuntimeData runtimeData = new RuntimeData();

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(runtimeData);

        try {
            cmd.parseArgs(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            cmd.usage(System.err);
            return;
        }

        PluginSystem.collectPlugins();

        if (runtimeData.obfuscate) {
            System.out.println(YELLOW + "WARN: --obfuscate disables library metadata generation. IDE support (IntelliJ/VSC) will not have method/class information." + RESET);
        }

        try {
            Path path = runtimeData.projectPath.resolve("scripts/");

            File[] filesToCompile;

            if (Files.isDirectory(path)) {
                filesToCompile = path.toFile().listFiles();
            } else {
                filesToCompile = new File[]{path.toFile()};
            }

            if (filesToCompile == null) {
                filesToCompile = new File[0];
            }

            AstBuilder astBuilder = new AstBuilder();
            CompilationUnit compilationUnit = new CompilationUnit();
            PluginSystem.onTaskStarted(TaskType.PARSE, compilationUnit);
            for (File file : filesToCompile) {
                if (!file.exists()) {
                    System.err.println("Datei nicht gefunden: " + file.getAbsolutePath());
                    continue;
                }

                try (InputStream stream = new FileInputStream(file)) {
                    CharStream input = CharStreams.fromStream(stream);

                    MCSLexer lexer = new MCSLexer(input);
                    CommonTokenStream tokens = new CommonTokenStream(lexer);
                    MCSParser parser = new MCSParser(tokens);

                    MCSParser.ProgramContext programContext = parser.program();

                    if (parser.getNumberOfSyntaxErrors() > 0) {
                        System.err.println("Parsing fehlgeschlagen. Syntaxfehler: " + parser.getNumberOfSyntaxErrors());
                        return;
                    }

                    ProgramNode ast = (ProgramNode) astBuilder.visit(programContext);
                    compilationUnit.addProgram(ast);

                    if (runtimeData.ast) {
                        System.out.println(ast);
                    }
                }
            }

            if (runtimeData.classpath != null) {
                for (String classPathPart : runtimeData.classpath) {
                    if (classPathPart == null || classPathPart.isBlank()) {
                        continue;
                    }

                    File file = new File(classPathPart);

                    if (!file.exists()) {
                        System.err.println("Classpath-Eintrag existiert nicht: " + file.getAbsolutePath());
                        continue;
                    }

                    CompilationUnit libraryUnit = loadLibrary(file);
                    for (ProgramNode node : libraryUnit.programs()) {
                        System.out.println(node);
                    }
                    compilationUnit.addCompilationUnit(libraryUnit);
                }
            }
            PluginSystem.onTaskFinished(TaskType.PARSE, compilationUnit);
            PluginSystem.onTaskStarted(TaskType.ANALYSE, compilationUnit);

            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            analyzer.analyzeOrThrow(compilationUnit);
            PluginSystem.onTaskFinished(TaskType.ANALYSE, compilationUnit);
            PluginSystem.onTaskStarted(TaskType.OPTIMIZE, compilationUnit);
            Optimizer optimizer = new Optimizer();
            optimizer.optimize(compilationUnit);
            analyzer.analyzeOrThrow(compilationUnit);

            PluginSystem.onTaskFinished(TaskType.OPTIMIZE, compilationUnit);
            PluginSystem.onTaskStarted(TaskType.GENERATE, compilationUnit);

            Generator generator = new Generator();
            MCFunctionUnit unit = generator.generate(compilationUnit);

            PluginSystem.onTaskFinished(TaskType.GENERATE, compilationUnit);

            Path out = runtimeData.outputDirectory.resolve("data/");
            deleteRecursive(out);
            Files.createDirectories(out);

            Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
            LibGenerator libGenerator = new LibGenerator();

            LibGenerator.JsonFile[] jsonFiles = libGenerator.generate(compilationUnit);

            for (LibGenerator.JsonFile jsonFile : jsonFiles) {
                Path p = out.getParent().resolve(jsonFile.path());
                File f = p.toFile();

                if (!f.exists()) {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                }

                try (FileOutputStream os = new FileOutputStream(f)) {
                    os.write(prettyGson.toJson(jsonFile.jsonElement()).getBytes(StandardCharsets.UTF_8));
                }
            }

            for (MCFunction function : unit.getFunctions()) {
                if (function instanceof LightMCFunction) continue;
                Path p = out
                        .resolve(function.getNamespace())
                        .resolve("function")
                        .resolve(function.getPath())
                        .resolve(function.getName() + ".mcfunction");

                File f = p.toFile();

                if (!f.exists()) {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                }

                try (FileOutputStream os = new FileOutputStream(f)) {
                    os.write(function.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            if (runtimeData.verbose) {
                System.out.println(unit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static CompilationUnit loadLibrary(File file) throws IOException {
        if (file.isDirectory()) {
            Path root = file.toPath();
            return loadFolderLibrary(root, root);
        }

        if (isZip(file)) {
            return loadZipLibrary(file);
        }

        if (isJson(file)) {
            try (InputStream is = new FileInputStream(file)) {
                return new CompilationUnit(List.of(loadLibraryFile(is, getSegments(file.toPath().getFileName().toString()))));
            }
        }

        if (looksLikeJson(file)) {
            try (InputStream is = new FileInputStream(file)) {
                return new CompilationUnit(List.of(loadLibraryFile(is, getSegments(stripExtension(file.getName())))));
            }
        }

        System.err.println("Classpath-Eintrag ist weder Ordner, Zip noch JSON-Lib: " + file.getAbsolutePath());
        return new CompilationUnit();
    }

    private static CompilationUnit loadFolderLibrary(Path current, Path root) throws IOException {
        CompilationUnit compilationUnit = new CompilationUnit();

        File[] files = current.toFile().listFiles();
        if (files == null) {
            return compilationUnit;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                compilationUnit.addCompilationUnit(loadFolderLibrary(file.toPath(), root));
                continue;
            }

            if (!isJson(file) && !looksLikeJson(file)) {
                continue;
            }

            Path relative = root.relativize(file.toPath());
            List<String> segments = getSegments(relative.toString());

            try (InputStream is = new FileInputStream(file)) {
                compilationUnit.addProgram(loadLibraryFile(is, segments));
            }
        }

        return compilationUnit;
    }

    private static CompilationUnit loadZipLibrary(File zipFile) throws IOException {
        CompilationUnit compilationUnit = new CompilationUnit();

        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();

                if (!name.endsWith(".json") || !name.startsWith("lib")) {
                    continue;
                }

                try (InputStream is = zip.getInputStream(entry)) {
                    compilationUnit.addProgram(loadLibraryFile(is, getSegments(name)));
                }
            }
        }

        return compilationUnit;
    }

    private static List<String> getSegments(String path) {
        String result = path.replace('\\', '/');

        if (result.endsWith(".json")) {
            result = result.substring(0, result.length() - 5);
        }

        while (result.startsWith("/")) {
            result = result.substring(1);
        }

        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }

        if (result.isBlank()) {
            throw new IllegalArgumentException("Library JSON need a name/path.");
        }

        return Arrays.stream(result.split("/"))
                .filter(s -> !s.isBlank())
                .toList();
    }

    private static ProgramNode loadLibraryFile(InputStream is, List<String> pathSegments) {
        JsonElement parsed = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonElement.class);

        if (parsed == null || !parsed.isJsonArray()) {
            throw new IllegalArgumentException("Library file requires a JSON-Array as root.");
        }

        JsonArray root = parsed.getAsJsonArray();

        QualifiedPathNode pathNode = new QualifiedPathNode(
                pathSegments.getFirst(),
                pathSegments.subList(1,pathSegments.size())
        );

        List<ClassDeclaration> classDeclarations = new ArrayList<>();

        for (JsonElement element : root) {
            JsonObject clazz = element.getAsJsonObject();
            classDeclarations.add(loadClassFromJson(clazz));
        }

        ProgramNode node = new ProgramNode(pathNode, classDeclarations);
        return node;
    }

    private static Declaration loadDeclarationFromJson(JsonObject decl) {
        String kind = decl.get("kind").getAsString();

        if (Objects.equals(kind, "class")) {
            return loadClassFromJson(decl);
        }

        if (Objects.equals(kind, "method")) {
            return loadMethodFromJson(decl);
        }

        return null;
    }

    private static ClassDeclaration loadClassFromJson(JsonObject clazz) {
        String name = clazz.get("name").getAsString();
        JsonArray members = clazz.getAsJsonArray("members");

        List<Declaration> declarationList = new ArrayList<>();

        if (members != null) {
            for (JsonElement member : members) {
                JsonObject obj = member.getAsJsonObject();
                Declaration declaration = loadDeclarationFromJson(obj);

                if (declaration != null) {
                    declarationList.add(declaration);
                }
            }
        }

        return new ClassDeclaration(name, declarationList);
    }

    private static MethodDeclaration loadMethodFromJson(JsonObject clazz) {
        String name = clazz.get("name").getAsString();
        String ref = clazz.get("ref").getAsString();
        JsonArray members = clazz.getAsJsonArray("parameters");

        List<ParameterDeclaration> declarationList = new ArrayList<>();

        if (members != null) {
            for (JsonElement member : members) {
                JsonObject obj = member.getAsJsonObject();

                String paramName = obj.get("name").getAsString();
                String paramType = obj.get("type").getAsString();

                declarationList.add(new ParameterDeclaration(new TypeNode(paramType), paramName));
            }
        }

        return new LightMethodDeclaration(new TypeNode("function"), name, declarationList, new LightMCFunction(ref));
    }

    private static boolean isZip(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".zip") || name.endsWith(".jar");
    }

    private static boolean isJson(File file) {
        return file.getName().toLowerCase(Locale.ROOT).endsWith(".json");
    }

    private static boolean looksLikeJson(File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            return element != null && element.isJsonArray();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String stripExtension(String name) {
        int index = name.lastIndexOf('.');
        return index <= 0 ? name : name.substring(0, index);
    }

    public static void deleteRecursive(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static List<ProgramNode> parseSourcePath(Path path, AstBuilder astBuilder) throws IOException {
        List<ProgramNode> nodes = new ArrayList<>();

        if (!Files.exists(path)) {
            System.err.println("Source path not found: " + path.toAbsolutePath());
            return nodes;
        }

        if (Files.isDirectory(path)) {
            try (var stream = Files.walk(path)) {
                List<File> files = stream
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .toList();

                for (File file : files) {
                    nodes.addAll(parseSourceFile(file, astBuilder));
                }
            }

            return nodes;
        }

        nodes.addAll(parseSourceFile(path.toFile(), astBuilder));
        return nodes;
    }

    private static List<ProgramNode> parseSourceFile(File file, AstBuilder astBuilder) throws IOException {
        List<ProgramNode> nodes = new ArrayList<>();

        if (!file.exists()) {
            System.err.println("File not found: " + file.getAbsolutePath());
            return nodes;
        }

        if (!file.isFile()) {
            return nodes;
        }

        try (InputStream stream = new FileInputStream(file)) {
            CharStream input = CharStreams.fromStream(stream);

            MCSLexer lexer = new MCSLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MCSParser parser = new MCSParser(tokens);

            MCSParser.ProgramContext programContext = parser.program();

            if (parser.getNumberOfSyntaxErrors() > 0) {
                throw new IllegalStateException("Parsing failed for " + file.getAbsolutePath() + ". Syntax errors: " + parser.getNumberOfSyntaxErrors());
            }

            ProgramNode ast = (ProgramNode) astBuilder.visit(programContext);
            nodes.add(ast);

            if (runtimeData.ast) {
                System.out.println(ast);
            }
        }

        return nodes;
    }
}
