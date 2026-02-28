package com.bajinho.continuebeans.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Multi-provider router for intelligent AI provider selection, load balancing,
 * failover handling, and provider optimization.
 * 
 * @author Continue Beans Team
 */
public class MultiProviderRouter {
    
    private static final Logger LOG = Logger.getLogger(MultiProviderRouter.class.getName());
    
    private static MultiProviderRouter instance;
    
    private final Map<String, ProviderConfig> providerConfigs;
    private final List<ProviderStrategy> strategies;
    private final LoadBalancer loadBalancer;
    private final FailoverManager failoverManager;
    private final ProviderOptimizer optimizer;
    private final RoutingMetrics metrics;
    
    /**
     * Provider configuration.
     */
    public static class ProviderConfig {
        private final String providerId;
        private final int priority;
        private final double weight;
        private final int maxRequestsPerMinute;
        private final int maxConcurrentRequests;
        private final boolean enabled;
        private final Map<String, Object> capabilities;
        private final List<String> supportedModels;
        private final ProviderSelectionCriteria selectionCriteria;
        
        public ProviderConfig(String providerId, int priority, double weight,
                             int maxRequestsPerMinute, int maxConcurrentRequests, boolean enabled,
                             Map<String, Object> capabilities, List<String> supportedModels,
                             ProviderSelectionCriteria selectionCriteria) {
            this.providerId = providerId;
            this.priority = priority;
            this.weight = weight;
            this.maxRequestsPerMinute = maxRequestsPerMinute;
            this.maxConcurrentRequests = maxConcurrentRequests;
            this.enabled = enabled;
            this.capabilities = capabilities != null ? capabilities : new HashMap<>();
            this.supportedModels = supportedModels != null ? supportedModels : new ArrayList<>();
            this.selectionCriteria = selectionCriteria != null ? selectionCriteria : ProviderSelectionCriteria.DEFAULT;
        }
        
        // Getters
        public String getProviderId() { return providerId; }
        public int getPriority() { return priority; }
        public double getWeight() { return weight; }
        public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
        public int getMaxConcurrentRequests() { return maxConcurrentRequests; }
        public boolean isEnabled() { return enabled; }
        public Map<String, Object> getCapabilities() { return capabilities; }
        public List<String> getSupportedModels() { return supportedModels; }
        public ProviderSelectionCriteria getSelectionCriteria() { return selectionCriteria; }
    }
    
    /**
     * Provider selection criteria.
     */
    public enum ProviderSelectionCriteria {
        DEFAULT,         // Default selection
        COST_OPTIMIZED,  // Cost-optimized selection
        SPEED_OPTIMIZED, // Speed-optimized selection
        QUALITY_OPTIMIZED, // Quality-optimized selection
        LOAD_BALANCED,   // Load-balanced selection
        FAILOVER_SAFE    // Failover-safe selection
    }
    
    /**
     * Provider strategy interface.
     */
    public interface ProviderStrategy {
        String selectProvider(List<ProviderConfig> providers, RoutingRequest request);
        String getStrategyName();
    }
    
    /**
     * Routing request.
     */
    public static class RoutingRequest {
        private final String requestId;
        private final String modelId;
        private final String taskType;
        private final Map<String, Object> parameters;
        private final long timestamp;
        private final int priority;
        
        public RoutingRequest(String requestId, String modelId, String taskType,
                            Map<String, Object> parameters, int priority) {
            this.requestId = requestId;
            this.modelId = modelId;
            this.taskType = taskType;
            this.parameters = parameters != null ? parameters : new HashMap<>();
            this.timestamp = System.currentTimeMillis();
            this.priority = priority;
        }
        
        // Getters
        public String getRequestId() { return requestId; }
        public String getModelId() { return modelId; }
        public String getTaskType() { return taskType; }
        public Map<String, Object> getParameters() { return parameters; }
        public long getTimestamp() { return timestamp; }
        public int getPriority() { return priority; }
    }
    
    /**
     * Load balancer.
     */
    public static class LoadBalancer {
        private final Map<String, ProviderLoad> providerLoads;
        private final AtomicInteger roundRobinIndex;
        
        public LoadBalancer() {
            this.providerLoads = new ConcurrentHashMap<>();
            this.roundRobinIndex = new AtomicInteger(0);
        }
        
