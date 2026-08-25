package com.bajinho.continuebeans.ai;

import com.bajinho.continuebeans.security.ToolExecutionPolicy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Real implementations for code-generation tools exposed to the AI agent. */
public final class RealCodeTools {

    private RealCodeTools() {
    }

    public static NetBeansFunctionExecutor.FunctionResult execute(
            String functionName, Map<String, Object> args) {
        try {
            switch (functionName) {
                case "generate_class":
                    return generateClass(args);
                case "generate_interface":
                    return generateInterface(args);
                case "generate_test_method":
                    return generateTest(args);
                case "add_dependency":
                    return addDependency(args);
                default:
                    return NetBeansFunctionExecutor.FunctionResult.error(
                            "Tool não implementada no RealCodeTools: " + functionName);
            }
        } catch (Exception e) {
            return NetBeansFunctionExecutor.FunctionResult.error(
                    "Falha na tool " + functionName + ": " + e.getMessage());
        }
    }

    private static NetBeansFunctionExecutor.FunctionResult generateClass(Map<String, Object> args)
            throws IOException {
        String className = required(args, "className");
        String packageName = required(args, "packageName");
        Path source = javaSourceRoot().resolve(packageName.replace('.', '/'))
                .resolve(className + ".java");
        ensureNewFile(source);

        StringBuilder code = new StringBuilder();
        appendPackage(code, packageName);
        code.append("public class ").append(className);

        String extendsClass = optional(args, "extendsClass");
        if (!extendsClass.isBlank()) {
            code.append(" extends ").append(extendsClass);
        }

        List<String> interfaces = stringList(args.get("implements"));
        if (!interfaces.isEmpty()) {
            code.append(" implements ").append(String.join(", ", interfaces));
        }
        code.append(" {\n\n");

        for (String field : stringList(args.get("fields"))) {
            code.append("    private ").append(field).append(";\n");
        }
        if (!stringList(args.get("fields")).isEmpty()) {
            code.append('\n');
        }
        for (String method : stringList(args.get("methods"))) {
            code.append("    ").append(method).append("\n\n");
        }
        code.append("}\n");

        write(source, code.toString());
        return success("Classe criada", source, code.toString());
    }

    private static NetBeansFunctionExecutor.FunctionResult generateInterface(Map<String, Object> args)
            throws IOException {
        String name = required(args, "interfaceName");
        String packageName = required(args, "packageName");
        Path source = javaSourceRoot().resolve(packageName.replace('.', '/'))
                .resolve(name + ".java");
        ensureNewFile(source);

        StringBuilder code = new StringBuilder();
        appendPackage(code, packageName);
        code.append("public interface ").append(name).append(" {\n\n");
        for (String method : stringList(args.get("methods"))) {
            code.append("    ").append(normalizeInterfaceMethod(method)).append("\n");
        }
        code.append("}\n");

        write(source, code.toString());
        return success("Interface criada", source, code.toString());
    }

    private static NetBeansFunctionExecutor.FunctionResult generateTest(Map<String, Object> args)
            throws IOException {
        String className = required(args, "className");
        String packageName = optional(args, "packageName");
        String testPackage = packageName.isBlank() ? packageFromWorkspace() : packageName;
        Path source = javaTestRoot().resolve(testPackage.replace('.', '/'))
                .resolve(className + "Test.java");
        ensureNewFile(source);

        StringBuilder code = new StringBuilder();
        if (!testPackage.isBlank()) {
            appendPackage(code, testPackage);
        }
        code.append("import org.junit.jupiter.api.Test;\n\n");
        code.append("class ").append(className).append("Test {\n\n");
        List<String> methods = stringList(args.get("testMethods"));
        if (methods.isEmpty()) {
            methods = List.of("shouldCreateInstance");
        }
        for (String method : methods) {
            String methodName = method.replaceAll("[^A-Za-z0-9_]", "_");
            code.append("    @Test\n");
            code.append("    void ").append(methodName).append("() {\n");
            code.append("        // TODO: add assertions for ").append(methodName).append("\n");
            code.append("    }\n\n");
        }
        code.append("}\n");

        write(source, code.toString());
        return success("Teste criado", source, code.toString());
    }

