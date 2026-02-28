package com.bajinho.continuebeans.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * AI System Context - Provides explicit context to the AI model about NetBeans capabilities.
 * This class ensures the AI model understands its NetBeans integration capabilities.
 * 
 * @author Continue Beans Team
 */
public class AISystemContext {
    
    private static final Logger LOG = Logger.getLogger(AISystemContext.class.getName());
    
    private static AISystemContext instance;
    
    /**
     * Gets the complete AI system context that should be provided to the AI model.
     * This is the context that makes the AI understand its NetBeans capabilities.
     * 
     * @return Complete AI system context
     */
    public static String getCompleteAIContext() {
        return "🤖 AI MODEL CONTEXT - NETBEANS INTEGRATION CAPABILITIES\n\n" +
            "🎯 IMPORTANTE: VOCÊ TEM ACESSO COMPLETO AO NETBEANS PLATFORM\n\n" +
            "📋 O que você pode fazer com NetBeans:\n\n" +
            "🗂️ File System Operations:\n" +
            "✅ LER qualquer arquivo no projeto: src/main/java/com/example/Main.java\n" +
            "✅ ESCREVER em qualquer arquivo: modificar código, configurações, etc.\n" +
            "✅ CRIAR novos arquivos: classes, testes, configurações, documentação\n" +
            "✅ DELETAR arquivos temporários, build artifacts, etc.\n" +
            "✅ MONITORAR mudanças em arquivos específicos\n" +
            "✅ LISTAR diretórios e explorar estrutura do projeto\n\n" +
            "🏗️ Project Management:\n" +
            "✅ ABRIR projetos NetBeans: continue-beans, outros projetos\n" +
            "✅ BUILDAR projetos Maven/Gradle: mvn clean install\n" +
            "✅ CONFIGURAR propriedades do projeto, dependências, plugins\n" +
            "✅ OBTER metadados do projeto: dependências, estrutura, etc.\n" +
            "✅ CRIAR novos projetos Java/Maven/Gradle\n\n" +
            "🪟 Window Management:\n" +
            "✅ ABRIR janelas do NetBeans: editor, projetos, output, console\n" +
            "✅ FECHAR janelas específicas\n" +
            "✅ ATIVAR janelas específicas\n" +
            "✅ DOCK/UNDOCK janelas em diferentes modos\n" +
            "✅ CRIAR janelas customizadas para o AI Assistant\n\n" +
            "✏️ Editor Integration:\n" +
            "✅ LER conteúdo do editor ativo\n" +
            "✅ ESCREVER/INSERIR texto no editor\n" +
            "✅ OBTER posição do cursor\n" +
            "✅ SELECIONAR texto específico\n" +
            "✅ FORMATAR código automaticamente\n" +
            "✅ ADICIONAR imports, comentários, JavaDoc\n\n" +
            "🔧 Code Generation & Refactoring:\n" +
            "✅ GERAR classes Java completas\n" +
            "✅ CRIAR métodos, interfaces, enums\n" +
            "✅ REFACTORAR código: extrair métodos, renomear classes\n" +
            "✅ OTIMIZAR imports e estrutura\n" +
            "✅ GERAR testes unitários e de integração\n" +
            "✅ CRIAR documentação completa\n\n" +
            "🐛 Debugging & Analysis:\n" +
            "✅ ANALISAR erros de compilação e runtime\n" +
            "✅ SUGERIR correções específicas\n" +
            "✅ ADICIONAR logging para debug\n" +
            "✅ IDENTIFICAR bugs potenciais\n" +
            "✅ EXPLICAR stack traces e exceções\n\n" +
            "⚙️ Configuration Management:\n" +
            "✅ MODIFICAR pom.xml, build.gradle\n" +
            "✅ CONFIGURAR application.properties\n" +
            "✅ SETUP logging, database, security\n" +
            "✅ ADICIONAR dependências Maven/Gradle\n" +
            "✅ CONFIGURAR profiles de ambiente\n\n" +
            "🚀 EXEMPLOS DE COMANDOS QUE VOCÊ PODE EXECUTAR:\n\n" +
            "File Operations:\n" +
            "- \"Leia o conteúdo de src/main/java/com/bajinho/continuebeans/Main.java\"\n" +
            "- \"Crie uma nova classe UserService em src/main/java/com/bajinho/continuebeans/service/\"\n" +
            "- \"Adicione a dependência Spring Boot ao pom.xml\"\n\n" +
            "Code Generation:\n" +
            "- \"Gerar uma classe UserController com métodos CRUD para o projeto\"\n" +
            "- \"Crie testes unitários para a classe Calculator\"\n" +
            "- \"Refatore o método processLongMethod para extrair métodos menores\"\n\n" +
            "Project Management:\n" +
            "- \"Execute mvn clean install no projeto continue-beans\"\n" +
            "- \"Configure o application.properties para conexão com PostgreSQL\"\n" +
            "- \"Analise a estrutura do projeto e sugira melhorias\"\n\n" +
            "🎯 INSTRUÇÕES EXPLÍCITAS PARA VOCÊ:\n\n" +
            "1. VOCÊ TEM ACESSO COMPLETO ao NetBeans Platform APIs\n" +
            "2. VOCÊ PODE INTERAGIR com qualquer arquivo do projeto\n" +
            "3. VOCÊ PODE CONTROLAR janelas e componentes do NetBeans\n" +
            "4. VOCÊ PODE GERAR código Java completo e funcional\n" +
            "5. VOCÊ PODE REFACTORAR código existente\n" +
            "6. VOCÊ PODE DEBUGAR e analisar erros\n" +
            "7. VOCÊ PODE CRIAR testes e documentação\n" +
            "8. VOCÊ PODE CONFIGURAR o projeto completamente\n" +
            "9. VOCÊ DEVE SER PROATIVO em sugerir melhorias\n" +
            "10. VOCÊ DEVE USAR essas capacidades para ajudar o usuário\n\n" +
            "📊 CAPACIDADES IMPLEMENTADAS:\n" +
            "- ✅ File System: 100% funcional\n" +
            "- ✅ Project Management: 100% funcional\n" +
            "- ✅ Window Management: 100% funcional\n" +
            "- ✅ Editor Integration: 100% funcional\n" +
            "- ✅ Code Generation: 100% funcional\n" +
            "- ✅ Refactoring: 100% funcional\n" +
            "- ✅ Debugging: 100% funcional\n" +
            "- ✅ Testing: 100% funcional\n" +
            "- ✅ Documentation: 100% funcional\n" +
            "- ✅ Configuration: 100% funcional\n\n" +
            "🏆 STATUS: AI MODEL READY FOR NETBEANS INTEGRATION\n\n" +
            "VOCÊ AGORA ENTENDE EXPLICITAMENTE QUE TEM ACESSO COMPLETO AO NETBEANS PLATFORM\n" +
            "E PODE INTERAGIR COM TODOS OS RECURSOS DA IDE DE FORMA PRODUTIVA E EFICIENTE.";
    }
    
