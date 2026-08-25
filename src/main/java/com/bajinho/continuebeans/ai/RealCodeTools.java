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

/** Real, Java 11-compatible implementations for code-generation tools. */
public final class RealCodeTools {
    private RealCodeTools() { }

    public static NetBeansFunctionExecutor.FunctionResult execute(String name, Map<String, Object> args) {
        try {
            switch (name) {
                case "generate_class": return generateClass(args);
                case "generate_interface": return generateInterface(args);
                case "generate_test_method": return generateTest(args);
                case "add_dependency": return addDependency(args);
                default: return NetBeansFunctionExecutor.FunctionResult.error("Tool não implementada: " + name);
            }
        } catch (Exception e) {
            return NetBeansFunctionExecutor.FunctionResult.error("Falha na tool " + name + ": " + e.getMessage());
        }
    }

    private static NetBeansFunctionExecutor.FunctionResult generateClass(Map<String, Object> args) throws IOException {
        String className = required(args, "className");
        String packageName = required(args, "packageName");
        Path file = sourceRoot().resolve(packageName.replace('.', '/')).resolve(className + ".java");
        createNewFile(file, buildClass(args, className, packageName));
        return created(file, "Classe criada");
    }

    private static String buildClass(Map<String, Object> args, String name, String pkg) {
        StringBuilder code = new StringBuilder();
        code.append("package ").append(pkg).append(";\n\npublic class ").append(name);
        String parent = optional(args, "extendsClass");
        if (!parent.isEmpty()) code.append(" extends ").append(parent);
        List<String> interfaces = strings(args.get("implements"));
        if (!interfaces.isEmpty()) code.append(" implements ").append(String.join(", ", interfaces));
        code.append(" {\n\n");
        for (String field : strings(args.get("fields"))) code.append("    private ").append(field).append(";\n");
        if (!strings(args.get("fields")).isEmpty()) code.append('\n');
        for (String method : strings(args.get("methods"))) code.append("    ").append(method).append("\n\n");
        code.append("}\n");
        return code.toString();
    }

    private static NetBeansFunctionExecutor.FunctionResult generateInterface(Map<String, Object> args) throws IOException {
        String name = required(args, "interfaceName");
        String pkg = required(args, "packageName");
        Path file = sourceRoot().resolve(pkg.replace('.', '/')).resolve(name + ".java");
        StringBuilder code = new StringBuilder("package ").append(pkg).append(";\n\npublic interface ").append(name).append(" {\n\n");
        for (String method : strings(args.get("methods"))) {
            code.append("    ").append(method.endsWith(";") ? method : method + ";").append("\n");
        }
        code.append("}\n");
        createNewFile(file, code.toString());
        return created(file, "Interface criada");
    }

    private static NetBeansFunctionExecutor.FunctionResult generateTest(Map<String, Object> args) throws IOException {
        String className = required(args, "className");
        String pkg = optional(args, "packageName");
        Path file = testRoot().resolve(pkg.isEmpty() ? "" : pkg.replace('.', '/')).resolve(className + "Test.java");
        List<String> methods = strings(args.get("testMethods"));
        if (methods.isEmpty()) {
            return NetBeansFunctionExecutor.FunctionResult.error(
                    "generate_test_method exige pelo menos uma asserção Java em testMethods");
        }

        String framework = optional(args, "testFramework");
        if (framework.isEmpty()) framework = "junit";
        if (!"junit".equalsIgnoreCase(framework)) {
            return NetBeansFunctionExecutor.FunctionResult.error(
                    "Framework de teste não suportado: " + framework + ". Use junit.");
        }

        StringBuilder code = new StringBuilder();
        if (!pkg.isEmpty()) code.append("package ").append(pkg).append(";\n\n");
        code.append("import org.junit.jupiter.api.Test;\n")
                .append("import static org.junit.jupiter.api.Assertions.*;\n\n")
                .append("class ").append(className).append("Test {\n\n");
        for (int i = 0; i < methods.size(); i++) {
            String assertion = methods.get(i).trim();
            validateAssertion(assertion);
            String methodName = "shouldPassGeneratedScenario" + (i + 1);
            code.append("    @Test\n    void ").append(methodName).append("() {\n")
                    .append("        ").append(assertion);
            if (!assertion.endsWith(";")) code.append(';');
            code.append("\n    }\n\n");
        }
        code.append("}\n");
        createNewFile(file, code.toString());
        return created(file, "Teste criado");
    }

    private static void validateAssertion(String assertion) {
        if (assertion.isEmpty()) {
            throw new IllegalArgumentException("Asserção de teste vazia");
        }
        String normalized = assertion.replaceAll("\\s+", " ").trim();
        if (!(normalized.startsWith("assert") || normalized.startsWith("Assertions.assert"))) {
            throw new IllegalArgumentException(
                    "testMethods deve conter asserções JUnit reais, por exemplo assertEquals(...)");
        }
    }

