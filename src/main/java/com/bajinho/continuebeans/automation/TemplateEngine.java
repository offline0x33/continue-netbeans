package com.bajinho.continuebeans.automation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Advanced template engine with variable substitution, conditional logic,
 * loops, functions, and template inheritance.
 * 
 * @author Continue Beans Team
 */
public class TemplateEngine {
    
    private static final Logger LOG = Logger.getLogger(TemplateEngine.class.getName());
    
    private static TemplateEngine instance;
    
    private final Map<String, TemplateFunction> functions;
    private final Map<String, Template> cachedTemplates;
    private final TemplateConfiguration config;
    
    /**
     * Template configuration.
     */
    public static class TemplateConfiguration {
        private final String variablePrefix;
        private final String variableSuffix;
        private final String conditionalPrefix;
        private final String conditionalSuffix;
        private final String loopPrefix;
        private final String loopSuffix;
        private final String functionPrefix;
        private final String functionSuffix;
        private final boolean enableCaching;
        private final int maxCacheSize;
        
        public TemplateConfiguration(String variablePrefix, String variableSuffix,
                                   String conditionalPrefix, String conditionalSuffix,
                                   String loopPrefix, String loopSuffix,
                                   String functionPrefix, String functionSuffix,
                                   boolean enableCaching, int maxCacheSize) {
            this.variablePrefix = variablePrefix;
            this.variableSuffix = variableSuffix;
            this.conditionalPrefix = conditionalPrefix;
            this.conditionalSuffix = conditionalSuffix;
            this.loopPrefix = loopPrefix;
            this.loopSuffix = loopSuffix;
            this.functionPrefix = functionPrefix;
            this.functionSuffix = functionSuffix;
            this.enableCaching = enableCaching;
            this.maxCacheSize = maxCacheSize;
        }
        
        /**
         * Creates default configuration.
         * @return Default TemplateConfiguration
         */
        public static TemplateConfiguration getDefault() {
            return new TemplateConfiguration(
                "${", "}",  // variables: ${variable}
                "#{", "}",  // conditionals: #{if condition}...#{endif}
                "@{", "}",  // loops: @{for item in items}...@{endfor}
                "${", "}",  // functions: ${function(args)}
                true,       // enable caching
                100         // max cache size
            );
        }
        
        // Getters
        public String getVariablePrefix() { return variablePrefix; }
        public String getVariableSuffix() { return variableSuffix; }
        public String getConditionalPrefix() { return conditionalPrefix; }
        public String getConditionalSuffix() { return conditionalSuffix; }
        public String getLoopPrefix() { return loopPrefix; }
        public String getLoopSuffix() { return loopSuffix; }
        public String getFunctionPrefix() { return functionPrefix; }
        public String getFunctionSuffix() { return functionSuffix; }
        public boolean isEnableCaching() { return enableCaching; }
        public int getMaxCacheSize() { return maxCacheSize; }
    }
    
    /**
     * Represents a compiled template.
     */
    public static class Template {
        private final String templateId;
        private final String originalContent;
        private final List<TemplateElement> elements;
        private final long compileTime;
        
        public Template(String templateId, String originalContent, List<TemplateElement> elements) {
            this.templateId = templateId;
            this.originalContent = originalContent;
            this.elements = elements;
            this.compileTime = System.currentTimeMillis();
        }
        
        /**
         * Processes the template with given context.
         * @param context The template context
         * @return Processed content
         */
        public String process(Map<String, Object> context) {
            StringBuilder result = new StringBuilder();
            
            for (TemplateElement element : elements) {
                result.append(element.process(context));
            }
            
            return result.toString();
        }
        
        // Getters
        public String getTemplateId() { return templateId; }
        public String getOriginalContent() { return originalContent; }
        public List<TemplateElement> getElements() { return elements; }
        public long getCompileTime() { return compileTime; }
    }
    
    /**
     * Template element interface.
     */
    public interface TemplateElement {
        String process(Map<String, Object> context);
    }
    
    /**
     * Static text element.
     */
    public static class StaticTextElement implements TemplateElement {
        private final String text;
        
        public StaticTextElement(String text) {
            this.text = text;
        }
        
        @Override
        public String process(Map<String, Object> context) {
            return text;
        }
    }
    
