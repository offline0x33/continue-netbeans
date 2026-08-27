package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MultiProviderRouterTest {

    private static MultiProviderRouter.ProviderConfig provider(String id, boolean enabled, double weight) {
        return new MultiProviderRouter.ProviderConfig(id, 1, weight, 60, 10, enabled,
                Map.of("chat", true), List.of("model"), MultiProviderRouter.ProviderSelectionCriteria.DEFAULT);
    }

    @Test
    void routingRequestPreservesInputsAndDefaultsNullParameters() {
        MultiProviderRouter.RoutingRequest request =
                new MultiProviderRouter.RoutingRequest("r1", "model", "chat", null, 5);
        assertEquals("r1", request.getRequestId());
        assertEquals("model", request.getModelId());
        assertEquals("chat", request.getTaskType());
        assertTrue(request.getParameters().isEmpty());
        assertEquals(5, request.getPriority());
        assertTrue(request.getTimestamp() > 0);
    }

    @Test
    void loadBalancerHandlesRoundRobinLeastConnectionsAndOverload() {
        MultiProviderRouter.LoadBalancer balancer = new MultiProviderRouter.LoadBalancer();
        List<MultiProviderRouter.ProviderConfig> providers = List.of(provider("a", true, 1), provider("b", true, 1));

        assertEquals("a", balancer.selectRoundRobin(providers));
        assertEquals("b", balancer.selectRoundRobin(providers));
        assertEquals("a", balancer.selectLeastConnections(providers));

        balancer.incrementConnections("a");
        assertEquals("b", balancer.selectLeastConnections(providers));
        balancer.decrementConnections("a");
        balancer.decrementConnections("missing");
        assertEquals(0, balancer.selectLeastConnections(List.of(provider("a", false, 1)) == null ? providers : providers).length());
    }

    @Test
    void loadBalancerReturnsNullForEmptyOrDisabledProviders() {
        MultiProviderRouter.LoadBalancer balancer = new MultiProviderRouter.LoadBalancer();
        assertNull(balancer.selectRoundRobin(List.of()));
        assertNull(balancer.selectWeightedRoundRobin(List.of()));
        assertNull(balancer.selectLeastConnections(List.of()));
        assertNull(balancer.selectRoundRobin(List.of(provider("a", false, 1))));
        assertNull(balancer.selectWeightedRoundRobin(List.of(provider("a", false, 1))));
        assertNull(balancer.selectLeastConnections(List.of(provider("a", false, 1))));
    }

    @Test
    void weightedRoundRobinAndZeroWeightsExerciseSelectionBranches() {
        MultiProviderRouter.LoadBalancer balancer = new MultiProviderRouter.LoadBalancer();
        assertNull(balancer.selectWeightedRoundRobin(List.of(provider("a", true, 0), provider("b", true, 0))));
        String selected = balancer.selectWeightedRoundRobin(List.of(provider("a", true, 1), provider("b", false, 2)));
        assertEquals("a", selected);
    }

    @Test
    void providerLoadTracksConnectionsAndOverloadThreshold() {
        MultiProviderRouter.ProviderLoad load = new MultiProviderRouter.ProviderLoad();
        assertFalse(load.isOverloaded());
        for (int i = 0; i < 11; i++) {
            load.incrementConnections();
        }
        assertEquals(11, load.getCurrentConnections());
        assertTrue(load.getRequestsPerMinute() > 0);
        assertTrue(load.getLastRequestTime() > 0);
        assertTrue(load.isOverloaded());
        load.decrementConnections();
        assertEquals(10, load.getCurrentConnections());
        for (int i = 0; i < 20; i++) {
            load.decrementConnections();
        }
        assertEquals(0, load.getCurrentConnections());
    }

    @Test
    void providerHealthRequiresThreeFailuresAndTracksRecovery() {
        MultiProviderRouter.ProviderHealth health = new MultiProviderRouter.ProviderHealth();
        assertTrue(health.isHealthy());
        health.updateHealth(false);
        health.updateHealth(false);
        assertTrue(health.isHealthy());
        health.updateHealth(false);
        assertFalse(health.isHealthy());
        assertEquals(3, health.getConsecutiveFailures());
        health.updateHealth(true);
        assertTrue(health.isHealthy());
        assertEquals(1, health.getConsecutiveSuccesses());
        assertEquals(0, health.getConsecutiveFailures());
        assertTrue(health.getLastCheck() > 0);
    }

    @Test
    void failoverManagerSelectsHealthyFallback() {
        MultiProviderRouter.FailoverManager manager = new MultiProviderRouter.FailoverManager();
        assertNull(manager.getFailoverProvider("openai"));
        manager.updateProviderHealth("openai", false);
        manager.updateProviderHealth("openai", false);
        manager.updateProviderHealth("openai", false);
        assertEquals("local", manager.getFailoverProvider("openai"));
        assertNotNull(manager.getProviderHealth("openai"));
        assertNull(manager.getProviderHealth("missing"));
        assertNull(manager.getFailoverProvider("unknown"));
    }

    @Test
    void optimizerReturnsNullForEmptyProvidersAndChoosesRecordedPerformance() {
        MultiProviderRouter.ProviderOptimizer optimizer = new MultiProviderRouter.ProviderOptimizer();
        assertNull(optimizer.getBestProvider(List.of(), MultiProviderRouter.ProviderSelectionCriteria.DEFAULT));
        List<MultiProviderRouter.ProviderConfig> providers = List.of(provider("cheap", true, 1), provider("fast", true, 1));
        assertNull(optimizer.getBestProvider(List.of(provider("disabled", false, 1)), MultiProviderRouter.ProviderSelectionCriteria.DEFAULT));
        assertEquals("cheap", optimizer.getBestProvider(providers, MultiProviderRouter.ProviderSelectionCriteria.DEFAULT));
        optimizer.recordPerformance("cheap", 50, true, 0.001);
        optimizer.recordPerformance("cheap", 40, true, 0.002);
        optimizer.recordPerformance("fast", 10, false, 0.100);
        assertNotNull(optimizer.getBestProvider(providers, MultiProviderRouter.ProviderSelectionCriteria.COST_OPTIMIZED));
        assertNotNull(optimizer.getBestProvider(providers, MultiProviderRouter.ProviderSelectionCriteria.SPEED_OPTIMIZED));
        assertNotNull(optimizer.getBestProvider(providers, MultiProviderRouter.ProviderSelectionCriteria.QUALITY_OPTIMIZED));
        assertNotNull(optimizer.getBestProvider(providers, MultiProviderRouter.ProviderSelectionCriteria.LOAD_BALANCED));
    }
}
