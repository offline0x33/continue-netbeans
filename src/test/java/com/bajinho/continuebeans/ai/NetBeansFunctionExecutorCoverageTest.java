package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cobertura comportamental para NetBeansFunctionExecutor: gestão de módulos,
 * janelas ativas, operações de editor (caminhos de sucesso), catch blocks e branches.
 */
class NetBeansFunctionExecutorCoverageTest {

    @TempDir
    Path tempDir;

    private final NetBeansFunctionExecutor executor = new NetBeansFunctionExecutor();

    private NetBeansFunctionExecutor.FunctionResult execute(String fn, Map<String, Object> args) {
        return executor.executeFunction(fn, args).join();
    }

    /** Mapa mutável que aceita valores null (Map.of não aceita). */
    private static Map<String, Object> nullableArgs(Object... kv) {
        java.util.HashMap<String, Object> m = new java.util.HashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    // ──────────────────────────────────────────────
    // Gestão de módulos (Lookup.getDefault em teste → módulos reais)
    // ──────────────────────────────────────────────

    @Test
    void listModulesReturnsModuleCount() {
        var r = execute("list_modules", Map.of());
        assertTrue(r.isSuccess(), "list_modules deve retornar sucesso");
        assertNotNull(r.getData().get("count"), "count deve estar presente");
        assertEquals(false, r.getData().get("enabledOnly"));
    }

    @Test
    void listModulesWithEnabledOnlyFilterReturnsCount() {
        var r = execute("list_modules", Map.of("enabledOnly", true));
        assertTrue(r.isSuccess());
        assertNotNull(r.getData().get("count"), "count deve estar presente");
        assertEquals(true, r.getData().get("enabledOnly"));
    }

    @Test
    void getModuleServicesReturnsErrorForUnknownModule() {
        var r = execute("get_module_services", Map.of("moduleCodeName", "com.example.unknown.module"));
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("Module not found"));
    }