        /**
         * Selects provider using round-robin.
         * @param providers Available providers
         * @return Selected provider
         */
        public String selectRoundRobin(List<ProviderConfig> providers) {
            if (providers.isEmpty()) {
                return null;
            }
            
            List<ProviderConfig> enabledProviders = new ArrayList<>();
            for (ProviderConfig provider : providers) {
                if (provider.isEnabled() && !isOverloaded(provider.getProviderId())) {
                    enabledProviders.add(provider);
                }
            }
            
            if (enabledProviders.isEmpty()) {
                return null;
            }
            
            int index = roundRobinIndex.getAndIncrement() % enabledProviders.size();
            return enabledProviders.get(index).getProviderId();
        }
        
        /**
         * Selects provider using weighted round-robin.
         * @param providers Available providers
         * @return Selected provider
         */
        public String selectWeightedRoundRobin(List<ProviderConfig> providers) {
            if (providers.isEmpty()) {
                return null;
            }
            
            // Calculate total weight
            double totalWeight = 0;
            for (ProviderConfig provider : providers) {
                if (provider.isEnabled() && !isOverloaded(provider.getProviderId())) {
                    totalWeight += provider.getWeight();
                }
            }
            
            if (totalWeight == 0) {
                return null;
            }
            
            // Select based on weight
            double random = Math.random() * totalWeight;
            double currentWeight = 0;
            
            for (ProviderConfig provider : providers) {
                if (provider.isEnabled() && !isOverloaded(provider.getProviderId())) {
                    currentWeight += provider.getWeight();
                    if (random <= currentWeight) {
                        return provider.getProviderId();
                    }
                }
            }
            
            return providers.get(0).getProviderId();
        }
        
        /**
         * Selects provider using least connections.
         * @param providers Available providers
         * @return Selected provider
         */
        public String selectLeastConnections(List<ProviderConfig> providers) {
            if (providers.isEmpty()) {
                return null;
            }
            
            String selectedProvider = null;
            int minConnections = Integer.MAX_VALUE;
            
            for (ProviderConfig provider : providers) {
                if (provider.isEnabled() && !isOverloaded(provider.getProviderId())) {
                    ProviderLoad load = providerLoads.get(provider.getProviderId());
                    int connections = load != null ? load.getCurrentConnections() : 0;
                    
                    if (connections < minConnections) {
                        minConnections = connections;
                        selectedProvider = provider.getProviderId();
                    }
                }
            }
            
            return selectedProvider;
        }
        
        /**
         * Increments connection count.
         * @param providerId The provider ID
         */
        public void incrementConnections(String providerId) {
            ProviderLoad load = providerLoads.computeIfAbsent(providerId, k -> new ProviderLoad());
            load.incrementConnections();
        }
        
        /**
         * Decrements connection count.
         * @param providerId The provider ID
         */
        public void decrementConnections(String providerId) {
            ProviderLoad load = providerLoads.get(providerId);
            if (load != null) {
                load.decrementConnections();
            }
        }
        
        /**
         * Checks if provider is overloaded.
         * @param providerId The provider ID
         * @return True if overloaded
         */
        private boolean isOverloaded(String providerId) {
            ProviderLoad load = providerLoads.get(providerId);
            return load != null && load.isOverloaded();
        }
    }
    
    /**
     * Provider load information.
     */
    public static class ProviderLoad {
        private int currentConnections;
        private long lastRequestTime;
        private int requestsPerMinute;
        private final long[] requestTimestamps;
        private int requestIndex;
        
        public ProviderLoad() {
            this.currentConnections = 0;
            this.lastRequestTime = 0;
            this.requestsPerMinute = 0;
            this.requestTimestamps = new long[60]; // Track last 60 seconds
            this.requestIndex = 0;
        }
        
        /**
         * Increments connection count.
         */
        public void incrementConnections() {
            currentConnections++;
            updateRequestRate();
        }
        
        /**
         * Decrements connection count.
         */
        public void decrementConnections() {
            if (currentConnections > 0) {
                currentConnections--;
            }
        }
        
        /**
         * Updates request rate.
         */
        private void updateRequestRate() {
            long now = System.currentTimeMillis();
            requestTimestamps[requestIndex] = now;
            requestIndex = (requestIndex + 1) % 60;
            
            // Count requests in last minute
            int count = 0;
            for (int i = 0; i < 60; i++) {
                if (now - requestTimestamps[i] < 60000) {
                    count++;
                }
            }
            requestsPerMinute = count;
            lastRequestTime = now;
        }
        
        /**
         * Checks if provider is overloaded.
         * @return True if overloaded
         */
        public boolean isOverloaded() {
            // TODO: Use actual provider limits
            return currentConnections > 10 || requestsPerMinute > 100;
        }
        