    /**
     * Variable element.
     */
    public static class VariableElement implements TemplateElement {
        private final String variableName;
        private final String defaultValue;
        
        public VariableElement(String variableName, String defaultValue) {
            this.variableName = variableName;
            this.defaultValue = defaultValue;
        }
        
        @Override
        public String process(Map<String, Object> context) {
            Object value = context.get(variableName);
            if (value != null) {
                return String.valueOf(value);
            }
            return defaultValue != null ? defaultValue : "";
        }
    }
    
    /**
     * Conditional element.
     */
    public static class ConditionalElement implements TemplateElement {
        private final String condition;
        private final List<TemplateElement> thenElements;
        private final List<TemplateElement> elseElements;
        
        public ConditionalElement(String condition, List<TemplateElement> thenElements, 
                                List<TemplateElement> elseElements) {
            this.condition = condition;
            this.thenElements = thenElements != null ? thenElements : new ArrayList<>();
            this.elseElements = elseElements != null ? elseElements : new ArrayList<>();
        }
        
        @Override
        public String process(Map<String, Object> context) {
            boolean conditionResult = evaluateCondition(condition, context);
            
            List<TemplateElement> elements = conditionResult ? thenElements : elseElements;
            StringBuilder result = new StringBuilder();
            
            for (TemplateElement element : elements) {
                result.append(element.process(context));
            }
            
            return result.toString();
        }
        
        /**
         * Evaluates a condition.
         * @param condition The condition string
         * @param context The template context
         * @return True if condition is true
         */
        private boolean evaluateCondition(String condition, Map<String, Object> context) {
            // Simple condition evaluation
            // TODO: Implement more sophisticated condition parsing
            
            // Check for existence
            if (condition.startsWith("exists ")) {
                String varName = condition.substring(7).trim();
                return context.containsKey(varName);
            }
            
            // Check for equality
            if (condition.contains("==")) {
                String[] parts = condition.split("==");
                if (parts.length == 2) {
                    String left = parts[0].trim();
                    String right = parts[1].trim();
                    
                    Object leftValue = resolveValue(left, context);
                    Object rightValue = resolveValue(right, context);
                    
                    return leftValue != null && leftValue.equals(rightValue);
                }
            }
            
            // Check for inequality
            if (condition.contains("!=")) {
                String[] parts = condition.split("!=");
                if (parts.length == 2) {
                    String left = parts[0].trim();
                    String right = parts[1].trim();
                    
                    Object leftValue = resolveValue(left, context);
                    Object rightValue = resolveValue(right, context);
                    
                    return !leftValue.equals(rightValue);
                }
            }
            
            // Default: check if variable exists and is truthy
            Object value = context.get(condition);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            return value != null && !value.toString().isEmpty();
        }
        
        /**
         * Resolves a value from context.
         * @param expression The expression
         * @param context The context
         * @return The resolved value
         */
        private Object resolveValue(String expression, Map<String, Object> context) {
            expression = expression.trim();
            
            // Remove quotes if present
            if (expression.startsWith("\"") && expression.endsWith("\"")) {
                return expression.substring(1, expression.length() - 1);
            }
            
            if (expression.startsWith("'") && expression.endsWith("'")) {
                return expression.substring(1, expression.length() - 1);
            }
            
            // Get from context
            return context.get(expression);
        }
    }
    
    /**
     * Loop element.
     */
    public static class LoopElement implements TemplateElement {
        private final String variable;
        private final String collection;
        private final List<TemplateElement> elements;
        
        public LoopElement(String variable, String collection, List<TemplateElement> elements) {
            this.variable = variable;
            this.collection = collection;
            this.elements = elements != null ? elements : new ArrayList<>();
        }
        
        @Override
        public String process(Map<String, Object> context) {
            Object collectionValue = context.get(collection);
            if (collectionValue == null) {
                return "";
            }
            
            StringBuilder result = new StringBuilder();
            
            if (collectionValue instanceof List) {
                List<?> list = (List<?>) collectionValue;
                for (Object item : list) {
                    Map<String, Object> loopContext = new HashMap<>(context);
                    loopContext.put(variable, item);
                    
                    for (TemplateElement element : elements) {
                        result.append(element.process(loopContext));
                    }
                }
            } else if (collectionValue.getClass().isArray()) {
                Object[] array = (Object[]) collectionValue;
                for (Object item : array) {
                    Map<String, Object> loopContext = new HashMap<>(context);
                    loopContext.put(variable, item);
                    
                    for (TemplateElement element : elements) {
                        result.append(element.process(loopContext));
                    }
                }
            }
            
            return result.toString();
        }
    }
    