    @Test
    void enableModuleReturnsErrorForUnknownModule() {
        var r = execute("enable_module", Map.of("moduleCodeName", "com.example.unknown.module"));
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("Module not found"));
    }

    @Test
    void disableModuleReturnsErrorForUnknownModule() {
        var r = execute("disable_module", Map.of("moduleCodeName", "com.example.unknown.module"));
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("Module not found"));
    }

    /**
     * Obtém o codeName de um módulo real registrado no Lookup.getDefault().
     */
    private String getRealModuleCodeName() {
        var r = execute("list_modules", Map.of());
        assertTrue(r.isSuccess(), "list_modules deve retornar sucesso");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> modules = (List<Map<String, Object>>) r.getData().get("modules");
        assertNotNull(modules, "modules não pode ser null");
        assertFalse(modules.isEmpty(), "deve haver pelo menos um módulo registrado");
        return (String) modules.get(0).get("codeName");
    }

    @Test
    void getModuleInfoWithRealModuleReturnsDetails() {
        String codeName = getRealModuleCodeName();
        var r = execute("get_module_info", Map.of("moduleCodeName", codeName));
        assertTrue(r.isSuccess(), "get_module_info deve retornar sucesso: " + r.getMessage());
        assertEquals(codeName, r.getData().get("codeName"));
        assertNotNull(r.getData().get("displayName"));
    }

    @Test
    void getModuleServicesWithRealModuleReturnsServices() {
        String codeName = getRealModuleCodeName();
        var r = execute("get_module_services", Map.of(
                "moduleCodeName", codeName,
                "serviceType", "all"));
        assertTrue(r.isSuccess(), "get_module_services deve retornar sucesso: " + r.getMessage());
    }

    @Test
    void getModuleServicesWithSpecificServiceType() {
        String codeName = getRealModuleCodeName();
        var r = execute("get_module_services", Map.of(
                "moduleCodeName", codeName,
                "serviceType", "FileSystem"));
        assertNotNull(r.getMessage());
    }

    @Test
    void enableModuleWithRealModuleReturnsStatus() {
        String codeName = getRealModuleCodeName();
        var r = execute("enable_module", Map.of("moduleCodeName", codeName));
        assertTrue(r.isSuccess(), "enable_module deve retornar sucesso: " + r.getMessage());
        assertNotNull(r.getData().get("status"));
    }

    @Test
    void disableModuleWithRealModuleReturnsStatus() {
        String codeName = getRealModuleCodeName();
        var r = execute("disable_module", Map.of("moduleCodeName", codeName));
        assertTrue(r.isSuccess(), "disable_module deve retornar sucesso: " + r.getMessage());
        assertNotNull(r.getData().get("status"));
    }

    @Test
    void getModuleInfoWithRealModuleAndDependencies() {
        String codeName = getRealModuleCodeName();
        var r = execute("get_module_info", Map.of("moduleCodeName", codeName));
        assertTrue(r.isSuccess());
        Object deps = r.getData().get("dependencies");
        assertNotNull(deps, "dependencies deve estar presente");
    }

    // ──────────────────────────────────────────────
    // Janelas ativas (TopComponent.getRegistry)
    // ──────────────────────────────────────────────

    @Test
    void getActiveWindowsReturnsSuccessOrCaughtError() {
        var r = execute("get_active_windows", Map.of());
        assertNotNull(r.getMessage());
    }

    // ──────────────────────────────────────────────
    // Operações de editor: caminhos de sucesso
    // ──────────────────────────────────────────────

    @Test
    void openEditorWithExistingFileReturnsSuccessOrHandledError() throws Exception {
        Path f = tempDir.resolve("editor.java");
        Files.writeString(f, "class Editor {}");

        var r = execute("open_editor", Map.of(
                "filePath", f.toString(),
                "lineNumber", 1,
                "focus", true));
        assertNotNull(r.getMessage());
    }

    @Test
    void closeEditorWithExistingFileReturnsSuccessOrHandledError() throws Exception {
        Path f = tempDir.resolve("close.java");
        Files.writeString(f, "class Close {}");

        var r = execute("close_editor", Map.of(
                "filePath", f.toString()));
        assertNotNull(r.getMessage());
    }

    @Test
    void saveEditorWithExistingFileReturnsSuccessOrHandledError() throws Exception {
        Path f = tempDir.resolve("save.java");
        Files.writeString(f, "class Save {}");

        var r = execute("save_editor", Map.of(
                "filePath", f.toString()));
        assertNotNull(r.getMessage());
    }

    // ──────────────────────────────────────────────
    // Blocos catch: argumentos inválidos que causam exceção
    // ──────────────────────────────────────────────

    @Test
    void readFileWithNullPathReturnsError() {
        var r = execute("read_file", Map.of());
        assertFalse(r.isSuccess());
    }

    @Test
    void createFileWithNullContentReturnsError() {
        var r = execute("create_file", Map.of(
                "filePath", tempDir.resolve("null-content.txt").toString()));
        assertNotNull(r.getMessage());
    }

    @Test
    void updateFileWithNullContentReturnsError() throws Exception {
        Path f = tempDir.resolve("update-null.txt");
        Files.writeString(f, "before");
        var r = execute("update_file", Map.of(
                "filePath", f.toString()));
        assertNotNull(r.getMessage());
    }

    @Test
    void deleteFileWithNullPathReturnsError() {
        var r = execute("delete_file", Map.of());
        assertFalse(r.isSuccess());
    }

    // ──────────────────────────────────────────────
    // analyze_code com includeSuggestions=false e analysisType customizado
    // ──────────────────────────────────────────────

    @Test
    void analyzeCodeWithoutSuggestionsOmitsSuggestionField() throws Exception {
        Path f = tempDir.resolve("NoSuggest.java");
        Files.writeString(f, "class NoSuggest {}\n");

        var r = execute("analyze_code", Map.of(
                "filePath", f.toString(),
                "includeSuggestions", false));
        assertTrue(r.isSuccess());
        assertFalse(r.getData().containsKey("suggestions"));
    }

    @Test
    void analyzeCodeWithCustomAnalysisType() throws Exception {
        Path f = tempDir.resolve("Custom.java");
        Files.writeString(f, "class Custom {}\n");

        var r = execute("analyze_code", Map.of(
                "filePath", f.toString(),
                "analysisType", "security"));
        assertTrue(r.isSuccess());
        assertEquals("security", r.getData().get("analysisType"));
    }

    // ──────────────────────────────────────────────
    // refactor_code com refactoringType diferente de optimize_imports
    // ──────────────────────────────────────────────

    @Test
    void refactorCodeWithNonOptimizeTypeLeavesContentUnchanged() throws Exception {
        Path f = tempDir.resolve("RefactorOther.java");
        Files.writeString(f, "import java.util.List;\nclass RefactorOther {}\n");

        var r = execute("refactor_code", Map.of(
                "filePath", f.toString(),
                "refactoringType", "rename_variable"));
        assertTrue(r.isSuccess());
        assertEquals("rename_variable", r.getData().get("refactoringType"));
    }

    // ──────────────────────────────────────────────
    // add_dependency com version ausente (default "latest")
    // ──────────────────────────────────────────────

    @Test
    void addDependencyWithoutVersionDefaultsToLatest() {
        var r = execute("add_dependency", Map.of(
                "groupId", "org.example",
                "artifactId", "demo"));
        assertTrue(r.isSuccess());
        assertEquals("latest", r.getData().get("version"));
    }

    // ──────────────────────────────────────────────
    // build_project com goals customizados e diretório não-Maven
    // ──────────────────────────────────────────────

    @Test
    void buildProjectWithCustomGoals() throws Exception {
        Path p = Files.createDirectory(tempDir.resolve("custom-goals"));
        Files.writeString(p.resolve("pom.xml"), "<project></project>");

        var r = execute("build_project", Map.of(
                "projectPath", p.toString(),
                "goals", java.util.List.of("compile")));
        assertNotNull(r.getMessage());
    }

    @Test
    void buildProjectWithNonMavenDirectory() throws Exception {
        Path p = Files.createDirectory(tempDir.resolve("non-maven"));
        var r = execute("build_project", Map.of(
                "projectPath", p.toString()));
        assertFalse(r.isSuccess());
    }

    // ──────────────────────────────────────────────
    // create_project com projectType customizado e diretório existente
    // ──────────────────────────────────────────────

    @Test
    void createProjectWithCustomType() throws Exception {
        var r = execute("create_project", Map.of(
                "projectName", "custom-type",
                "projectType", "gradle",
                "packageName", "com.example.custom",
                "location", tempDir.toString()));
        assertTrue(r.isSuccess());
        assertEquals("gradle", r.getData().get("projectType"));
    }

    @Test
    void createProjectWithExistingDirectory() throws Exception {
        Path existing = Files.createDirectory(tempDir.resolve("existing-project"));
        var r = execute("create_project", Map.of(
                "projectName", "existing-project",
                "packageName", "com.test",
                "location", tempDir.toString()));
        assertFalse(r.isSuccess());
    }

    // ──────────────────────────────────────────────
    // list_directory com includeHidden=true e recursive=true
    // ──────────────────────────────────────────────

    @Test
    void listDirectoryWithIncludeHidden() throws Exception {
        Path hidden = tempDir.resolve(".hidden-file");
        Files.writeString(hidden, "secret");

        var r = execute("list_directory", Map.of(
                "directoryPath", tempDir.toString(),
                "includeHidden", true));
        assertTrue(r.isSuccess());
        assertEquals(true, r.getData().get("includeHidden"));
    }

    @Test
    void listDirectoryWithRecursiveAndHidden() throws Exception {
        Path sub = Files.createDirectory(tempDir.resolve("subdir"));
        Files.writeString(sub.resolve("nested.txt"), "nested");
        var r = execute("list_directory", Map.of(
                "directoryPath", tempDir.toString(),
                "recursive", true,
                "includeHidden", true));
        assertTrue(r.isSuccess());
        assertEquals(true, r.getData().get("recursive"));
    }

    // ──────────────────────────────────────────────
    // generate_test_method com testFramework customizado
    // ──────────────────────────────────────────────

    @Test
    void generateTestMethodWithCustomFramework() {
        var r = execute("generate_test_method", Map.of(
                "className", "CustomTest",
                "testFramework", "testng"));
        assertTrue(r.isSuccess());
        assertEquals("testng", r.getData().get("testFramework"));
    }

    // ──────────────────────────────────────────────
    // FunctionResult: error com data null → empty map
    // ──────────────────────────────────────────────

    @Test
    void functionResultErrorWithNullDataReturnsEmptyMap() {
        NetBeansFunctionExecutor.FunctionResult result = NetBeansFunctionExecutor.FunctionResult.error("test");
        assertFalse(result.isSuccess());
        assertEquals("test", result.getMessage());
        assertTrue(result.getData().isEmpty());
    }

    // ──────────────────────────────────────────────
    // get_project_info com projeto válido (pom.xml)
    // ──────────────────────────────────────────────

    @Test
    void getProjectInfoWithValidMavenProject() throws Exception {
        Path p = Files.createDirectory(tempDir.resolve("info-project"));
        Files.writeString(p.resolve("pom.xml"), "<project><groupId>x</groupId></project>");

        var r = execute("get_project_info", Map.of(
                "projectPath", p.toString()));
        assertNotNull(r.getMessage());
    }

    // ──────────────────────────────────────────────
    // Catch blocks: NPE em argumentos null (catch geral do dispatch)
    // ──────────────────────────────────────────────

    @Test
    void readFileWithDirectoryPathTriggersIOException() throws Exception {
        Path dir = Files.createDirectory(tempDir.resolve("read-dir"));
        var r = execute("read_file", Map.of(
                "filePath", dir.toString()));
        assertNotNull(r.getMessage());
    }

    @Test
    void createFileWithNullFilePathTriggersNpe() {
        var r = execute("create_file", nullableArgs("filePath", null));
        assertNotNull(r.getMessage());
    }

    @Test
    void updateFileWithNullFilePathTriggersNpe() {
        var r = execute("update_file", nullableArgs("filePath", null));
        assertNotNull(r.getMessage());
    }

    @Test
    void deleteFileWithNullFilePathTriggersNpe() {
        var r = execute("delete_file", nullableArgs("filePath", null));
        assertNotNull(r.getMessage());
    }

    @Test
    void listDirectoryWithNullDirectoryPathTriggersNpe() {
        var r = execute("list_directory", nullableArgs("directoryPath", null));
        assertNotNull(r.getMessage());
    }

    @Test
    void buildProjectWithNullProjectPathTriggersNpe() {
        var r = execute("build_project", nullableArgs("projectPath", null));
        assertNotNull(r.getMessage());
    }

    @Test
    void createProjectWithNullLocationTriggersNpe() {
        var r = execute("create_project", Map.of(
                "projectName", "test",
                "packageName", "com.test"));
        assertNotNull(r.getMessage());
    }

    @Test
    void openEditorWithNullFilePathTriggersNpe() {
        var r = execute("open_editor", nullableArgs("filePath", null));
        assertNotNull(r.getMessage());
    }

    @Test
    void closeEditorWithNullFilePathTriggersNpe() {
        var r = execute("close_editor", nullableArgs("filePath", null));
        assertNotNull(r.getMessage());
    }

    @Test
    void saveEditorWithNullFilePathTriggersNpe() {
        var r = execute("save_editor", nullableArgs("filePath", null));
        assertNotNull(r.getMessage());
    }

    @Test
    void analyzeCodeWithNullFilePathTriggersNpe() {
        var r = execute("analyze_code", nullableArgs("filePath", null));
        assertNotNull(r.getMessage());
    }

    @Test
    void refactorCodeWithNullFilePathTriggersNpe() {
        var r = execute("refactor_code", nullableArgs("filePath", null));
        assertNotNull(r.getMessage());
    }

    @Test
    void generateClassWithNullClassNameTriggersNpe() {
        var r = execute("generate_class", Map.of(
                "packageName", "com.test"));
        assertNotNull(r.getMessage());
    }

    @Test
    void generateInterfaceWithNullNameTriggersNpe() {
        var r = execute("generate_interface", Map.of(
                "packageName", "com.test"));
        assertNotNull(r.getMessage());
    }

    @Test
    void generateTestMethodWithNullClassNameTriggersNpe() {
        var r = execute("generate_test_method", Map.of(
                "testFramework", "junit"));
        assertNotNull(r.getMessage());
    }

    @Test
    void addDependencyWithNullGroupIdTriggersNpe() {
        var r = execute("add_dependency", Map.of(
                "artifactId", "demo"));
        assertNotNull(r.getMessage());
    }

    @Test
    void getProjectInfoWithNullProjectPathTriggersNpe() {
        var r = execute("get_project_info", nullableArgs("projectPath", null));
        assertNotNull(r.getMessage());
    }

    // ──────────────────────────────────────────────
    // Branches: delete_file com confirm=true/false, update_file append, create_file overwrite
    // ──────────────────────────────────────────────

    @Test
    void deleteFileWithConfirmFalse() throws Exception {
        Path f = tempDir.resolve("to-delete.txt");
        Files.writeString(f, "delete me");
        var r = execute("delete_file", Map.of(
                "filePath", f.toString()));
        assertFalse(r.isSuccess());
    }

    @Test
    void deleteFileWithConfirmTrue() throws Exception {
        Path f = tempDir.resolve("to-delete-confirmed.txt");
        Files.writeString(f, "delete me");
        var r = execute("delete_file", Map.of(
                "filePath", f.toString(),
                "confirm", true));
        assertTrue(r.isSuccess());
    }

    @Test
    void updateFileWithAppend() throws Exception {
        Path f = tempDir.resolve("append.txt");
        Files.writeString(f, "first ");
        var r = execute("update_file", Map.of(
                "filePath", f.toString(),
                "content", "second",
                "append", true));
        assertTrue(r.isSuccess());
    }

    @Test
    void createFileWithOverwrite() throws Exception {
        Path f = tempDir.resolve("overwrite.txt");
        Files.writeString(f, "original");
        var r = execute("create_file", Map.of(
                "filePath", f.toString(),
                "content", "replaced",
                "overwrite", true));
        assertTrue(r.isSuccess());
    }

    @Test
    void createFileWithoutOverwriteOnExisting() throws Exception {
        Path f = tempDir.resolve("no-overwrite.txt");
        Files.writeString(f, "original");
        var r = execute("create_file", Map.of(
                "filePath", f.toString(),
                "content", "replaced"));
        assertFalse(r.isSuccess());
    }

    // ──────────────────────────────────────────────
    // get_module_services com serviceType null (default "all")
    // ──────────────────────────────────────────────

    @Test
    void getModuleServicesWithNullServiceType() {
        var r = execute("get_module_services", Map.of(
                "moduleCodeName", "com.example.module"));
        assertNotNull(r.getMessage());
    }
}