        // Getters
        public int getCurrentConnections() { return currentConnections; }
        public long getLastRequestTime() { return lastRequestTime; }
        public int getRequestsPerMinute() { return requestsPerMinute; }
    }
    
    /**
     * Failover manager.
     */
    public static class FailoverManager {
        private final Map<String, ProviderHealth> providerHealth;
        private final Map<String, List<String>> failoverChains;
        
        public FailoverManager() {
            this.providerHealth = new ConcurrentHashMap<>();
            this.failoverChains = new ConcurrentHashMap<>();
            initializeFailoverChains();
        }
        
        /**
         * Initializes failover chains.
         */
        private void initializeFailoverChains() {
            // OpenAI -> Local -> Anthropic
            failoverChains.put("openai", List.of("local", "anthropic"));
            
            // Local -> OpenAI -> Anthropic
            failoverChains.put("local", List.of("openai", "anthropic"));
            
            // Anthropic -> OpenAI -> Local
            failoverChains.put("anthropic", List.of("openai", "local"));
        }
        
        /**
         * Gets failover provider.
         * @param primaryProvider The primary provider
         * @return Failover provider or null
         */
        public String getFailoverProvider(String primaryProvider) {
            ProviderHealth health = providerHealth.get(primaryProvider);
            if (health != null && health.isHealthy()) {
                return null; // Primary provider is healthy
            }
            
            List<String> chain = failoverChains.get(primaryProvider);
            if (chain != null) {
                for (String providerId : chain) {
                    ProviderHealth providerHealth = this.providerHealth.get(providerId);
                    if (providerHealth == null || providerHealth.isHealthy()) {
                        return providerId;
                    }
                }
            }
            
            return null;
        }
        
        /**
         * Updates provider health.
         * @param providerId The provider ID
         * @param healthy Whether the provider is healthy
         */
        public void updateProviderHealth(String providerId, boolean healthy) {
            ProviderHealth health = providerHealth.computeIfAbsent(providerId, k -> new ProviderHealth());
            health.updateHealth(healthy);
        }
        
        /**
         * Gets provider health.
         * @param providerId The provider ID
         * @return Provider health
         */
        public ProviderHealth getProviderHealth(String providerId) {
            return providerHealth.get(providerId);
        }
    }
    
    /**
     * Provider health information.
     */
    public static class ProviderHealth {
        private boolean healthy;
        private long lastCheck;
        private int consecutiveFailures;
        private int consecutiveSuccesses;
        
        public ProviderHealth() {
            this.healthy = true;
            this.lastCheck = System.currentTimeMillis();
            this.consecutiveFailures = 0;
            this.consecutiveSuccesses = 0;
        }
        
        /**
         * Updates health status.
         * @param isHealthy Whether the provider is healthy
         */
        public void updateHealth(boolean isHealthy) {
            this.lastCheck = System.currentTimeMillis();
            
            if (isHealthy) {
                consecutiveSuccesses++;
                consecutiveFailures = 0;
                if (consecutiveSuccesses >= 3) {
                    healthy = true;
                }
            } else {
                consecutiveFailures++;
                consecutiveSuccesses = 0;
                if (consecutiveFailures >= 3) {
                    healthy = false;
                }
            }
        }
        
        // Getters
        public boolean isHealthy() { return healthy; }
        public long getLastCheck() { return lastCheck; }
        public int getConsecutiveFailures() { return consecutiveFailures; }
        public int getConsecutiveSuccesses() { return consecutiveSuccesses; }
    }
    
    /**
     * Provider optimizer.
     */
    public static class ProviderOptimizer {
        private final Map<String, ProviderPerformance> providerPerformance;
        
        public ProviderOptimizer() {
            this.providerPerformance = new ConcurrentHashMap<>();
        }
        
        /**
         * Records provider performance.
         * @param providerId The provider ID
         * @param responseTime The response time
         * @param success Whether the request was successful
         * @param cost The cost
         */
        public void recordPerformance(String providerId, long responseTime, boolean success, double cost) {
            ProviderPerformance performance = providerPerformance.computeIfAbsent(providerId, k -> new ProviderPerformance());
            performance.recordPerformance(responseTime, success, cost);
        }
        
