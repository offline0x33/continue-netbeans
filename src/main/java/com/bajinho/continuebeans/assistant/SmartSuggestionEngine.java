package com.bajinho.continuebeans.assistant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bajinho.continuebeans.assistant.ContextAwareAssistant.Context;
import com.bajinho.continuebeans.assistant.ContextAwareAssistant.ProjectContext;
import com.bajinho.continuebeans.assistant.ContextAwareAssistant.UserContext;
import com.bajinho.continuebeans.assistant.ContextAwareAssistant.AssistantSuggestion;
import com.bajinho.continuebeans.assistant.ContextAwareAssistant.SuggestionProvider;

/**
 * Smart suggestion engine with pattern recognition, learning capabilities,
 * contextual analysis, and intelligent recommendation system.
 * 
 * @author Continue Beans Team
 */
public class SmartSuggestionEngine {
    
    private static final Logger LOG = Logger.getLogger(SmartSuggestionEngine.class.getName());
    
    private final Map<String, SuggestionPattern> patterns;
    private final List<SuggestionProvider> providers;
    private final LearningEngine learningEngine;
    private final ContextAnalyzer contextAnalyzer;
    private final SuggestionCache suggestionCache;
    
    /**
     * Suggestion pattern.
     */
    public static class SuggestionPattern {
        private final String patternId;
        private final String name;
        private final String description;
        private final String condition;
        private final String action;
        private final Map<String, Object> parameters;
        private final double confidence;
        private final String category;
        private final int priority;
        
        public SuggestionPattern(String patternId, String name, String description,
                                String condition, String action, Map<String, Object> parameters,
                                double confidence, String category, int priority) {
            this.patternId = patternId;
            this.name = name;
            this.description = description;
            this.condition = condition;
            this.action = action;
            this.parameters = parameters != null ? parameters : new HashMap<>();
            this.confidence = confidence;
            this.category = category;
            this.priority = priority;
        }
        
        // Getters
        public String getPatternId() { return patternId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getCondition() { return condition; }
        public String getAction() { return action; }
        public Map<String, Object> getParameters() { return parameters; }
        public double getConfidence() { return confidence; }
        public String getCategory() { return category; }
        public int getPriority() { return priority; }
    }
    
    /**
     * Learning engine.
     */
    public static class LearningEngine {
        private final Map<String, UserPattern> userPatterns;
        private final Map<String, PatternUsage> patternUsage;
        
        public LearningEngine() {
            this.userPatterns = new ConcurrentHashMap<>();
            this.patternUsage = new ConcurrentHashMap<>();
        }
        
        /**
         * Records pattern usage.
         * @param userId The user ID
         * @param patternId The pattern ID
         * @param accepted Whether the suggestion was accepted
         */
        public void recordUsage(String userId, String patternId, boolean accepted) {
            String key = userId + ":" + patternId;
            PatternUsage usage = patternUsage.computeIfAbsent(key, k -> new PatternUsage());
            usage.recordUsage(accepted);
            
            // Update user pattern
            UserPattern userPattern = userPatterns.computeIfAbsent(userId, k -> new UserPattern());
            userPattern.updatePattern(patternId, accepted);
        }
        
        /**
         * Gets user pattern.
         * @param userId The user ID
         * @return User pattern
         */
        public UserPattern getUserPattern(String userId) {
            return userPatterns.get(userId);
        }
        
        /**
         * Gets pattern usage.
         * @param userId The user ID
         * @param patternId The pattern ID
         * @return Pattern usage
         */
        public PatternUsage getPatternUsage(String userId, String patternId) {
            return patternUsage.get(userId + ":" + patternId);
        }
    }
    
    /**
     * User pattern information.
     */
    public static class UserPattern {
        private final Map<String, PatternStats> patterns;
        private final long lastUpdated;
        
        public UserPattern() {
            this.patterns = new HashMap<>();
            this.lastUpdated = System.currentTimeMillis();
        }
        
        /**
         * Updates pattern statistics.
         * @param patternId The pattern ID
         * @param accepted Whether accepted
         */
        public void updatePattern(String patternId, boolean accepted) {
            PatternStats stats = patterns.computeIfAbsent(patternId, k -> new PatternStats());
            stats.update(accepted);
        }
        
