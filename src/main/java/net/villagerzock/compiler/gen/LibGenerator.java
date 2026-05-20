package net.villagerzock.compiler.gen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.villagerzock.compiler.ast.CompilationUnit;
import net.villagerzock.compiler.ast.decl.*;

import java.nio.file.Path;
import java.util.List;

public class LibGenerator {
    public record JsonFile(Path path, JsonArray jsonElement){}
    public JsonFile[] generate(CompilationUnit compilationUnit){
        List<ProgramNode> nodes = compilationUnit.sourcePrograms();
        JsonFile[] files = new JsonFile[nodes.size()];
        for(int i = 0; i < files.length; i++){
            ProgramNode node = nodes.get(i);
            JsonArray root = null;
            Path path = Path.of("lib").resolve(node.packagePath().namespace()).resolve(node.packagePath().path()+".json");
            JsonFile jsonFile = null;
            for (JsonFile file : files){
                if(file != null && file.path.equals(path)){
                    jsonFile = file;
                    root = file.jsonElement;
                }
            }
            if (jsonFile == null){
                root = new JsonArray();
                jsonFile = new JsonFile(path, root);
            }
            files[i] = jsonFile;

            for (ClassDeclaration declaration : node.classes()){
                JsonObject classObject = new JsonObject();
                addClass(declaration,classObject);
                root.add(classObject);
            }
        }
        return files;
    }
    private void addClass(ClassDeclaration declaration, JsonObject json){
        json.addProperty("name", declaration.name());
        json.addProperty("kind", declaration instanceof RecordDeclaration ? "record" : "class");
        if (declaration instanceof RecordDeclaration recordDeclaration) {
            JsonArray components = new JsonArray();
            for (ParameterDeclaration component : recordDeclaration.components()) {
                JsonObject componentObj = new JsonObject();
                componentObj.addProperty("name", component.name());
                componentObj.addProperty("type", component.type().resolvedType().getCanonnicalName());
                components.add(componentObj);
            }
            json.add("components", components);
        }
        JsonArray members = new JsonArray();
        for (Declaration member : declaration.members()){
            JsonObject memberObj = new JsonObject();
            addMember(member, memberObj);
            members.add(memberObj);
        }
        json.add("members",members);
    }
    private void addMember(Declaration decl, JsonObject json){
        if (decl instanceof ClassDeclaration classDeclaration){
            addClass(classDeclaration, json);
            return;
        }
        if (decl instanceof MethodDeclaration methodDeclaration){
            addMethod(methodDeclaration,json);
            return;
        }
        if (decl instanceof ConstructorDeclaration constructorDeclaration){
            addConstructor(constructorDeclaration, json);
            return;
        }
        if (decl instanceof FieldDeclaration fieldDeclaration){
            addField(fieldDeclaration, json);
        }
    }

    private void addMethod(MethodDeclaration methodDeclaration, JsonObject json) {
        json.addProperty("name", methodDeclaration.name());
        json.addProperty("kind", "method");
        json.addProperty("ref",methodDeclaration.getFunction().getFullPath());
        JsonArray parameters = new JsonArray();
        for (ParameterDeclaration parameter : methodDeclaration.parameters()){
            JsonObject param = new JsonObject();
            param.addProperty("name", parameter.name());
            param.addProperty("type", parameter.type().resolvedType().getCanonnicalName());
            parameters.add(param);
        }
        json.add("parameters",parameters);
    }

    private void addConstructor(ConstructorDeclaration constructorDeclaration, JsonObject json) {
        json.addProperty("kind", "constructor");
        if (constructorDeclaration.getFunction() != null) {
            json.addProperty("ref", constructorDeclaration.getFunction().getFullPath());
        }

        JsonArray parameters = new JsonArray();
        for (ParameterDeclaration parameter : constructorDeclaration.parameters()){
            JsonObject param = new JsonObject();
            param.addProperty("name", parameter.name());
            param.addProperty("type", parameter.type().resolvedType().getCanonnicalName());
            parameters.add(param);
        }
        json.add("parameters",parameters);
    }

    private void addField(FieldDeclaration fieldDeclaration, JsonObject json) {
        json.addProperty("name", fieldDeclaration.name());
        json.addProperty("kind", "field");
        json.addProperty("type", fieldDeclaration.type().resolvedType().getCanonnicalName());

        if (fieldDeclaration.getGetter() != null) {
            json.addProperty("getter", fieldDeclaration.getGetter().getFullPath());
        }
        if (fieldDeclaration.getSetter() != null) {
            json.addProperty("setter", fieldDeclaration.getSetter().getFullPath());
        }
    }
}