        /**
         * Gets best provider for criteria.
         * @param providers Available providers
         * @param criteria Selection criteria
         * @return Best provider
         */
        public String getBestProvider(List<ProviderConfig> providers, ProviderSelectionCriteria criteria) {
            if (providers.isEmpty()) {
                return null;
            }
            
            String bestProvider = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            
            for (ProviderConfig provider : providers) {
                if (!provider.isEnabled()) {
                    continue;
                }
                
                ProviderPerformance performance = providerPerformance.get(provider.getProviderId());
                double score = calculateScore(provider, performance, criteria);
                
                if (score > bestScore) {
                    bestScore = score;
                    bestProvider = provider.getProviderId();
                }
            }
            
            return bestProvider;
        }
        
        /**
         * Calculates provider score.
         * @param provider The provider config
         * @param performance The performance data
         * @param criteria The selection criteria
         * @return The score
         */
        private double calculateScore(ProviderConfig provider, ProviderPerformance performance, ProviderSelectionCriteria criteria) {
            if (performance == null) {
                return 0.0;
            }
            
            switch (criteria) {
                case COST_OPTIMIZED:
                    return -performance.getAverageCost(); // Lower cost is better
                case SPEED_OPTIMIZED:
                    return -performance.getAverageResponseTime(); // Lower response time is better
                case QUALITY_OPTIMIZED:
                    return performance.getSuccessRate(); // Higher success rate is better
                case LOAD_BALANCED:
                    return 1.0 / (performance.getCurrentLoad() + 1); // Lower load is better
                default:
                    return performance.getSuccessRate() * 0.5 + (1.0 / (performance.getAverageResponseTime() + 1)) * 0.3 + (1.0 / (performance.getAverageCost() + 0.01)) * 0.2;
            }
        }
    }
    
    /**
     * Provider performance data.
     */
    public static class ProviderPerformance {
        private long totalResponseTime;
        private int totalRequests;
        private int successfulRequests;
        private double totalCost;
        private int currentLoad;
        private long lastUpdate;
        
        public ProviderPerformance() {
            this.lastUpdate = System.currentTimeMillis();
        }
        
        /**
         * Records performance data.
         * @param responseTime The response time
         * @param success Whether successful
         * @param cost The cost
         */
        public void recordPerformance(long responseTime, boolean success, double cost) {
            totalRequests++;
            if (success) {
                successfulRequests++;
            }
            totalResponseTime += responseTime;
            totalCost += cost;
            lastUpdate = System.currentTimeMillis();
        }
        
        /**
         * Gets average response time.
         * @return Average response time
         */
        public double getAverageResponseTime() {
            return totalRequests > 0 ? (double) totalResponseTime / totalRequests : 0.0;
        }
        
        /**
         * Gets success rate.
         * @return Success rate
         */
        public double getSuccessRate() {
            return totalRequests > 0 ? (double) successfulRequests / totalRequests : 0.0;
        }
        
        /**
         * Gets average cost.
         * @return Average cost
         */
        public double getAverageCost() {
            return totalRequests > 0 ? totalCost / totalRequests : 0.0;
        }
        
        // Getters
        public long getTotalResponseTime() { return totalResponseTime; }
        public int getTotalRequests() { return totalRequests; }
        public int getSuccessfulRequests() { return successfulRequests; }
        public double getTotalCost() { return totalCost; }
        public int getCurrentLoad() { return currentLoad; }
        public long getLastUpdate() { return lastUpdate; }
    }
    
    /**
     * Routing metrics.
     */
    public static class RoutingMetrics {
        private final Map<String, AtomicInteger> providerRequestCounts;
        private final Map<String, AtomicInteger> providerSuccessCounts;
        private final Map<String, AtomicInteger> providerFailureCounts;
        private final AtomicInteger totalRequests;
        private final AtomicInteger totalFailures;
        
        public RoutingMetrics() {
            this.providerRequestCounts = new ConcurrentHashMap<>();
            this.providerSuccessCounts = new ConcurrentHashMap<>();
            this.providerFailureCounts = new ConcurrentHashMap<>();
            this.totalRequests = new AtomicInteger(0);
            this.totalFailures = new AtomicInteger(0);
        }
        
        /**
         * Records request.
         * @param providerId The provider ID
         * @param success Whether successful
         */
        public void recordRequest(String providerId, boolean success) {
            totalRequests.incrementAndGet();
            providerRequestCounts.computeIfAbsent(providerId, k -> new AtomicInteger(0)).incrementAndGet();
            
            if (success) {
                providerSuccessCounts.computeIfAbsent(providerId, k -> new AtomicInteger(0)).incrementAndGet();
            } else {
                totalFailures.incrementAndGet();
                providerFailureCounts.computeIfAbsent(providerId, k -> new AtomicInteger(0)).incrementAndGet();
            }
        }
        
