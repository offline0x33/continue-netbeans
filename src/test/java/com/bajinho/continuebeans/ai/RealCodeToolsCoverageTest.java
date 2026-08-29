package com.bajinho.continuebeans.ai;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Cobertura comportamental das ferramentas de geração de código em {@link RealCodeTools}. */
class RealCodeToolsCoverageTest {
    @TempDir
    Path workspace;

    @BeforeEach
    void setWorkspace() {
        System.setProperty("continuebeans.workspace", workspace.toString());
    }

    @AfterEach
    void clearWorkspaceProperty() {
        System.clearProperty("continuebeans.workspace");
    }

    private NetBeansFunctionExecutor.FunctionResult execute(String name, Map<String, Object> args) throws Exception {
        return RealCodeTools.execute(name, args);
    }

    // ------------------------------------------------------------------ dispatch

    @Test
    void unknownToolReturnsError() throws Exception {
        NetBeansFunctionExecutor.FunctionResult result = execute("unknown_tool", Map.of());
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Tool não implementada"));
    }

    // ------------------------------------------------------------- generate_class

    @Test
    void generateClassMinimalCreatesFile() throws Exception {
        Files.createDirectories(workspace.resolve("src/main/java"));

        NetBeansFunctionExecutor.FunctionResult result = execute("generate_class", Map.of(
                "className", "Calculator", "packageName", "com.example"));

        assertTrue(result.isSuccess());
        Path file = workspace.resolve("src/main/java/com/example/Calculator.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.startsWith("package com.example;"));
        assertTrue(content.contains("public class Calculator {"));
        assertEquals("created", result.getData().get("status"));
    }

    @Test
    void generateClassWithInheritanceFieldsAndMethods() throws Exception {
        Files.createDirectories(workspace.resolve("src/main/java"));

        NetBeansFunctionExecutor.FunctionResult result = execute("generate_class", Map.of(
                "className", "Calculator", "packageName", "com.example",
                "extendsClass", "BaseCalc",
                "implements", List.of("Serializable", "Cloneable"),
                "fields", List.of("int total", "String label"),
                "methods", List.of("public int add(int a, int b) { return a + b; }")));

        assertTrue(result.isSuccess());
        String content = Files.readString(workspace.resolve("src/main/java/com/example/Calculator.java"), StandardCharsets.UTF_8);
        assertTrue(content.contains("extends BaseCalc"));
        assertTrue(content.contains("implements Serializable, Cloneable"));
        assertTrue(content.contains("private int total;"));
        assertTrue(content.contains("public int add(int a, int b) { return a + b; }"));
    }

    @Test
    void generateClassRequiresClassName() throws Exception {
        Files.createDirectories(workspace.resolve("src/main/java"));

        NetBeansFunctionExecutor.FunctionResult result = execute("generate_class", Map.of(
                "packageName", "com.example"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Parâmetro obrigatório: className"));
    }

    @Test
    void generateClassFailsWhenFileAlreadyExists() throws Exception {
        Files.createDirectories(workspace.resolve("src/main/java/com/example"));
        Path existing = workspace.resolve("src/main/java/com/example/Calculator.java");
        Files.write(existing, "old".getBytes(StandardCharsets.UTF_8));

        NetBeansFunctionExecutor.FunctionResult result = execute("generate_class", Map.of(
                "className", "Calculator", "packageName", "com.example"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Arquivo já existe"));
    }

    @Test
    void generateClassFailsWhenSourceRootMissing() throws Exception {
        NetBeansFunctionExecutor.FunctionResult result = execute("generate_class", Map.of(
                "className", "Calculator", "packageName", "com.example"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("src/main/java não encontrado no workspace"));
    }

    @Test
    void generateClassFindsSourceRootByWalk() throws Exception {
        Path nested = workspace.resolve("project/src/main/java");
        Files.createDirectories(nested);

        NetBeansFunctionExecutor.FunctionResult result = execute("generate_class", Map.of(
                "className", "Calculator", "packageName", "com.example"));

        assertTrue(result.isSuccess());
        assertTrue(Files.exists(nested.resolve("com/example/Calculator.java")));
    }

    // ---------------------------------------------------------- generate_interface

    @Test
    void generateInterfaceAddsMissingSemicolons() throws Exception {
        Files.createDirectories(workspace.resolve("src/main/java"));

        NetBeansFunctionExecutor.FunctionResult result = execute("generate_interface", Map.of(
                "interfaceName", "Calculator", "packageName", "com.example",
                "methods", List.of("int add(int a, int b)", "void reset()")));

        assertTrue(result.isSuccess());
        String content = Files.readString(workspace.resolve("src/main/java/com/example/Calculator.java"), StandardCharsets.UTF_8);
        assertTrue(content.contains("public interface Calculator {"));
        assertTrue(content.contains("int add(int a, int b);"));
        assertTrue(content.contains("void reset();"));
    }

    @Test
    void generateInterfaceRequiresPackageName() throws Exception {
        Files.createDirectories(workspace.resolve("src/main/java"));

        NetBeansFunctionExecutor.FunctionResult result = execute("generate_interface", Map.of(
                "interfaceName", "Calculator"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Parâmetro obrigatório: packageName"));
    }

    // ---------------------------------------------------------- generate_test_method

    @Test
    void generateTestWithPackage() throws Exception {
        Files.createDirectories(workspace.resolve("src/test/java"));

        NetBeansFunctionExecutor.FunctionResult result = execute("generate_test_method", Map.of(
                "className", "Calculator", "packageName", "com.example",
                "testMethods", List.of("assertEquals(2, 1 + 1)")));

        assertTrue(result.isSuccess());
        String content = Files.readString(workspace.resolve("src/test/java/com/example/CalculatorTest.java"), StandardCharsets.UTF_8);
        assertTrue(content.contains("package com.example;"));
        assertTrue(content.contains("@Test"));
    }

    @Test
    void generateTestRejectsNonJunitFramework() throws Exception {
        Files.createDirectories(workspace.resolve("src/test/java"));

        NetBeansFunctionExecutor.FunctionResult result = execute("generate_test_method", Map.of(
                "className", "Calculator", "testMethods", List.of("assertEquals(1, 1)"),
                "testFramework", "testng"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Framework de teste não suportado"));
    }

    // --------------------------------------------------------------- add_dependency

    private void writePom(String content) throws Exception {
        Files.write(workspace.resolve("pom.xml"), content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void addDependencyWithVersionAndScope() throws Exception {
        writePom("<project><dependencies></dependencies></project>");

        NetBeansFunctionExecutor.FunctionResult result = execute("add_dependency", Map.of(
                "groupId", "org.junit.jupiter", "artifactId", "junit-jupiter",
                "version", "5.10.0", "scope", "test"));

        assertTrue(result.isSuccess());
        assertEquals("updated", result.getData().get("status"));
        String pom = Files.readString(workspace.resolve("pom.xml"), StandardCharsets.UTF_8);
        assertTrue(pom.contains("<groupId>org.junit.jupiter</groupId>"));
        assertTrue(pom.contains("<version>5.10.0</version>"));
        assertTrue(pom.contains("<scope>test</scope>"));
    }

    @Test
    void addDependencyWithoutVersionOrScope() throws Exception {
        writePom("<project></project>");

        NetBeansFunctionExecutor.FunctionResult result = execute("add_dependency", Map.of(
                "groupId", "com.example", "artifactId", "lib"));

        assertTrue(result.isSuccess());
        String pom = Files.readString(workspace.resolve("pom.xml"), StandardCharsets.UTF_8);
        assertTrue(pom.contains("<dependencies>"));
        assertFalse(pom.contains("<version>"));
        assertFalse(pom.contains("<scope>"));
    }

    @Test
    void addDependencyRejectsDuplicate() throws Exception {
        writePom("<project><dependencies>" +
                "<dependency><groupId>com.example</groupId><artifactId>lib</artifactId></dependency>" +
                "</dependencies></project>");

        NetBeansFunctionExecutor.FunctionResult result = execute("add_dependency", Map.of(
                "groupId", "com.example", "artifactId", "lib"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Dependência já existe"));
    }

    @Test
    void addDependencyFailsWithoutPom() throws Exception {
        NetBeansFunctionExecutor.FunctionResult result = execute("add_dependency", Map.of(
                "groupId", "com.example", "artifactId", "lib"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("pom.xml não encontrado no workspace"));
    }
}
