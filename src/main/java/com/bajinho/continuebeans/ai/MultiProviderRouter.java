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
            this.requestId=requestId; this.modelId=modelId; this.taskType=taskType; this.parameters=parameters!=null?parameters:new HashMap<>();
            this.timestamp=System.currentTimeMillis(); this.priority=priority;
        }
        public String getRequestId(){return requestId;} public String getModelId(){return modelId;} public String getTaskType(){return taskType;}
        public Map<String,Object> getParameters(){return parameters;} public long getTimestamp(){return timestamp;} public int getPriority(){return priority;}
    }
    public static class LoadBalancer {
        private final Map<String,ProviderLoad> providerLoads=new ConcurrentHashMap<>(); private final AtomicInteger roundRobinIndex=new AtomicInteger(0);
        public LoadBalancer() {}
        public String selectRoundRobin(List<ProviderConfig> providers){ if(providers.isEmpty())return null; List<ProviderConfig> e=new ArrayList<>(); for(ProviderConfig p:providers)if(p.isEnabled()&&!isOverloaded(p.getProviderId()))e.add(p); if(e.isEmpty())return null; return e.get(roundRobinIndex.getAndIncrement()%e.size()).getProviderId(); }
        public String selectWeightedRoundRobin(List<ProviderConfig> providers){ if(providers.isEmpty())return null; double total=0; for(ProviderConfig p:providers)if(p.isEnabled()&&!isOverloaded(p.getProviderId()))total+=p.getWeight(); if(total==0)return null; double random=Math.random()*total,current=0; for(ProviderConfig p:providers)if(p.isEnabled()&&!isOverloaded(p.getProviderId())){current+=p.getWeight(); if(random<=current)return p.getProviderId();} return providers.get(0).getProviderId(); }
        public String selectLeastConnections(List<ProviderConfig> providers){ if(providers.isEmpty())return null; String selected=null; int min=Integer.MAX_VALUE; for(ProviderConfig p:providers)if(p.isEnabled()&&!isOverloaded(p.getProviderId())){ProviderLoad l=providerLoads.get(p.getProviderId()); int c=l!=null?l.getCurrentConnections():0; if(c<min){min=c;selected=p.getProviderId();}} return selected; }
        public void incrementConnections(String providerId){providerLoads.computeIfAbsent(providerId,k->new ProviderLoad()).incrementConnections();}
        public void decrementConnections(String providerId){ProviderLoad l=providerLoads.get(providerId);if(l!=null)l.decrementConnections();}
        private boolean isOverloaded(String providerId){ProviderLoad l=providerLoads.get(providerId);return l!=null&&l.isOverloaded();}
    }
    public static class ProviderLoad {
        private int currentConnections; private long lastRequestTime; private int requestsPerMinute; private final long[] requestTimestamps=new long[60]; private int requestIndex;
        public ProviderLoad(){currentConnections=0;lastRequestTime=0;requestsPerMinute=0;requestIndex=0;}
        public void incrementConnections(){currentConnections++;updateRequestRate();}
        public void decrementConnections(){if(currentConnections>0)currentConnections--;}
        private void updateRequestRate(){long now=System.currentTimeMillis();requestTimestamps[requestIndex]=now;requestIndex=(requestIndex+1)%60;int count=0;for(long t:requestTimestamps)if(now-t<60000)count++;requestsPerMinute=count;lastRequestTime=now;}
        public boolean isOverloaded(){return currentConnections>10||requestsPerMinute>100;}
        public int getCurrentConnections(){return currentConnections;} public long getLastRequestTime(){return lastRequestTime;} public int getRequestsPerMinute(){return requestsPerMinute;}
    }
    public static class FailoverManager {
        private final Map<String,ProviderHealth> providerHealth=new ConcurrentHashMap<>(); private final Map<String,List<String>> failoverChains=new ConcurrentHashMap<>();
        public FailoverManager(){failoverChains.put("openai",List.of("local","anthropic"));failoverChains.put("local",List.of("openai","anthropic"));failoverChains.put("anthropic",List.of("openai","local"));}
        public String getFailoverProvider(String primaryProvider){ProviderHealth health=providerHealth.get(primaryProvider);if(health==null||health.isHealthy())return null;List<String> chain=failoverChains.get(primaryProvider);if(chain!=null)for(String id:chain){ProviderHealth candidate=providerHealth.get(id);if(candidate==null||candidate.isHealthy())return id;}return null;}
        public void updateProviderHealth(String providerId,boolean healthy){providerHealth.computeIfAbsent(providerId,k->new ProviderHealth()).updateHealth(healthy);}
        public ProviderHealth getProviderHealth(String providerId){return providerHealth.get(providerId);}
    }
    public static class ProviderHealth {
        private boolean healthy=true; private long lastCheck=System.currentTimeMillis(); private int consecutiveFailures; private int consecutiveSuccesses;
        public void updateHealth(boolean isHealthy){lastCheck=System.currentTimeMillis();if(isHealthy){consecutiveSuccesses++;consecutiveFailures=0;healthy=true;}else{consecutiveFailures++;consecutiveSuccesses=0;if(consecutiveFailures>=3)healthy=false;}}
        public boolean isHealthy(){return healthy;} public long getLastCheck(){return lastCheck;} public int getConsecutiveFailures(){return consecutiveFailures;} public int getConsecutiveSuccesses(){return consecutiveSuccesses;}
    }
    public static class ProviderOptimizer {
        private final Map<String,ProviderPerformance> providerPerformance=new ConcurrentHashMap<>();
        public void recordPerformance(String providerId,long responseTime,boolean success,double cost){providerPerformance.computeIfAbsent(providerId,k->new ProviderPerformance()).recordPerformance(responseTime,success,cost);}
        public String getBestProvider(List<ProviderConfig> providers,ProviderSelectionCriteria criteria){if(providers.isEmpty())return null;String best=null;double bestScore=Double.NEGATIVE_INFINITY;for(ProviderConfig p:providers)if(p.isEnabled()){ProviderPerformance perf=providerPerformance.get(p.getProviderId());double score=calculateScore(p,perf,criteria);if(score>bestScore){bestScore=score;best=p.getProviderId();}}return best;}
        private double calculateScore(ProviderConfig p,ProviderPerformance perf,ProviderSelectionCriteria c){if(perf==null)return 0.0;switch(c){case COST_OPTIMIZED:return -perf.getAverageCost();case SPEED_OPTIMIZED:return -perf.getAverageResponseTime();case QUALITY_OPTIMIZED:return perf.getSuccessRate();case LOAD_BALANCED:return 1.0/(perf.getCurrentLoad()+1);default:return perf.getSuccessRate()*0.5+(1.0/(perf.getAverageResponseTime()+1))*0.3+(1.0/(perf.getAverageCost()+0.01))*0.2;}}
    }
    public static class ProviderPerformance {
        private long totalResponseTime; private int totalRequests; private int successfulRequests; private double totalCost; private int currentLoad; private long lastUpdate;
        public ProviderPerformance(){lastUpdate=System.currentTimeMillis();}
        public void recordPerformance(long responseTime,boolean success,double cost){totalRequests++;if(success)successfulRequests++;totalResponseTime+=responseTime;totalCost+=cost;lastUpdate=System.currentTimeMillis();}
        public double getAverageResponseTime(){return totalRequests>0?(double)totalResponseTime/totalRequests:0.0;}
        public double getSuccessRate(){return totalRequests>0?(double)successfulRequests/totalRequests:0.0;}
        public double getAverageCost(){return totalRequests>0?totalCost/totalRequests:0.0;}
        public long getTotalResponseTime(){return totalResponseTime;} public int getTotalRequests(){return totalRequests;} public int getSuccessfulRequests(){return successfulRequests;} public double getTotalCost(){return totalCost;} public int getCurrentLoad(){return currentLoad;} public long getLastUpdate(){return lastUpdate;}
    }
    public static class RoutingMetrics {
        private final Map<String,AtomicInteger> providerRequestCounts=new ConcurrentHashMap<>(); private final Map<String,AtomicInteger> providerSuccessCounts=new ConcurrentHashMap<>(); private final Map<String,AtomicInteger> providerFailureCounts=new ConcurrentHashMap<>(); private final AtomicInteger totalRequests=new AtomicInteger(); private final AtomicInteger totalFailures=new AtomicInteger();
        public void recordRequest(String providerId,boolean success){totalRequests.incrementAndGet();providerRequestCounts.computeIfAbsent(providerId,k->new AtomicInteger()).incrementAndGet();if(success)providerSuccessCounts.computeIfAbsent(providerId,k->new AtomicInteger()).incrementAndGet();else{totalFailures.incrementAndGet();providerFailureCounts.computeIfAbsent(providerId,k->new AtomicInteger()).incrementAndGet();}}
        public Map<String,Object> getProviderStats(String providerId){Map<String,Object>s=new HashMap<>();int r=providerRequestCounts.getOrDefault(providerId,new AtomicInteger()).get();int ok=providerSuccessCounts.getOrDefault(providerId,new AtomicInteger()).get();s.put("requests",r);s.put("successes",ok);s.put("failures",providerFailureCounts.getOrDefault(providerId,new AtomicInteger()).get());s.put("successRate",r>0?(double)ok/r:0.0);return s;}
        public int getTotalRequests(){return totalRequests.get();} public int getTotalFailures(){return totalFailures.get();} public double getOverallSuccessRate(){int total=totalRequests.get();return total>0?(double)(total-totalFailures.get())/total:0.0;}
    }
    private MultiProviderRouter(){providerConfigs=new ConcurrentHashMap<>();strategies=new ArrayList<>();loadBalancer=new LoadBalancer();failoverManager=new FailoverManager();optimizer=new ProviderOptimizer();metrics=new RoutingMetrics();initializeStrategies();initializeProviders();LOG.info("MultiProviderRouter initialized");}
    public static synchronized MultiProviderRouter getInstance(){if(instance==null)instance=new MultiProviderRouter();return instance;}
    private void initializeStrategies(){strategies.add(new ProviderStrategy(){public String selectProvider(List<ProviderConfig> p,RoutingRequest r){return loadBalancer.selectRoundRobin(p);}public String getStrategyName(){return "RoundRobin";}});strategies.add(new ProviderStrategy(){public String selectProvider(List<ProviderConfig> p,RoutingRequest r){return loadBalancer.selectWeightedRoundRobin(p);}public String getStrategyName(){return "WeightedRoundRobin";}});strategies.add(new ProviderStrategy(){public String selectProvider(List<ProviderConfig> p,RoutingRequest r){return loadBalancer.selectLeastConnections(p);}public String getStrategyName(){return "LeastConnections";}});}
    private void initializeProviders(){providerConfigs.put("openai",new ProviderConfig("openai",1,1.0,1000,10,true,Map.of("maxTokens",4096,"supportsStreaming",true),List.of("gpt-4","gpt-3.5-turbo"),ProviderSelectionCriteria.DEFAULT));providerConfigs.put("local",new ProviderConfig("local",2,0.5,500,5,true,Map.of("maxTokens",4096,"supportsStreaming",true),List.of("local-model"),ProviderSelectionCriteria.COST_OPTIMIZED));}
    public CompletableFuture<String> routeRequest(RoutingRequest request){return CompletableFuture.supplyAsync(()->{try{List<ProviderConfig> available=getAvailableProviders(request);if(available.isEmpty()){LOG.warning("No available providers for request: "+request.getRequestId());return null;}String selected=selectProvider(available,request);if(selected==null){LOG.warning("No provider selected for request: "+request.getRequestId());return null;}String failover=failoverManager.getFailoverProvider(selected);if(failover!=null){selected=failover;LOG.info("Using failover provider: "+selected+" for request: "+request.getRequestId());}loadBalancer.incrementConnections(selected);return selected;}catch(Exception e){LOG.log(Level.SEVERE,"Failed to route request: "+request.getRequestId(),e);return null;}});}
    private List<ProviderConfig> getAvailableProviders(RoutingRequest request){List<ProviderConfig>available=new ArrayList<>();for(ProviderConfig p:providerConfigs.values())if(p.isEnabled()&&p.getSupportedModels().contains(request.getModelId()))available.add(p);return available;}
    private String selectProvider(List<ProviderConfig> providers,RoutingRequest request){String optimized=optimizer.getBestProvider(providers,request.getParameters().containsKey("criteria")?ProviderSelectionCriteria.valueOf(request.getParameters().get("criteria").toString()):ProviderSelectionCriteria.DEFAULT);if(optimized!=null)return optimized;if(!strategies.isEmpty())return strategies.get(0).selectProvider(providers,request);return null;}
    public void recordRequestCompletion(String providerId,long responseTime,boolean success,double cost){loadBalancer.decrementConnections(providerId);failoverManager.updateProviderHealth(providerId,success);optimizer.recordPerformance(providerId,responseTime,success,cost);metrics.recordRequest(providerId,success);}
    public void addProviderConfig(ProviderConfig config){providerConfigs.put(config.getProviderId(),config);LOG.info("Provider config added: "+config.getProviderId());}
    public void removeProviderConfig(String providerId){providerConfigs.remove(providerId);LOG.info("Provider config removed: "+providerId);}
    public ProviderConfig getProviderConfig(String providerId){return providerConfigs.get(providerId);}
    public Map<String,ProviderConfig> getAllProviderConfigs(){return new HashMap<>(providerConfigs);}
    public Map<String,Object> getRoutingStatistics(){Map<String,Object>stats=new HashMap<>();stats.put("totalRequests",metrics.getTotalRequests());stats.put("totalFailures",metrics.getTotalFailures());stats.put("overallSuccessRate",metrics.getOverallSuccessRate());stats.put("providerCount",providerConfigs.size());stats.put("strategyCount",strategies.size());Map<String,Object>providerStats=new HashMap<>();for(String id:providerConfigs.keySet())providerStats.put(id,metrics.getProviderStats(id));stats.put("providerStats",providerStats);return stats;}
    public LoadBalancer getLoadBalancer(){return loadBalancer;} public FailoverManager getFailoverManager(){return failoverManager;} public ProviderOptimizer getOptimizer(){return optimizer;} public RoutingMetrics getMetrics(){return metrics;}
}