        /**
         * Gets pattern statistics.
         * @param patternId The pattern ID
         * @return Pattern stats
         */
        public PatternStats getPatternStats(String patternId) {
            return patterns.get(patternId);
        }
        
        // Getters
        public Map<String, PatternStats> getPatterns() { return patterns; }
        public long getLastUpdated() { return lastUpdated; }
    }
    
    /**
     * Pattern statistics.
     */
    public static class PatternStats {
        private int totalUsage;
        private int acceptedUsage;
        private double acceptanceRate;
        
        public PatternStats() {
            this.totalUsage = 0;
            this.acceptedUsage = 0;
            this.acceptanceRate = 0.0;
        }
        
        /**
         * Updates statistics.
         * @param accepted Whether accepted
         */
        public void update(boolean accepted) {
            totalUsage++;
            if (accepted) {
                acceptedUsage++;
            }
            acceptanceRate = totalUsage > 0 ? (double) acceptedUsage / totalUsage : 0.0;
        }
        
        // Getters
        public int getTotalUsage() { return totalUsage; }
        public int getAcceptedUsage() { return acceptedUsage; }
        public double getAcceptanceRate() { return acceptanceRate; }
    }
    
    /**
     * Pattern usage tracking.
     */
    public static class PatternUsage {
        private int totalUsage;
        private int acceptedUsage;
        private long lastUsed;
        
        public PatternUsage() {
            this.totalUsage = 0;
            this.acceptedUsage = 0;
            this.lastUsed = 0;
        }
        
        /**
         * Records usage.
         * @param accepted Whether accepted
         */
        public void recordUsage(boolean accepted) {
            totalUsage++;
            if (accepted) {
                acceptedUsage++;
            }
            lastUsed = System.currentTimeMillis();
        }
        
        // Getters
        public int getTotalUsage() { return totalUsage; }
        public int getAcceptedUsage() { return acceptedUsage; }
        public long getLastUsed() { return lastUsed; }
        
        public double getAcceptanceRate() {
            return totalUsage > 0 ? (double) acceptedUsage / totalUsage : 0.0;
        }
    }
    
    /**
     * Context analyzer.
     */
    public static class ContextAnalyzer {
        private final Map<String, ContextRule> rules;
        
        public ContextAnalyzer() {
            this.rules = new HashMap<>();
            initializeDefaultRules();
        }
        
        /**
         * Initializes default context rules.
         */
        private void initializeDefaultRules() {
            // TODO: Initialize default context analysis rules
        }
        
        /**
         * Analyzes context and returns relevant factors.
         * @param context The context
         * @return Context factors
         */
        public Map<String, Object> analyzeContext(Context context) {
            Map<String, Object> factors = new HashMap<>();
            
            // Analyze project context
            if (context.getProjectContext() != null) {
                ProjectContext projectContext = context.getProjectContext();
                factors.put("projectType", projectContext.getProjectType());
                factors.put("projectSize", projectContext.getMetrics().getTotalFiles());
                factors.put("complexity", projectContext.getMetrics().getComplexity());
            }
            
            // Analyze user context
            if (context.getUserContext() != null) {
                UserContext userContext = context.getUserContext();
                factors.put("recentFiles", userContext.getRecentFiles().size());
                factors.put("commandFrequency", userContext.getCommandFrequency());
            }
            
            return factors;
        }
        
        /**
         * Adds a context rule.
         * @param ruleId The rule ID
         * @param rule The rule to add
         */
        public void addRule(String ruleId, ContextRule rule) {
            rules.put(ruleId, rule);
        }
    }
    
    /**
     * Context rule interface.
     */
    public interface ContextRule {
        boolean matches(Context context);
        Map<String, Object> extractFactors(Context context);
    }
    
    /**
     * Suggestion cache.
     */
    public static class SuggestionCache {
        private final Map<String, CachedSuggestion> cache;
        private final long maxAge;
        private final int maxSize;
        
        public SuggestionCache(long maxAge, int maxSize) {
            this.cache = new ConcurrentHashMap<>();
            this.maxAge = maxAge;
            this.maxSize = maxSize;
        }
        
        /**
         * Gets cached suggestions.
         * @param key The cache key
         * @return Cached suggestions or null
         */
        public List<AssistantSuggestion> get(String key) {
            CachedSuggestion cached = cache.get(key);
            if (cached != null && (System.currentTimeMillis() - cached.getTimestamp()) < maxAge) {
                return cached.getSuggestions();
            }
            return null;
        }
        