    /**
     * Function element.
     */
    public static class FunctionElement implements TemplateElement {
        private final String functionName;
        private final List<String> arguments;
        private final TemplateEngine engine;
        
        public FunctionElement(String functionName, List<String> arguments, TemplateEngine engine) {
            this.functionName = functionName;
            this.arguments = arguments != null ? arguments : new ArrayList<>();
            this.engine = engine;
        }
        
        @Override
        public String process(Map<String, Object> context) {
            TemplateFunction function = engine.getFunction(functionName);
            if (function == null) {
                LOG.warning("Function not found: " + functionName);
                return "";
            }
            
            // Resolve arguments
            List<Object> resolvedArgs = new ArrayList<>();
            for (String arg : arguments) {
                Object value = resolveArgument(arg, context);
                resolvedArgs.add(value);
            }
            
            return function.execute(resolvedArgs, context);
        }
        
        /**
         * Resolves an argument value.
         * @param arg The argument string
         * @param context The template context
         * @return The resolved value
         */
        private Object resolveArgument(String arg, Map<String, Object> context) {
            arg = arg.trim();
            
            // String literals
            if (arg.startsWith("\"") && arg.endsWith("\"")) {
                return arg.substring(1, arg.length() - 1);
            }
            
            if (arg.startsWith("'") && arg.endsWith("'")) {
                return arg.substring(1, arg.length() - 1);
            }
            
            // Numbers
            try {
                if (arg.contains(".")) {
                    return Double.parseDouble(arg);
                } else {
                    return Integer.parseInt(arg);
                }
            } catch (NumberFormatException e) {
                // Not a number, treat as variable
            }
            
            // Variables
            return context.get(arg);
        }
    }
    
    /**
     * Template function interface.
     */
    public interface TemplateFunction {
        String execute(List<Object> arguments, Map<String, Object> context);
        String getDescription();
    }
    
