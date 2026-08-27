package com.bajinho.continuebeans.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
        public ProviderConfig(String providerId, int priority, double weight, int maxRequestsPerMinute,
                int maxConcurrentRequests, boolean enabled, Map<String, Object> capabilities,
                List<String> supportedModels, ProviderSelectionCriteria selectionCriteria) {
            this.providerId = providerId; this.priority = priority; this.weight = weight;
            this.maxRequestsPerMinute = maxRequestsPerMinute; this.maxConcurrentRequests = maxConcurrentRequests;
            this.enabled = enabled; this.capabilities = capabilities != null ? capabilities : new HashMap<>();
            this.supportedModels = supportedModels != null ? supportedModels : new ArrayList<>();
            this.selectionCriteria = selectionCriteria != null ? selectionCriteria : ProviderSelectionCriteria.DEFAULT;
        }
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

    public enum ProviderSelectionCriteria { DEFAULT, COST_OPTIMIZED, SPEED_OPTIMIZED, QUALITY_OPTIMIZED, LOAD_BALANCED, FAILOVER_SAFE }

    public interface ProviderStrategy { String selectProvider(List<ProviderConfig> providers, RoutingRequest request); String getStrategyName(); }

    public static class RoutingRequest {
        private final String requestId; private final String modelId; private final String taskType;
        private final Map<String, Object> parameters; private final long timestamp; private final int priority;
        public RoutingRequest(String requestId, String modelId, String taskType, Map<String, Object> parameters, int priority) {
            this.requestId = requestId; this.modelId = modelId; this.taskType = taskType;
            this.parameters = parameters != null ? parameters : new HashMap<>(); this.timestamp = System.currentTimeMillis(); this.priority = priority;
        }
        public String getRequestId() { return requestId; } public String getModelId() { return modelId; }
        public String getTaskType() { return taskType; } public Map<String, Object> getParameters() { return parameters; }
        public long getTimestamp() { return timestamp; } public int getPriority() { return priority; }
    }

    public static class LoadBalancer {
        private final Map<String, ProviderLoad> providerLoads = new ConcurrentHashMap<>();
        private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
        public String selectRoundRobin(List<ProviderConfig> providers) {
            if (providers.isEmpty()) return null;
            List<ProviderConfig> enabledProviders = new ArrayList<>();
            for (ProviderConfig provider : providers) if (provider.isEnabled() && !isOverloaded(provider.getProviderId())) enabledProviders.add(provider);
            if (enabledProviders.isEmpty()) return null;
            int index = roundRobinIndex.getAndIncrement() % enabledProviders.size();
            return enabledProviders.get(index).getProviderId();
        }
        public String selectWeightedRoundRobin(List<ProviderConfig> providers) {
            if (providers.isEmpty()) return null;
            double totalWeight = 0;
            for (ProviderConfig provider : providers) if (provider.isEnabled() && !isOverloaded(provider.getProviderId())) totalWeight += provider.getWeight();
            if (totalWeight == 0) return null;
            double random = Math.random() * totalWeight, currentWeight = 0;
            for (ProviderConfig provider : providers) if (provider.isEnabled() && !isOverloaded(provider.getProviderId())) {
                currentWeight += provider.getWeight(); if (random <= currentWeight) return provider.getProviderId();
            }
            return providers.get(0).getProviderId();
        }
        public String selectLeastConnections(List<ProviderConfig> providers) {
            if (providers.isEmpty()) return null;
            String selectedProvider = null; int minConnections = Integer.MAX_VALUE;
            for (ProviderConfig provider : providers) if (provider.isEnabled() && !isOverloaded(provider.getProviderId())) {
                ProviderLoad load = providerLoads.get(provider.getProviderId());
                int connections = load != null ? load.getCurrentConnections() : 0;
                if (connections < minConnections) { minConnections = connections; selectedProvider = provider.getProviderId(); }
            }
            return selectedProvider;
        }
        public void incrementConnections(String providerId) { providerLoads.computeIfAbsent(providerId, k -> new ProviderLoad()).incrementConnections(); }
        public void decrementConnections(String providerId) { ProviderLoad load = providerLoads.get(providerId); if (load != null) load.decrementConnections(); }
        private boolean isOverloaded(String providerId) { ProviderLoad load = providerLoads.get(providerId); return load != null && load.isOverloaded(); }
    }

    public static class ProviderLoad {
        private int currentConnections; private long lastRequestTime; private int requestsPerMinute;
        private final long[] requestTimestamps = new long[60]; private int requestIndex;
        public void incrementConnections() { currentConnections++; updateRequestRate(); }
        public void decrementConnections() { if (currentConnections > 0) currentConnections--; }
        private void updateRequestRate() {
            long now = System.currentTimeMillis(); requestTimestamps[requestIndex] = now; requestIndex = (requestIndex + 1) % 60;
            int count = 0; for (long timestamp : requestTimestamps) if (now - timestamp < 60000) count++;
            requestsPerMinute = count; lastRequestTime = now;
        }
        public boolean isOverloaded() { return currentConnections > 10 || requestsPerMinute > 100; }
        public int getCurrentConnections() { return currentConnections; } public long getLastRequestTime() { return lastRequestTime; }
        public int getRequestsPerMinute() { return requestsPerMinute; }
    }

    public static class FailoverManager {
        private final Map<String, ProviderHealth> providerHealth = new ConcurrentHashMap<>();
        private final Map<String, List<String>> failoverChains = new ConcurrentHashMap<>();
        public FailoverManager() {
            failoverChains.put("openai", List.of("local", "anthropic"));
            failoverChains.put("local", List.of("openai", "anthropic"));
            failoverChains.put("anthropic", List.of("openai", "local"));
        }
        public String getFailoverProvider(String primaryProvider) {
            ProviderHealth health = providerHealth.get(primaryProvider);
            if (health == null || health.isHealthy()) return null;
            List<String> chain = failoverChains.get(primaryProvider);
            if (chain != null) for (String providerId : chain) {
                ProviderHealth candidate = providerHealth.get(providerId);
                if (candidate == null || candidate.isHealthy()) return providerId;
            }
            return null;
        }
        public void updateProviderHealth(String providerId, boolean healthy) {
            providerHealth.computeIfAbsent(providerId, k -> new ProviderHealth()).updateHealth(healthy);
        }
        public ProviderHealth getProviderHealth(String providerId) { return providerHealth.get(providerId); }
    }

    public static class ProviderHealth {
        private boolean healthy = true; private long lastCheck = System.currentTimeMillis();
        private int consecutiveFailures; private int consecutiveSuccesses;
        public void updateHealth(boolean isHealthy) {
            lastCheck = System.currentTimeMillis();
            if (isHealthy) { consecutiveSuccesses++; consecutiveFailures = 0; healthy = true; }
            else { consecutiveFailures++; consecutiveSuccesses = 0; if (consecutiveFailures >= 3) healthy = false; }
        }
        public boolean isHealthy() { return healthy; } public long getLastCheck() { return lastCheck; }
        public int getConsecutiveFailures() { return consecutiveFailures; } public int getConsecutiveSuccesses() { return consecutiveSuccesses; }
    }

    public static class ProviderOptimizer {
        private final Map<String, ProviderPerformance> providerPerformance = new ConcurrentHashMap<>();
        public void recordPerformance(String providerId, long responseTime, boolean success, double cost) {
            providerPerformance.computeIfAbsent(providerId, k -> new ProviderPerformance()).recordPerformance(responseTime, success, cost);
        }
        public String getBestProvider(List<ProviderConfig> providers, ProviderSelectionCriteria criteria) {
            if (providers.isEmpty()) return null;
            String bestProvider = null; double bestScore = Double.NEGATIVE_INFINITY;
            for (ProviderConfig provider : providers) if (provider.isEnabled()) {
                double score = calculateScore(provider, providerPerformance.get(provider.getProviderId()), criteria);
                if (score > bestScore) { bestScore = score; bestProvider = provider.getProviderId(); }
            }
            return bestProvider;
        }
        private double calculateScore(ProviderConfig provider, ProviderPerformance performance, ProviderSelectionCriteria criteria) {
            if (performance == null) return 0.0;
            return switch (criteria) {
                case COST_OPTIMIZED -> -performance.getAverageCost(); case SPEED_OPTIMIZED -> -performance.getAverageResponseTime();
                case QUALITY_OPTIMIZED -> performance.getSuccessRate(); case LOAD_BALANCED -> provider.getWeight();
                case FAILOVER_SAFE -> performance.getSuccessRate(); default -> provider.getPriority();
            };
        }
    }

    public static class ProviderPerformance {
        private long totalResponseTime; private int totalRequests; private int successfulRequests; private double totalCost;
        public synchronized void recordPerformance(long responseTime, boolean success, double cost) { totalResponseTime += responseTime; totalRequests++; if (success) successfulRequests++; totalCost += cost; }
        public double getAverageResponseTime() { return totalRequests == 0 ? 0 : (double) totalResponseTime / totalRequests; }
        public double getSuccessRate() { return totalRequests == 0 ? 0 : (double) successfulRequests / totalRequests; }
        public double getAverageCost() { return totalRequests == 0 ? 0 : totalCost / totalRequests; }
    }

    public static class RoutingMetrics {
        private final Map<String, Long> requestCounts = new ConcurrentHashMap<>();
        public void recordRequest(String providerId) { requestCounts.merge(providerId, 1L, Long::sum); }
        public long getRequestCount(String providerId) { return requestCounts.getOrDefault(providerId, 0L); }
        public Map<String, Long> getRequestCounts() { return new HashMap<>(requestCounts); }
    }

    public MultiProviderRouter() {
        providerConfigs = new ConcurrentHashMap<>(); strategies = new ArrayList<>(); loadBalancer = new LoadBalancer();
        failoverManager = new FailoverManager(); optimizer = new ProviderOptimizer(); metrics = new RoutingMetrics();
    }
    public static synchronized MultiProviderRouter getInstance() { if (instance == null) instance = new MultiProviderRouter(); return instance; }
    public void registerProvider(ProviderConfig config) { providerConfigs.put(config.getProviderId(), config); }
    public void unregisterProvider(String providerId) { providerConfigs.remove(providerId); }
    public ProviderConfig getProviderConfig(String providerId) { return providerConfigs.get(providerId); }
    public List<ProviderConfig> getProviders() { return new ArrayList<>(providerConfigs.values()); }
    public void addStrategy(ProviderStrategy strategy) { strategies.add(strategy); }
    public String route(RoutingRequest request) {
        List<ProviderConfig> providers = getProviders();
        if (providers.isEmpty()) return null;
        String selected = strategies.isEmpty() ? loadBalancer.selectRoundRobin(providers) : strategies.get(0).selectProvider(providers, request);
        if (selected != null) metrics.recordRequest(selected);
        return selected;
    }
    public String routeWithFailover(RoutingRequest request) {
        String selected = route(request); if (selected == null) return null;
        String failover = failoverManager.getFailoverProvider(selected); return failover != null ? failover : selected;
    }
    public LoadBalancer getLoadBalancer() { return loadBalancer; }
    public FailoverManager getFailoverManager() { return failoverManager; }
    public ProviderOptimizer getOptimizer() { return optimizer; }
    public RoutingMetrics getMetrics() { return metrics; }
}