        /**
         * Gets provider statistics.
         * @param providerId The provider ID
         * @return Provider statistics
         */
        public Map<String, Object> getProviderStats(String providerId) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("requests", providerRequestCounts.getOrDefault(providerId, new AtomicInteger(0)).get());
            stats.put("successes", providerSuccessCounts.getOrDefault(providerId, new AtomicInteger(0)).get());
            stats.put("failures", providerFailureCounts.getOrDefault(providerId, new AtomicInteger(0)).get());
            
            int requests = providerRequestCounts.getOrDefault(providerId, new AtomicInteger(0)).get();
            int successes = providerSuccessCounts.getOrDefault(providerId, new AtomicInteger(0)).get();
            stats.put("successRate", requests > 0 ? (double) successes / requests : 0.0);
            
            return stats;
        }
        
        // Getters
        public int getTotalRequests() { return totalRequests.get(); }
        public int getTotalFailures() { return totalFailures.get(); }
        public double getOverallSuccessRate() {
            int total = totalRequests.get();
            return total > 0 ? (double) (total - totalFailures.get()) / total : 0.0;
        }
    }
    
    /**
     * Private constructor for singleton.
     */
    private MultiProviderRouter() {
        this.providerConfigs = new ConcurrentHashMap<>();
        this.strategies = new ArrayList<>();
        this.loadBalancer = new LoadBalancer();
        this.failoverManager = new FailoverManager();
        this.optimizer = new ProviderOptimizer();
        this.metrics = new RoutingMetrics();
        
        initializeStrategies();
        initializeProviders();
        
        LOG.info("MultiProviderRouter initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The MultiProviderRouter instance
     */
    public static synchronized MultiProviderRouter getInstance() {
        if (instance == null) {
            instance = new MultiProviderRouter();
        }
        return instance;
    }
    
    /**
     * Initializes routing strategies.
     */
    private void initializeStrategies() {
        // Round-robin strategy
        strategies.add(new ProviderStrategy() {
            @Override
            public String selectProvider(List<ProviderConfig> providers, RoutingRequest request) {
                return loadBalancer.selectRoundRobin(providers);
            }
            
            @Override
            public String getStrategyName() {
                return "RoundRobin";
            }
        });
        
        // Weighted round-robin strategy
        strategies.add(new ProviderStrategy() {
            @Override
            public String selectProvider(List<ProviderConfig> providers, RoutingRequest request) {
                return loadBalancer.selectWeightedRoundRobin(providers);
            }
            
            @Override
            public String getStrategyName() {
                return "WeightedRoundRobin";
            }
        });
        
        // Least connections strategy
        strategies.add(new ProviderStrategy() {
            @Override
            public String selectProvider(List<ProviderConfig> providers, RoutingRequest request) {
                return loadBalancer.selectLeastConnections(providers);
            }
            
            @Override
            public String getStrategyName() {
                return "LeastConnections";
            }
        });
    }
    
    /**
     * Initializes default providers.
     */
    private void initializeProviders() {
        // OpenAI provider config
        ProviderConfig openai = new ProviderConfig(
            "openai", 1, 1.0, 1000, 10, true,
            Map.of("maxTokens", 4096, "supportsStreaming", true),
            List.of("gpt-4", "gpt-3.5-turbo"),
            ProviderSelectionCriteria.DEFAULT
        );
        providerConfigs.put("openai", openai);
        
        // Local provider config
        ProviderConfig local = new ProviderConfig(
            "local", 2, 0.5, 500, 5, true,
            Map.of("maxTokens", 4096, "supportsStreaming", true),
            List.of("local-model"),
            ProviderSelectionCriteria.COST_OPTIMIZED
        );
        providerConfigs.put("local", local);
    }
    
    /**
     * Routes a request to the best provider.
     * @param request The routing request
     * @return CompletableFuture with the selected provider
     */
    public CompletableFuture<String> routeRequest(RoutingRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get available providers
                List<ProviderConfig> availableProviders = getAvailableProviders(request);
                
                if (availableProviders.isEmpty()) {
                    LOG.warning("No available providers for request: " + request.getRequestId());
                    return null;
                }
                
                // Select provider using strategy
                String selectedProvider = selectProvider(availableProviders, request);
                
                if (selectedProvider == null) {
                    LOG.warning("No provider selected for request: " + request.getRequestId());
                    return null;
                }
                
                // Check failover
                String failoverProvider = failoverManager.getFailoverProvider(selectedProvider);
                if (failoverProvider != null) {
                    selectedProvider = failoverProvider;
                    LOG.info("Using failover provider: " + selectedProvider + " for request: " + request.getRequestId());
                }
                
                // Increment connections
                loadBalancer.incrementConnections(selectedProvider);
                
                return selectedProvider;
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to route request: " + request.getRequestId(), e);
                return null;
            }
        });
    }
    
    /**
     * Gets available providers for request.
     * @param request The routing request
     * @return Available providers
     */
    private List<ProviderConfig> getAvailableProviders(RoutingRequest request) {
        List<ProviderConfig> available = new ArrayList<>();
        
        for (ProviderConfig provider : providerConfigs.values()) {
            if (provider.isEnabled() && provider.getSupportedModels().contains(request.getModelId())) {
                available.add(provider);
            }
        }
        
        return available;
    }
    
    /**
     * Selects provider using strategy.
     * @param providers Available providers
     * @param request The routing request
     * @return Selected provider
     */
    private String selectProvider(List<ProviderConfig> providers, RoutingRequest request) {
        // Use optimizer for selection
        String optimizedProvider = optimizer.getBestProvider(providers, request.getParameters().containsKey("criteria") ? 
            ProviderSelectionCriteria.valueOf(request.getParameters().get("criteria").toString()) : 
            ProviderSelectionCriteria.DEFAULT);
        
        if (optimizedProvider != null) {
            return optimizedProvider;
        }
        
        // Fallback to first strategy
        if (!strategies.isEmpty()) {
            return strategies.get(0).selectProvider(providers, request);
        }
        
        return null;
    }
    
    /**
     * Records request completion.
     * @param providerId The provider ID
     * @param responseTime The response time
     * @param success Whether successful
     * @param cost The cost
     */
    public void recordRequestCompletion(String providerId, long responseTime, boolean success, double cost) {
        // Update load balancer
        loadBalancer.decrementConnections(providerId);
        
        // Update failover manager
        failoverManager.updateProviderHealth(providerId, success);
        
        // Update optimizer
        optimizer.recordPerformance(providerId, responseTime, success, cost);
        
        // Update metrics
        metrics.recordRequest(providerId, success);
    }
    
    /**
     * Adds a provider configuration.
     * @param config The provider configuration
     */
    public void addProviderConfig(ProviderConfig config) {
        providerConfigs.put(config.getProviderId(), config);
        LOG.info("Provider config added: " + config.getProviderId());
    }
    
    /**
     * Removes a provider configuration.
     * @param providerId The provider ID
     */
    public void removeProviderConfig(String providerId) {
        providerConfigs.remove(providerId);
        LOG.info("Provider config removed: " + providerId);
    }
    
    /**
     * Gets provider configuration.
     * @param providerId The provider ID
     * @return The provider configuration
     */
    public ProviderConfig getProviderConfig(String providerId) {
        return providerConfigs.get(providerId);
    }
    
    /**
     * Gets all provider configurations.
     * @return All provider configurations
     */
    public Map<String, ProviderConfig> getAllProviderConfigs() {
        return new HashMap<>(providerConfigs);
    }
    
    /**
     * Gets routing statistics.
     * @return Routing statistics
     */
    public Map<String, Object> getRoutingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", metrics.getTotalRequests());
        stats.put("totalFailures", metrics.getTotalFailures());
        stats.put("overallSuccessRate", metrics.getOverallSuccessRate());
        stats.put("providerCount", providerConfigs.size());
        stats.put("strategyCount", strategies.size());
        
        // Provider-specific stats
        Map<String, Object> providerStats = new HashMap<>();
        for (String providerId : providerConfigs.keySet()) {
            providerStats.put(providerId, metrics.getProviderStats(providerId));
        }
        stats.put("providerStats", providerStats);
        
        return stats;
    }
    
    /**
     * Gets load balancer.
     * @return Load balancer
     */
    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }
    
    /**
     * Gets failover manager.
     * @return Failover manager
     */
    public FailoverManager getFailoverManager() {
        return failoverManager;
    }
    
    /**
     * Gets optimizer.
     * @return Provider optimizer
     */
    public ProviderOptimizer getOptimizer() {
        return optimizer;
    }
    
    /**
     * Gets metrics.
     * @return Routing metrics
     */
    public RoutingMetrics getMetrics() {
        return metrics;
    }
}