        /**
         * Puts suggestions in cache.
         * @param key The cache key
         * @param suggestions The suggestions to cache
         */
        public void put(String key, List<AssistantSuggestion> suggestions) {
            if (cache.size() >= maxSize) {
                // Remove oldest entry
                String oldestKey = cache.keySet().iterator().next();
                cache.remove(oldestKey);
            }
            cache.put(key, new CachedSuggestion(suggestions, System.currentTimeMillis()));
        }
        
        /**
         * Clears the cache.
         */
        public void clear() {
            cache.clear();
        }
    }
    
    /**
     * Cached suggestion wrapper.
     */
    public static class CachedSuggestion {
        private final List<AssistantSuggestion> suggestions;
        private final long timestamp;
        
        public CachedSuggestion(List<AssistantSuggestion> suggestions, long timestamp) {
            this.suggestions = suggestions;
            this.timestamp = timestamp;
        }
        
        public List<AssistantSuggestion> getSuggestions() { return suggestions; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Smart suggestion provider.
     */
    public static class SmartSuggestionProvider implements SuggestionProvider {
        private final SmartSuggestionEngine engine;
        
        public SmartSuggestionProvider(SmartSuggestionEngine engine) {
            this.engine = engine;
        }
        
        @Override
        public List<AssistantSuggestion> generateSuggestions(Context context) {
            return engine.generateSmartSuggestions(context);
        }
        
        @Override
        public String getProviderName() {
            return "SmartSuggestionProvider";
        }
    }
    
    /**
     * Constructor.
     */
    public SmartSuggestionEngine() {
        this.patterns = new ConcurrentHashMap<>();
        this.providers = new ArrayList<>();
        this.learningEngine = new LearningEngine();
        this.contextAnalyzer = new ContextAnalyzer();
        this.suggestionCache = new SuggestionCache(60000, 200); // 1 minute, 200 entries
        
        initializeDefaultPatterns();
    }
    
    /**
     * Initializes default suggestion patterns.
     */
    private void initializeDefaultPatterns() {
        // Code completion pattern
        SuggestionPattern codeCompletion = new SuggestionPattern(
            "code_completion", "Code Completion", "Suggest code completions based on context",
            "currentWord != null && currentWord.length() > 2", "complete_code",
            Map.of("type", "completion"), 0.8, "code", 1
        );
        patterns.put("code_completion", codeCompletion);
        
        // Import suggestion pattern
        SuggestionPattern importSuggestion = new SuggestionPattern(
            "import_suggestion", "Import Suggestion", "Suggest missing imports",
            "hasUnresolvedImport", "add_import", Map.of("type", "import"), 0.9, "code", 2
        );
        patterns.put("import_suggestion", importSuggestion);
        
        // Refactoring pattern
        SuggestionPattern refactoring = new SuggestionPattern(
            "refactoring", "Refactoring Suggestion", "Suggest code refactoring",
            "complexity > 10", "refactor_code", Map.of("type", "refactor"), 0.7, "quality", 3
        );
        patterns.put("refactoring", refactoring);
        
        // Test generation pattern
        SuggestionPattern testGeneration = new SuggestionPattern(
            "test_generation", "Test Generation", "Suggest test creation",
            "hasNoTest && isPublicMethod", "create_test", Map.of("type", "test"), 0.8, "testing", 4
        );
        patterns.put("test_generation", testGeneration);
        
        // Documentation pattern
        SuggestionPattern documentation = new SuggestionPattern(
            "documentation", "Documentation", "Suggest adding documentation",
            "hasNoDoc && isPublic", "add_documentation", Map.of("type", "doc"), 0.6, "quality", 5
        );
        patterns.put("documentation", documentation);
    }
    
    /**
     * Generates smart suggestions based on context.
     * @param context The context
     * @return List of suggestions
     */
    public List<AssistantSuggestion> generateSmartSuggestions(Context context) {
        List<AssistantSuggestion> suggestions = new ArrayList<>();
        
        try {
            // Check cache first
            String cacheKey = generateCacheKey(context);
            List<AssistantSuggestion> cached = suggestionCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            
            // Analyze context
            Map<String, Object> factors = contextAnalyzer.analyzeContext(context);
            
            // Generate pattern-based suggestions
            for (SuggestionPattern pattern : patterns.values()) {
                if (evaluateCondition(pattern.getCondition(), factors, context)) {
                    AssistantSuggestion suggestion = createSuggestionFromPattern(pattern, context);
                    suggestions.add(suggestion);
                }
            }
            
            // Generate learning-based suggestions
            suggestions.addAll(generateLearningSuggestions(context));
            
            // Generate provider-based suggestions
            for (SuggestionProvider provider : providers) {
                try {
                    suggestions.addAll(provider.generateSuggestions(context));
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error generating suggestions with provider: " + provider.getClass().getSimpleName(), e);
                }
            }
            
            // Sort by confidence and priority
            suggestions.sort((a, b) -> {
                int priorityA = a.getParameters().containsKey("priority") ? 
                    Integer.parseInt(a.getParameters().get("priority").toString()) : 0;
                int priorityB = b.getParameters().containsKey("priority") ? 
                    Integer.parseInt(b.getParameters().get("priority").toString()) : 0;
                int priorityCompare = Integer.compare(priorityB, priorityA);
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                return Double.compare(b.getConfidence(), a.getConfidence());
            });
            
            // Cache suggestions
            suggestionCache.put(cacheKey, suggestions);
            
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error generating smart suggestions", e);
        }
        
        return suggestions;
    }
    
    /**
     * Evaluates a condition.
     * @param condition The condition string
     * @param factors Context factors
     * @param context The context
     * @return True if condition is met
     */
    private boolean evaluateCondition(String condition, Map<String, Object> factors, Context context) {
        // TODO: Implement proper condition evaluation
        // For now, use simple string matching
        
        if (condition.contains("currentWord")) {
            // Check if there's a current word in context
            return true; // Simplified
        }
        
        if (condition.contains("complexity")) {
            Object complexity = factors.get("complexity");
            if (complexity instanceof Number) {
                return ((Number) complexity).doubleValue() > 10;
            }
        }
        
        // Default to true for unknown conditions
        return true;
    }
    
    /**
     * Creates suggestion from pattern.
     * @param pattern The pattern
     * @param context The context
     * @return Assistant suggestion
     */
    private AssistantSuggestion createSuggestionFromPattern(SuggestionPattern pattern, Context context) {
        String suggestionId = "suggestion_" + System.currentTimeMillis() + "_" + pattern.getPatternId().hashCode();
        
        // Adjust confidence based on user learning
        double adjustedConfidence = pattern.getConfidence();
        if (context.getUserContext() != null) {
            String userId = context.getUserContext().getUserId();
            PatternUsage usage = learningEngine.getPatternUsage(userId, pattern.getPatternId());
            if (usage != null) {
                // Boost confidence if user frequently accepts this pattern
                double acceptanceRate = usage.getAcceptanceRate();
                adjustedConfidence = pattern.getConfidence() * (0.5 + acceptanceRate);
            }
        }
        
        Map<String, Object> parameters = new HashMap<>(pattern.getParameters());
        parameters.put("priority", pattern.getPriority());
        
        return new AssistantSuggestion(
            suggestionId, pattern.getCategory(), pattern.getName(),
            pattern.getDescription(), pattern.getAction(), parameters,
            adjustedConfidence, pattern.getCondition()
        );
    }
    
    /**
     * Generates learning-based suggestions.
     * @param context The context
     * @return List of suggestions
     */
    private List<AssistantSuggestion> generateLearningSuggestions(Context context) {
        List<AssistantSuggestion> suggestions = new ArrayList<>();
        
        if (context.getUserContext() != null) {
            String userId = context.getUserContext().getUserId();
            UserPattern userPattern = learningEngine.getUserPattern(userId);
            
            if (userPattern != null) {
                // Generate suggestions based on user patterns
                for (Map.Entry<String, PatternStats> entry : userPattern.getPatterns().entrySet()) {
                    String patternId = entry.getKey();
                    PatternStats stats = entry.getValue();
                    
                    // Suggest patterns with high acceptance rate
                    if (stats.getAcceptanceRate() > 0.7 && stats.getTotalUsage() > 5) {
                        SuggestionPattern pattern = patterns.get(patternId);
                        if (pattern != null) {
                            AssistantSuggestion suggestion = createSuggestionFromPattern(pattern, context);
                            // Boost confidence for user-preferred patterns
                            Map<String, Object> params = new HashMap<>(suggestion.getParameters());
                            params.put("userPreferred", true);
                            
                            AssistantSuggestion boostedSuggestion = new AssistantSuggestion(
                                suggestion.getSuggestionId(), suggestion.getType(), suggestion.getTitle(),
                                suggestion.getDescription(), suggestion.getAction(), params,
                                Math.min(1.0, suggestion.getConfidence() * 1.2), suggestion.getContext()
                            );
                            suggestions.add(boostedSuggestion);
                        }
                    }
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * Generates cache key for context.
     * @param context The context
     * @return Cache key
     */
    private String generateCacheKey(Context context) {
        StringBuilder key = new StringBuilder();
        
        if (context.getProjectContext() != null) {
            key.append(context.getProjectContext().getProjectPath());
        }
        
        if (context.getUserContext() != null) {
            key.append(":").append(context.getUserContext().getUserId());
        }
        
        // Include recent files for more specific caching
        if (context.getUserContext() != null && !context.getUserContext().getRecentFiles().isEmpty()) {
            key.append(":").append(context.getUserContext().getRecentFiles().get(0));
        }
        
        return key.toString();
    }
    
    /**
     * Records suggestion feedback.
     * @param userId The user ID
     * @param suggestionId The suggestion ID
     * @param accepted Whether the suggestion was accepted
     */
    public void recordFeedback(String userId, String suggestionId, boolean accepted) {
        // Extract pattern ID from suggestion ID
        String patternId = extractPatternId(suggestionId);
        if (patternId != null) {
            learningEngine.recordUsage(userId, patternId, accepted);
        }
    }
    
    /**
     * Extracts pattern ID from suggestion ID.
     * @param suggestionId The suggestion ID
     * @return Pattern ID or null
     */
    private String extractPatternId(String suggestionId) {
        // Extract pattern ID from suggestion ID format: "suggestion_timestamp_patternHash"
        if (suggestionId.contains("_")) {
            String[] parts = suggestionId.split("_");
            if (parts.length >= 3) {
                return parts[2];
            }
        }
        return null;
    }
    
    /**
     * Adds a suggestion pattern.
     * @param pattern The pattern to add
     */
    public void addPattern(SuggestionPattern pattern) {
        patterns.put(pattern.getPatternId(), pattern);
        LOG.info("Suggestion pattern added: " + pattern.getPatternId());
    }
    
    /**
     * Removes a suggestion pattern.
     * @param patternId The pattern ID to remove
     */
    public void removePattern(String patternId) {
        patterns.remove(patternId);
        LOG.info("Suggestion pattern removed: " + patternId);
    }
    
    /**
     * Adds a suggestion provider.
     * @param provider The provider to add
     */
    public void addProvider(SuggestionProvider provider) {
        providers.add(provider);
        LOG.info("Suggestion provider added: " + provider.getProviderName());
    }
    
    /**
     * Removes a suggestion provider.
     * @param provider The provider to remove
     */
    public void removeProvider(SuggestionProvider provider) {
        providers.remove(provider);
        LOG.info("Suggestion provider removed: " + provider.getProviderName());
    }
    
    /**
     * Gets all patterns.
     * @return Copy of all patterns
     */
    public Map<String, SuggestionPattern> getPatterns() {
        return new HashMap<>(patterns);
    }
    
    /**
     * Gets learning engine.
     * @return Learning engine
     */
    public LearningEngine getLearningEngine() {
        return learningEngine;
    }
    
    /**
     * Gets context analyzer.
     * @return Context analyzer
     */
    public ContextAnalyzer getContextAnalyzer() {
        return contextAnalyzer;
    }
    
    /**
     * Clears suggestion cache.
     */
    public void clearCache() {
        suggestionCache.clear();
        LOG.info("Suggestion cache cleared");
    }
    
    /**
     * Gets suggestion statistics.
     * @return Statistics map
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("patterns", patterns.size());
        stats.put("providers", providers.size());
        stats.put("cacheSize", suggestionCache.cache.size());
        stats.put("userPatterns", learningEngine.userPatterns.size());
        stats.put("patternUsage", learningEngine.patternUsage.size());
        return stats;
    }
}