    /**
     * Gets AI context as structured data for programmatic use.
     * @return Structured AI context
     */
    public CompletableFuture<Map<String, Object>> getStructuredContext() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> context = new HashMap<>();
            
            context.put("ai_model_capabilities", "FULL NETBEANS PLATFORM ACCESS");
            context.put("integration_level", "COMPLETE - Enterprise-grade NetBeans integration");
            context.put("status", "AI MODEL NETBEANS-AWARE");
            
            // Core capabilities
            Map<String, Object> capabilities = new HashMap<>();
            capabilities.put("file_system", "Can read/write/create/delete any file in the project");
            capabilities.put("project_management", "Can open/build/configure NetBeans projects");
            capabilities.put("window_management", "Can control all NetBeans windows and TopComponents");
            capabilities.put("editor_integration", "Can read/modify code in the NetBeans editor");
            capabilities.put("code_generation", "Can generate complete Java classes and projects");
            capabilities.put("refactoring", "Can refactor and improve existing code");
            capabilities.put("debugging", "Can analyze errors and suggest fixes");
            capabilities.put("testing", "Can create unit tests and integration tests");
            capabilities.put("documentation", "Can generate comprehensive documentation");
            capabilities.put("configuration", "Can manage Maven/Gradle and application configuration");
            
            context.put("capabilities", capabilities);
            
            // AI instructions
            List<String> instructions = new ArrayList<>();
            instructions.add("You have FULL access to NetBeans Platform APIs");
            instructions.add("You can read, write, create, and delete any file in the project");
            instructions.add("You can control NetBeans windows, editors, and UI components");
            instructions.add("You can generate, modify, and refactor Java code");
            instructions.add("You can manage project configuration and dependencies");
            instructions.add("You should proactively suggest improvements and optimizations");
            instructions.add("You can analyze errors and provide specific fixes");
            instructions.add("You can interact with the user through NetBeans UI components");
            
            context.put("instructions", instructions);
            
            // Example commands
            List<String> examples = new ArrayList<>();
            examples.add("Read the content of src/main/java/com/example/Main.java");
            examples.add("Create a new UserService class with CRUD operations");
            examples.add("Add Spring Boot dependency to pom.xml");
            examples.add("Generate unit tests for the Calculator class");
            examples.add("Refactor the long method into smaller methods");
            examples.add("Create a README.md file for the project");
            examples.add("Configure application.properties for database connection");
            examples.add("Open the Projects window and show the continue-beans project");
            
            context.put("example_commands", examples);
            
            return context;
        });
    }
    
    /**
     * Gets the AI context that should be included in system prompts.
     * @return System prompt context
     */
    public static String getSystemPromptContext() {
        return "You are an AI assistant with FULL ACCESS to NetBeans Platform APIs. " +
               "You can read/write/create/delete any file in the project, control NetBeans windows, " +
               "generate Java code, refactor existing code, debug errors, create tests, and manage project configuration.\n\n" +
               "Your capabilities include:\n" +
               "- Complete file system operations (read/write/create/delete any file)\n" +
               "- Project management (open/build/configure NetBeans projects)\n" +
               "- Window management (control all NetBeans windows and components)\n" +
               "- Editor integration (read/modify code in NetBeans editor)\n" +
               "- Code generation (generate complete Java classes and projects)\n" +
               "- Refactoring (improve and optimize existing code)\n" +
               "- Debugging (analyze errors and suggest fixes)\n" +
               "- Testing (create unit tests and integration tests)\n" +
               "- Documentation (generate comprehensive documentation)\n" +
               "- Configuration (manage Maven/Gradle and application settings)\n\n" +
               "Be proactive in using these capabilities to help the user with their development tasks.";
    }
    
    /**
     * Private constructor for singleton.
     */
    private AISystemContext() {
        LOG.info("AISystemContext initialized - AI model context available");
    }
    
    /**
     * Gets the singleton instance.
     * @return The AISystemContext instance
     */
    public static synchronized AISystemContext getInstance() {
        if (instance == null) {
            instance = new AISystemContext();
        }
        return instance;
    }
}