    /**
     * Private constructor for singleton.
     */
    private TemplateEngine() {
        this.functions = new ConcurrentHashMap<>();
        this.cachedTemplates = new ConcurrentHashMap<>();
        this.config = TemplateConfiguration.getDefault();
        
        initializeDefaultFunctions();
        
        LOG.info("TemplateEngine initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The TemplateEngine instance
     */
    public static synchronized TemplateEngine getInstance() {
        if (instance == null) {
            instance = new TemplateEngine();
        }
        return instance;
    }
    
    /**
     * Initializes default template functions.
     */
    private void initializeDefaultFunctions() {
        // Upper case function
        functions.put("upper", new TemplateFunction() {
            @Override
            public String execute(List<Object> arguments, Map<String, Object> context) {
                if (arguments.isEmpty()) return "";
                return arguments.get(0).toString().toUpperCase();
            }
            
            @Override
            public String getDescription() {
                return "Converts string to uppercase";
            }
        });
        
        // Lower case function
        functions.put("lower", new TemplateFunction() {
            @Override
            public String execute(List<Object> arguments, Map<String, Object> context) {
                if (arguments.isEmpty()) return "";
                return arguments.get(0).toString().toLowerCase();
            }
            
            @Override
            public String getDescription() {
                return "Converts string to lowercase";
            }
        });
        
        // Capitalize function
        functions.put("capitalize", new TemplateFunction() {
            @Override
            public String execute(List<Object> arguments, Map<String, Object> context) {
                if (arguments.isEmpty()) return "";
                String str = arguments.get(0).toString();
                if (str.isEmpty()) return "";
                return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
            }
            
            @Override
            public String getDescription() {
                return "Capitalizes the first letter of a string";
            }
        });
        
        // Length function
        functions.put("length", new TemplateFunction() {
            @Override
            public String execute(List<Object> arguments, Map<String, Object> context) {
                if (arguments.isEmpty()) return "0";
                Object arg = arguments.get(0);
                if (arg instanceof String) {
                    return String.valueOf(((String) arg).length());
                } else if (arg instanceof List) {
                    return String.valueOf(((List<?>) arg).size());
                } else if (arg.getClass().isArray()) {
                    return String.valueOf(((Object[]) arg).length);
                }
                return "1";
            }
            
            @Override
            public String getDescription() {
                return "Returns the length of a string or collection";
            }
        });
        
        // Format date function
        functions.put("formatDate", new TemplateFunction() {
            @Override
            public String execute(List<Object> arguments, Map<String, Object> context) {
                if (arguments.isEmpty()) return "";
                // TODO: Implement date formatting
                return arguments.get(0).toString();
            }
            
            @Override
            public String getDescription() {
                return "Formats a date value";
            }
        });
        
        // Default value function
        functions.put("default", new TemplateFunction() {
            @Override
            public String execute(List<Object> arguments, Map<String, Object> context) {
                if (arguments.isEmpty()) return "";
                Object value = arguments.get(0);
                if (value == null || value.toString().isEmpty()) {
                    return arguments.size() > 1 ? arguments.get(1).toString() : "";
                }
                return value.toString();
            }
            
            @Override
            public String getDescription() {
                return "Returns default value if input is null or empty";
            }
        });
    }
    
    /**
     * Compiles a template.
     * @param templateId The template ID
     * @param content The template content
     * @return Compiled template
     */
    public Template compileTemplate(String templateId, String content) {
        // Check cache first
        if (config.isEnableCaching() && cachedTemplates.containsKey(templateId)) {
            return cachedTemplates.get(templateId);
        }
        
        List<TemplateElement> elements = parseTemplate(content);
        Template template = new Template(templateId, content, elements);
        
        // Cache if enabled
        if (config.isEnableCaching()) {
            if (cachedTemplates.size() >= config.getMaxCacheSize()) {
                // Remove oldest entry (simple LRU)
                String oldestKey = cachedTemplates.keySet().iterator().next();
                cachedTemplates.remove(oldestKey);
            }
            cachedTemplates.put(templateId, template);
        }
        
        return template;
    }
    
    /**
     * Parses template content into elements.
     * @param content The template content
     * @return List of template elements
     */
    private List<TemplateElement> parseTemplate(String content) {
        List<TemplateElement> elements = new ArrayList<>();
        int pos = 0;
        
        while (pos < content.length()) {
            // Find next template element
            int nextVar = content.indexOf(config.getVariablePrefix(), pos);
            int nextCond = content.indexOf(config.getConditionalPrefix(), pos);
            int nextLoop = content.indexOf(config.getLoopPrefix(), pos);
            int nextFunc = content.indexOf(config.getFunctionPrefix(), pos);
            
            // Find the earliest occurrence
            int nextElement = -1;
            String elementType = null;
            
            if (nextVar != -1 && (nextElement == -1 || nextVar < nextElement)) {
                nextElement = nextVar;
                elementType = "variable";
            }
            if (nextCond != -1 && (nextElement == -1 || nextCond < nextElement)) {
                nextElement = nextCond;
                elementType = "conditional";
            }
            if (nextLoop != -1 && (nextElement == -1 || nextLoop < nextElement)) {
                nextElement = nextLoop;
                elementType = "loop";
            }
            if (nextFunc != -1 && (nextElement == -1 || nextFunc < nextElement)) {
                nextElement = nextFunc;
                elementType = "function";
            }
            
            if (nextElement == -1) {
                // No more template elements, add remaining text
                elements.add(new StaticTextElement(content.substring(pos)));
                break;
            }
            
            // Add static text before element
            if (nextElement > pos) {
                elements.add(new StaticTextElement(content.substring(pos, nextElement)));
            }
            
            // Parse template element
            switch (elementType) {
                case "variable":
                    parseVariable(content, nextElement, elements);
                    pos = findEndOfVariable(content, nextElement);
                    break;
                case "conditional":
                    parseConditional(content, nextElement, elements);
                    pos = findEndOfConditional(content, nextElement);
                    break;
                case "loop":
                    parseLoop(content, nextElement, elements);
                    pos = findEndOfLoop(content, nextElement);
                    break;
                case "function":
                    parseFunction(content, nextElement, elements);
                    pos = findEndOfFunction(content, nextElement);
                    break;
            }
        }
        
        return elements;
    }
    
    /**
     * Parses a variable element.
     */
    private void parseVariable(String content, int startPos, List<TemplateElement> elements) {
        int endPos = content.indexOf(config.getVariableSuffix(), startPos);
        if (endPos == -1) {
            elements.add(new StaticTextElement(content.substring(startPos)));
            return;
        }
        
        String variableContent = content.substring(
            startPos + config.getVariablePrefix().length(), endPos);
        
        // Check for default value
        String defaultValue = "";
        if (variableContent.contains(":")) {
            String[] parts = variableContent.split(":", 2);
            variableContent = parts[0].trim();
            defaultValue = parts[1].trim();
        }
        
        elements.add(new VariableElement(variableContent.trim(), defaultValue));
    }
    
    /**
     * Parses a conditional element.
     */
    private void parseConditional(String content, int startPos, List<TemplateElement> elements) {
        // Find endif
        int endifPos = findMatchingEnd(content, startPos, 
            config.getConditionalPrefix(), config.getConditionalSuffix(),
            "#{endif}", "#{else}");
        
        if (endifPos == -1) {
            elements.add(new StaticTextElement(content.substring(startPos)));
            return;
        }
        
        // Extract condition
        int conditionEnd = content.indexOf(config.getConditionalSuffix(), startPos);
        if (conditionEnd == -1) {
            elements.add(new StaticTextElement(content.substring(startPos)));
            return;
        }
        
        String condition = content.substring(
            startPos + config.getConditionalPrefix().length(), conditionEnd).trim();
        
        // Parse then block
        String thenContent = content.substring(conditionEnd + config.getConditionalSuffix().length(), endifPos);
        List<TemplateElement> thenElements = parseTemplate(thenContent);
        
        // Parse else block if present
        List<TemplateElement> elseElements = new ArrayList<>();
        int elsePos = content.lastIndexOf("#{else}", endifPos);
        if (elsePos > startPos && elsePos < endifPos) {
            String elseContent = content.substring(elsePos + 6, endifPos);
            elseElements = parseTemplate(elseContent);
            // Remove else part from then content
            thenContent = content.substring(conditionEnd + config.getConditionalSuffix().length(), elsePos);
            thenElements = parseTemplate(thenContent);
        }
        
        elements.add(new ConditionalElement(condition, thenElements, elseElements));
    }
    
    /**
     * Parses a loop element.
     */
    private void parseLoop(String content, int startPos, List<TemplateElement> elements) {
        // Find endfor
        int endforPos = findMatchingEnd(content, startPos,
            config.getLoopPrefix(), config.getLoopSuffix(), "@{endfor}", null);
        
        if (endforPos == -1) {
            elements.add(new StaticTextElement(content.substring(startPos)));
            return;
        }
        
        // Extract loop syntax
        int loopEnd = content.indexOf(config.getLoopSuffix(), startPos);
        if (loopEnd == -1) {
            elements.add(new StaticTextElement(content.substring(startPos)));
            return;
        }
        
        String loopSyntax = content.substring(
            startPos + config.getLoopPrefix().length(), loopEnd).trim();
        
        // Parse "for item in items"
        String variable = "";
        String collection = "";
        
        if (loopSyntax.startsWith("for ") && loopSyntax.contains(" in ")) {
            String[] parts = loopSyntax.substring(4).split(" in ", 2);
            variable = parts[0].trim();
            collection = parts[1].trim();
        }
        
        // Parse loop content
        String loopContent = content.substring(loopEnd + config.getLoopSuffix().length(), endforPos);
        List<TemplateElement> loopElements = parseTemplate(loopContent);
        
        elements.add(new LoopElement(variable, collection, loopElements));
    }
    
    /**
     * Parses a function element.
     */
    private void parseFunction(String content, int startPos, List<TemplateElement> elements) {
        int endPos = content.indexOf(config.getFunctionSuffix(), startPos);
        if (endPos == -1) {
            elements.add(new StaticTextElement(content.substring(startPos)));
            return;
        }
        
        String functionContent = content.substring(
            startPos + config.getFunctionPrefix().length(), endPos);
        
        // Parse function name and arguments
        String functionName;
        List<String> arguments = new ArrayList<>();
        
        if (functionContent.contains("(")) {
            int parenIndex = functionContent.indexOf('(');
            functionName = functionContent.substring(0, parenIndex).trim();
            
            String argsString = functionContent.substring(parenIndex + 1);
            if (argsString.endsWith(")")) {
                argsString = argsString.substring(0, argsString.length() - 1);
            }
            
            // Parse arguments
            if (!argsString.trim().isEmpty()) {
                String[] args = argsString.split(",");
                for (String arg : args) {
                    arguments.add(arg.trim());
                }
            }
        } else {
            functionName = functionContent.trim();
        }
        
        elements.add(new FunctionElement(functionName, arguments, this));
    }
    
    /**
     * Finds the end position of a variable.
     */
    private int findEndOfVariable(String content, int startPos) {
        int endPos = content.indexOf(config.getVariableSuffix(), startPos);
        return endPos != -1 ? endPos + config.getVariableSuffix().length() : content.length();
    }
    
    /**
     * Finds the end position of a conditional.
     */
    private int findEndOfConditional(String content, int startPos) {
        return content.indexOf("#{endif}", startPos) + 6;
    }
    
    /**
     * Finds the end position of a loop.
     */
    private int findEndOfLoop(String content, int startPos) {
        return content.indexOf("@{endfor}", startPos) + 8;
    }
    
    /**
     * Finds the end position of a function.
     */
    private int findEndOfFunction(String content, int startPos) {
        int endPos = content.indexOf(config.getFunctionSuffix(), startPos);
        return endPos != -1 ? endPos + config.getFunctionSuffix().length() : content.length();
    }
    
    /**
     * Finds matching end tag for nested structures.
     */
    private int findMatchingEnd(String content, int startPos, String openPrefix, String openSuffix,
                                String endTag, String elseTag) {
        int depth = 1;
        int pos = startPos + openPrefix.length();
        
        while (pos < content.length() && depth > 0) {
            int nextOpen = content.indexOf(openPrefix, pos);
            int nextEnd = content.indexOf(endTag, pos);
            
            if (nextEnd == -1) {
                return -1;
            }
            
            if (nextOpen != -1 && nextOpen < nextEnd) {
                depth++;
                pos = nextOpen + openPrefix.length();
            } else {
                depth--;
                if (depth == 0) {
                    return nextEnd;
                }
                pos = nextEnd + endTag.length();
            }
        }
        
        return -1;
    }
    
    /**
     * Processes a template.
     * @param templateId The template ID
     * @param content The template content
     * @param context The template context
     * @return Processed content
     */
    public String processTemplate(String templateId, String content, Map<String, Object> context) {
        Template template = compileTemplate(templateId, content);
        return template.process(context);
    }
    
    /**
     * Processes a template string.
     * @param content The template content
     * @param context The template context
     * @return Processed content
     */
    public String processTemplate(String content, Map<String, Object> context) {
        return processTemplate("temp_" + System.currentTimeMillis(), content, context);
    }
    
    /**
     * Adds a template function.
     * @param name The function name
     * @param function The function implementation
     */
    public void addFunction(String name, TemplateFunction function) {
        functions.put(name, function);
        LOG.info("Template function added: " + name);
    }
    
    /**
     * Removes a template function.
     * @param name The function name to remove
     */
    public void removeFunction(String name) {
        functions.remove(name);
        LOG.info("Template function removed: " + name);
    }
    
    /**
     * Gets a template function.
     * @param name The function name
     * @return The function or null if not found
     */
    public TemplateFunction getFunction(String name) {
        return functions.get(name);
    }
    
    /**
     * Gets all template functions.
     * @return Copy of all functions
     */
    public Map<String, TemplateFunction> getFunctions() {
        return new HashMap<>(functions);
    }
    
    /**
     * Clears template cache.
     */
    public void clearCache() {
        cachedTemplates.clear();
        LOG.info("Template cache cleared");
    }
    
    /**
     * Gets cache statistics.
     * @return Cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachedTemplates", cachedTemplates.size());
        stats.put("maxCacheSize", config.getMaxCacheSize());
        stats.put("cachingEnabled", config.isEnableCaching());
        return stats;
    }
}