    private static NetBeansFunctionExecutor.FunctionResult addDependency(Map<String, Object> args)
            throws Exception {
        String groupId = required(args, "groupId");
        String artifactId = required(args, "artifactId");
        String version = optional(args, "version");
        String scope = optional(args, "scope");
        if (scope.isBlank()) {
            scope = "compile";
        }

        Path pom = findPom();
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(pom.toFile());
        Element project = document.getDocumentElement();
        Element dependencies = firstChild(project, "dependencies");
        if (dependencies == null) {
            dependencies = document.createElement("dependencies");
            project.appendChild(dependencies);
        }

        for (Element dependency : children(dependencies, "dependency")) {
            String existingGroup = text(dependency, "groupId");
            String existingArtifact = text(dependency, "artifactId");
            if (groupId.equals(existingGroup) && artifactId.equals(existingArtifact)) {
                return NetBeansFunctionExecutor.FunctionResult.error(
                        "Dependência já existe: " + groupId + ":" + artifactId);
            }
        }

        Element dependency = document.createElement("dependency");
        appendElement(document, dependency, "groupId", groupId);
        appendElement(document, dependency, "artifactId", artifactId);
        if (!version.isBlank()) {
            appendElement(document, dependency, "version", version);
        }
        if (!"compile".equals(scope)) {
            appendElement(document, dependency, "scope", scope);
        }
        dependencies.appendChild(dependency);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        var transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(document), new StreamResult(pom.toFile()));

        return NetBeansFunctionExecutor.FunctionResult.success(
                "Dependência adicionada", Map.of(
                        "pom", pom.toString(),
                        "groupId", groupId,
                        "artifactId", artifactId,
                        "version", version,
                        "scope", scope,
                        "status", "updated"));
    }

    private static Path javaSourceRoot() throws IOException {
        return findSourceRoot("src/main/java");
    }

    private static Path javaTestRoot() throws IOException {
        return findSourceRoot("src/test/java");
    }

    private static Path findSourceRoot(String relative) throws IOException {
        Path root = ToolExecutionPolicy.workspaceRoot();
        Path candidate = root.resolve(relative);
        if (Files.isDirectory(candidate)) {
            return candidate;
        }
        try (var stream = Files.walk(root, 5)) {
            return stream.filter(Files::isDirectory)
                    .filter(p -> p.endsWith(Path.of(relative.split("/"))))
                    .findFirst()
                    .orElseThrow(() -> new IOException("Diretório " + relative + " não encontrado no workspace"));
        }
    }

    private static Path findPom() throws IOException {
        Path root = ToolExecutionPolicy.workspaceRoot();
        Path direct = root.resolve("pom.xml");
        if (Files.isRegularFile(direct)) {
            return direct;
        }
        try (var stream = Files.walk(root, 5)) {
            return stream.filter(p -> p.getFileName().toString().equals("pom.xml"))
                    .findFirst()
                    .orElseThrow(() -> new IOException("pom.xml não encontrado no workspace"));
        }
    }

    private static void ensureNewFile(Path path) throws IOException {
        Path normalized = ToolExecutionPolicy.requireWorkspacePath(path.toString());
        if (Files.exists(normalized)) {
            throw new IOException("Arquivo já existe: " + normalized);
        }
        Files.createDirectories(normalized.getParent());
    }

    private static void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);
    }

    private static String required(Map<String, Object> args, String key) {
        String value = optional(args, key);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Parâmetro obrigatório: " + key);
        }
        return value;
    }

    private static String optional(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?>)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object entry : (List<?>) value) {
            if (entry != null && !String.valueOf(entry).isBlank()) {
                result.add(String.valueOf(entry).trim());
            }
        }
        return result;
    }

    private static void appendPackage(StringBuilder code, String packageName) {
        if (!packageName.isBlank()) {
            code.append("package ").append(packageName).append(";\n\n");
        }
    }

    private static String normalizeInterfaceMethod(String method) {
        String value = method.trim();
        return value.endsWith(";") ? value : value + ";";
    }

    private static NetBeansFunctionExecutor.FunctionResult success(
            String message, Path path, String content) {
        return NetBeansFunctionExecutor.FunctionResult.success(message, Map.of(
                "filePath", path.toString(),
                "content", content,
                "size", content.length(),
                "status", "created"));
    }

    private static String packageFromWorkspace() {
        return "";
    }

    private static Element firstChild(Element parent, String tagName) {
        for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element && tagName.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }

    private static List<Element> children(Element parent, String tagName) {
        List<Element> result = new ArrayList<>();
        for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element && tagName.equals(child.getNodeName())) {
                result.add((Element) child);
            }
        }
        return result;
    }

    private static String text(Element parent, String tagName) {
        Element element = firstChild(parent, tagName);
        return element == null ? "" : element.getTextContent().trim();
    }

    private static void appendElement(Document document, Element parent, String name, String value) {
        Element child = document.createElement(name);
        child.setTextContent(value);
        parent.appendChild(child);
    }
}