    private static NetBeansFunctionExecutor.FunctionResult addDependency(Map<String, Object> args) throws Exception {
        String groupId = required(args, "groupId");
        String artifactId = required(args, "artifactId");
        String version = optional(args, "version");
        String scope = optional(args, "scope");
        if (scope.isEmpty()) scope = "compile";

        Path pom = findPom();
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pom.toFile());
        Element project = document.getDocumentElement();
        Element dependencies = firstChild(project, "dependencies");
        if (dependencies == null) {
            dependencies = document.createElement("dependencies");
            project.appendChild(dependencies);
        }
        for (Element dep : children(dependencies, "dependency")) {
            if (groupId.equals(text(dep, "groupId")) && artifactId.equals(text(dep, "artifactId"))) {
                return NetBeansFunctionExecutor.FunctionResult.error("Dependência já existe: " + groupId + ":" + artifactId);
            }
        }
        Element dep = document.createElement("dependency");
        append(document, dep, "groupId", groupId);
        append(document, dep, "artifactId", artifactId);
        if (!version.isEmpty()) append(document, dep, "version", version);
        if (!"compile".equals(scope)) append(document, dep, "scope", scope);
        dependencies.appendChild(dep);

        javax.xml.transform.Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(document), new StreamResult(pom.toFile()));
        return NetBeansFunctionExecutor.FunctionResult.success("Dependência adicionada", Map.of(
                "pom", pom.toString(), "groupId", groupId, "artifactId", artifactId,
                "version", version, "scope", scope, "status", "updated"));
    }

    private static Path sourceRoot() throws IOException { return findRoot("src/main/java"); }
    private static Path testRoot() throws IOException { return findRoot("src/test/java"); }

    private static Path findRoot(String relative) throws IOException {
        Path root = ToolExecutionPolicy.workspaceRoot();
        Path direct = root.resolve(relative);
        if (Files.isDirectory(direct)) return direct;
        Path result = root;
        String[] parts = relative.split("/");
        try (java.util.stream.Stream<Path> stream = Files.walk(root, 5)) {
            result = stream.filter(Files::isDirectory).filter(p -> endsWithSegments(p, parts)).findFirst().orElse(null);
        }
        if (result == null) throw new IOException("Diretório " + relative + " não encontrado no workspace");
        return result;
    }

    private static boolean endsWithSegments(Path path, String[] parts) {
        Path current = path;
        for (int i = parts.length - 1; i >= 0; i--) {
            if (current == null || !parts[i].equals(current.getFileName().toString())) return false;
            current = current.getParent();
        }
        return true;
    }

    private static Path findPom() throws IOException {
        Path root = ToolExecutionPolicy.workspaceRoot();
        Path direct = root.resolve("pom.xml");
        if (Files.isRegularFile(direct)) return direct;
        try (java.util.stream.Stream<Path> stream = Files.walk(root, 5)) {
            return stream.filter(p -> p.getFileName() != null && "pom.xml".equals(p.getFileName().toString()))
                    .findFirst().orElseThrow(() -> new IOException("pom.xml não encontrado no workspace"));
        }
    }

    private static void createNewFile(Path file, String content) throws IOException {
        Path safe = ToolExecutionPolicy.requireWorkspacePath(file.toString());
        if (Files.exists(safe)) throw new IOException("Arquivo já existe: " + safe);
        Files.createDirectories(safe.getParent());
        Files.write(safe, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
    }

    private static NetBeansFunctionExecutor.FunctionResult created(Path file, String message) throws IOException {
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        return NetBeansFunctionExecutor.FunctionResult.success(message, Map.of(
                "filePath", file.toString(), "content", content, "size", content.length(), "status", "created"));
    }

    private static String required(Map<String, Object> args, String key) {
        String value = optional(args, key);
        if (value.isEmpty()) throw new IllegalArgumentException("Parâmetro obrigatório: " + key);
        return value;
    }

    private static String optional(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static List<String> strings(Object value) {
        if (!(value instanceof List<?>)) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (Object item : (List<?>) value) if (item != null && !String.valueOf(item).trim().isEmpty()) result.add(String.valueOf(item).trim());
        return result;
    }

    private static Element firstChild(Element parent, String tag) {
        for (org.w3c.dom.Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element && tag.equals(node.getNodeName())) return (Element) node;
        }
        return null;
    }

    private static List<Element> children(Element parent, String tag) {
        List<Element> result = new ArrayList<>();
        for (org.w3c.dom.Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element && tag.equals(node.getNodeName())) result.add((Element) node);
        }
        return result;
    }

    private static String text(Element parent, String tag) {
        Element child = firstChild(parent, tag);
        return child == null ? "" : child.getTextContent().trim();
    }

    private static void append(Document document, Element parent, String tag, String value) {
        Element child = document.createElement(tag);
        child.setTextContent(value);
        parent.appendChild(child);
    }
}
