package com.bajinho.continuebeans.automation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Advanced workflow engine with parallel execution, dependencies,
 * error handling, retry logic, and workflow orchestration.
 * 
 * @author Continue Beans Team
 */
public class WorkflowEngine {
    
    private static final Logger LOG = Logger.getLogger(WorkflowEngine.class.getName());
    
    private static WorkflowEngine instance;
    
    private final Map<String, Workflow> workflows;
    private final Map<String, WorkflowExecution> activeExecutions;
    private final List<WorkflowListener> listeners;
    private final ExecutorService executor;
    private final WorkflowConfiguration config;
    
    /**
     * Workflow configuration.
     */
    public static class WorkflowConfiguration {
        private final int maxConcurrentWorkflows;
        private final long defaultTimeout;
        private final boolean enableRetry;
        private final int maxRetries;
        private final long retryDelay;
        private final boolean enableParallelExecution;
        
        public WorkflowConfiguration(int maxConcurrentWorkflows, long defaultTimeout,
                                   boolean enableRetry, int maxRetries, long retryDelay,
                                   boolean enableParallelExecution) {
            this.maxConcurrentWorkflows = maxConcurrentWorkflows;
            this.defaultTimeout = defaultTimeout;
            this.enableRetry = enableRetry;
            this.maxRetries = maxRetries;
            this.retryDelay = retryDelay;
            this.enableParallelExecution = enableParallelExecution;
        }
        
        /**
         * Creates default configuration.
         * @return Default WorkflowConfiguration
         */
        public static WorkflowConfiguration getDefault() {
            return new WorkflowConfiguration(
                10,        // max 10 concurrent workflows
                300000,    // 5 minute default timeout
                true,      // enable retry
                3,         // max 3 retries
                1000,      // 1 second retry delay
                true       // enable parallel execution
            );
        }
        
        // Getters
        public int getMaxConcurrentWorkflows() { return maxConcurrentWorkflows; }
        public long getDefaultTimeout() { return defaultTimeout; }
        public boolean isEnableRetry() { return enableRetry; }
        public int getMaxRetries() { return maxRetries; }
        public long getRetryDelay() { return retryDelay; }
        public boolean isEnableParallelExecution() { return enableParallelExecution; }
    }
    
    /**
     * Represents a workflow definition.
     */
    public static class Workflow {
        private final String workflowId;
        private final String name;
        private final String description;
        private final List<WorkflowStep> steps;
        private final Map<String, Object> defaultContext;
        private final WorkflowExecutionMode executionMode;
        private final long timeout;
        private final ErrorHandlingStrategy errorHandling;
        
        public Workflow(String workflowId, String name, String description,
                       List<WorkflowStep> steps, Map<String, Object> defaultContext,
                       WorkflowExecutionMode executionMode, long timeout,
                       ErrorHandlingStrategy errorHandling) {
            this.workflowId = workflowId;
            this.name = name;
            this.description = description;
            this.steps = steps != null ? steps : new ArrayList<>();
            this.defaultContext = defaultContext != null ? defaultContext : new HashMap<>();
            this.executionMode = executionMode != null ? executionMode : WorkflowExecutionMode.SEQUENTIAL;
            this.timeout = timeout;
            this.errorHandling = errorHandling != null ? errorHandling : ErrorHandlingStrategy.STOP_ON_ERROR;
        }
        
        // Getters
        public String getWorkflowId() { return workflowId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<WorkflowStep> getSteps() { return steps; }
        public Map<String, Object> getDefaultContext() { return defaultContext; }
        public WorkflowExecutionMode getExecutionMode() { return executionMode; }
        public long getTimeout() { return timeout; }
        public ErrorHandlingStrategy getErrorHandling() { return errorHandling; }
    }
    
    /**
     * Workflow execution mode.
     */
    public enum WorkflowExecutionMode {
        SEQUENTIAL,    // Execute steps one after another
        PARALLEL,      // Execute all steps in parallel
        CONDITIONAL     // Execute based on conditions
    }
    
    /**
     * Error handling strategy.
     */
    public enum ErrorHandlingStrategy {
        STOP_ON_ERROR,        // Stop workflow on first error
        CONTINUE_ON_ERROR,    // Continue with other steps
        RETRY_ON_ERROR,       // Retry failed steps
        COMPENSATE_ON_ERROR   // Execute compensation actions
    }
    
    /**
     * Represents a workflow step.
     */
    public static class WorkflowStep {
        private final String stepId;
        private final String name;
        private final String description;
        private final StepType type;
        private final Map<String, Object> parameters;
        private final List<String> dependencies;
        private final List<String> conditions;
        private final boolean optional;
        private final int retryCount;
        private final long timeout;
        private final List<WorkflowStep> compensationSteps;
        
        public WorkflowStep(String stepId, String name, String description, StepType type,
                           Map<String, Object> parameters, List<String> dependencies,
                           List<String> conditions, boolean optional, int retryCount,
                           long timeout, List<WorkflowStep> compensationSteps) {
            this.stepId = stepId;
            this.name = name;
            this.description = description;
            this.type = type;
            this.parameters = parameters != null ? parameters : new HashMap<>();
            this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
            this.conditions = conditions != null ? conditions : new ArrayList<>();
            this.optional = optional;
            this.retryCount = retryCount;
            this.timeout = timeout;
            this.compensationSteps = compensationSteps != null ? compensationSteps : new ArrayList<>();
        }
        
        // Getters
        public String getStepId() { return stepId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public StepType getType() { return type; }
        public Map<String, Object> getParameters() { return parameters; }
        public List<String> getDependencies() { return dependencies; }
        public List<String> getConditions() { return conditions; }
        public boolean isOptional() { return optional; }
        public int getRetryCount() { return retryCount; }
        public long getTimeout() { return timeout; }
        public List<WorkflowStep> getCompensationSteps() { return compensationSteps; }
    }
    
    /**
     * Step type enumeration.
     */
    public enum StepType {
        TASK,           // Execute a task
        DECISION,       // Make a decision
        PARALLEL,       // Execute parallel branches
        SUB_WORKFLOW,   // Execute another workflow
        DELAY,          // Wait for specified time
        CONDITION,      // Conditional execution
        COMPENSATION    // Compensation action
    }
    
    /**
     * Represents a workflow execution.
     */
    public static class WorkflowExecution {
        private final String executionId;
        private final String workflowId;
        private final Map<String, Object> context;
        private final long startTime;
        private long endTime;
        private WorkflowExecutionStatus status;
        private String errorMessage;
        private final List<StepExecution> stepExecutions;
        private final AtomicInteger completedSteps;
        
        public WorkflowExecution(String executionId, String workflowId, Map<String, Object> context) {
            this.executionId = executionId;
            this.workflowId = workflowId;
            this.context = new HashMap<>(context);
            this.startTime = System.currentTimeMillis();
            this.status = WorkflowExecutionStatus.PENDING;
            this.stepExecutions = new ArrayList<>();
            this.completedSteps = new AtomicInteger(0);
        }
        
        // Getters and setters
        public String getExecutionId() { return executionId; }
        public String getWorkflowId() { return workflowId; }
        public Map<String, Object> getContext() { return context; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public WorkflowExecutionStatus getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }
        public List<StepExecution> getStepExecutions() { return stepExecutions; }
        public int getCompletedSteps() { return completedSteps.get(); }
        
        public void setStatus(WorkflowExecutionStatus status) {
            this.status = status;
            if (status.isCompleted()) {
                this.endTime = System.currentTimeMillis();
            }
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public void addStepExecution(StepExecution stepExecution) {
            stepExecutions.add(stepExecution);
            if (stepExecution.getStatus().isCompleted()) {
                completedSteps.incrementAndGet();
            }
        }
        
        public long getDuration() {
            return endTime - startTime;
        }
        
        public double getProgress() {
            if (stepExecutions.isEmpty()) return 0.0;
            return (double) completedSteps.get() / stepExecutions.size();
        }
    }
    
    /**
     * Workflow execution status.
     */
    public enum WorkflowExecutionStatus {
        PENDING(false),
        RUNNING(false),
        COMPLETED(true),
        FAILED(true),
        CANCELLED(true),
        TIMEOUT(true);
        
        private final boolean completed;
        
        WorkflowExecutionStatus(boolean completed) {
            this.completed = completed;
        }
        
        public boolean isCompleted() {
            return completed;
        }
    }
    
    /**
     * Represents a step execution.
     */
    public static class StepExecution {
        private final String stepId;
        private final long startTime;
        private long endTime;
        private StepExecutionStatus status;
        private String errorMessage;
        private final Map<String, Object> result;
        private int attemptCount;
        
        public StepExecution(String stepId) {
            this.stepId = stepId;
            this.startTime = System.currentTimeMillis();
            this.status = StepExecutionStatus.PENDING;
            this.result = new HashMap<>();
            this.attemptCount = 1;
        }
        
        // Getters and setters
        public String getStepId() { return stepId; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public StepExecutionStatus getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getResult() { return result; }
        public int getAttemptCount() { return attemptCount; }
        
        public void setStatus(StepExecutionStatus status) {
            this.status = status;
            if (status.isCompleted()) {
                this.endTime = System.currentTimeMillis();
            }
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public void incrementAttemptCount() {
            attemptCount++;
        }
        
        public long getDuration() {
            return endTime - startTime;
        }
    }
    
    /**
     * Step execution status.
     */
    public enum StepExecutionStatus {
        PENDING(false),
        RUNNING(false),
        COMPLETED(true),
        FAILED(true),
        SKIPPED(true),
        TIMEOUT(true);
        
        private final boolean completed;
        
        StepExecutionStatus(boolean completed) {
            this.completed = completed;
        }
        
        public boolean isCompleted() {
            return completed;
        }
    }
    
    /**
     * Workflow listener interface.
     */
    public interface WorkflowListener {
        void onWorkflowStarted(String executionId, String workflowId);
        void onWorkflowCompleted(String executionId, String workflowId, boolean success);
        void onWorkflowFailed(String executionId, String workflowId, String error);
        void onWorkflowCancelled(String executionId, String workflowId);
        void onStepStarted(String executionId, String stepId);
        void onStepCompleted(String executionId, String stepId, boolean success);
        void onStepFailed(String executionId, String stepId, String error);
        void onStepSkipped(String executionId, String stepId);
    }
    
    /**
     * Step executor interface.
     */
    public interface StepExecutor {
        CompletableFuture<StepExecutionResult> executeStep(WorkflowStep step, Map<String, Object> context);
        String getSupportedStepType();
    }
    
    /**
     * Step execution result.
     */
    public static class StepExecutionResult {
        private final boolean success;
        private final String errorMessage;
        private final Map<String, Object> result;
        private final boolean shouldRetry;
        
        public StepExecutionResult(boolean success, String errorMessage, Map<String, Object> result, boolean shouldRetry) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.result = result != null ? result : new HashMap<>();
            this.shouldRetry = shouldRetry;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getResult() { return result; }
        public boolean shouldRetry() { return shouldRetry; }
        
        /**
         * Creates a successful result.
         * @param result The result data
         * @return Successful StepExecutionResult
         */
        public static StepExecutionResult success(Map<String, Object> result) {
            return new StepExecutionResult(true, null, result, false);
        }
        
        /**
         * Creates a failed result.
         * @param errorMessage The error message
         * @return Failed StepExecutionResult
         */
        public static StepExecutionResult failure(String errorMessage) {
            return new StepExecutionResult(false, errorMessage, null, false);
        }
        
        /**
         * Creates a failed result that should be retried.
         * @param errorMessage The error message
         * @return Failed StepExecutionResult with retry
         */
        public static StepExecutionResult retry(String errorMessage) {
            return new StepExecutionResult(false, errorMessage, null, true);
        }
    }
    
    /**
     * Private constructor for singleton.
     */
    private WorkflowEngine() {
        this.workflows = new ConcurrentHashMap<>();
        this.activeExecutions = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.executor = Executors.newFixedThreadPool(10);
        this.config = WorkflowConfiguration.getDefault();
        
        initializeDefaultExecutors();
        
        LOG.info("WorkflowEngine initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The WorkflowEngine instance
     */
    public static synchronized WorkflowEngine getInstance() {
        if (instance == null) {
            instance = new WorkflowEngine();
        }
        return instance;
    }
    
    /**
     * Initializes default step executors.
     */
    private void initializeDefaultExecutors() {
        // Task executor
        // TODO: Register default executors
    }
    
    /**
     * Registers a workflow.
     * @param workflow The workflow to register
     */
    public void registerWorkflow(Workflow workflow) {
        workflows.put(workflow.getWorkflowId(), workflow);
        LOG.info("Workflow registered: " + workflow.getWorkflowId());
    }
    
    /**
     * Unregisters a workflow.
     * @param workflowId The workflow ID to unregister
     */
    public void unregisterWorkflow(String workflowId) {
        workflows.remove(workflowId);
        LOG.info("Workflow unregistered: " + workflowId);
    }
    
    /**
     * Executes a workflow.
     * @param workflowId The workflow ID
     * @param context The execution context
     * @return CompletableFuture with the execution result
     */
    public CompletableFuture<Boolean> executeWorkflow(String workflowId, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Workflow workflow = workflows.get(workflowId);
                if (workflow == null) {
                    throw new IllegalArgumentException("Workflow not found: " + workflowId);
                }
                
                String executionId = "exec_" + System.currentTimeMillis() + "_" + workflowId.hashCode();
                
                // Merge default context
                Map<String, Object> fullContext = new HashMap<>(workflow.getDefaultContext());
                fullContext.putAll(context);
                
                WorkflowExecution execution = new WorkflowExecution(executionId, workflowId, fullContext);
                activeExecutions.put(executionId, execution);
                
                notifyWorkflowStarted(executionId, workflowId);
                
                boolean success = executeWorkflowInternal(workflow, execution);
                
                if (success) {
                    execution.setStatus(WorkflowExecutionStatus.COMPLETED);
                    notifyWorkflowCompleted(executionId, workflowId, true);
                } else {
                    execution.setStatus(WorkflowExecutionStatus.FAILED);
                    notifyWorkflowFailed(executionId, workflowId, execution.getErrorMessage());
                }
                
                return success;
                
            } catch (Exception e) {
                LOG.severe("Failed to execute workflow: " + workflowId + " - " + e.getMessage());
                return false;
            }
        }, executor);
    }
    
    /**
     * Internal workflow execution logic.
     * @param workflow The workflow to execute
     * @param execution The execution context
     * @return True if successful
     */
    private boolean executeWorkflowInternal(Workflow workflow, WorkflowExecution execution) {
        try {
            execution.setStatus(WorkflowExecutionStatus.RUNNING);
            
            switch (workflow.getExecutionMode()) {
                case SEQUENTIAL:
                    return executeSequential(workflow, execution);
                case PARALLEL:
                    return executeParallel(workflow, execution);
                case CONDITIONAL:
                    return executeConditional(workflow, execution);
                default:
                    execution.setErrorMessage("Unknown execution mode: " + workflow.getExecutionMode());
                    return false;
            }
            
        } catch (Exception e) {
            execution.setErrorMessage("Workflow execution failed: " + e.getMessage());
            LOG.severe("Workflow execution failed: " + e.getMessage());
            return false;
        } finally {
            activeExecutions.remove(execution.getExecutionId());
        }
    }
    
    /**
     * Executes workflow steps sequentially.
     * @param workflow The workflow
     * @param execution The execution context
     * @return True if successful
     */
    private boolean executeSequential(Workflow workflow, WorkflowExecution execution) {
        for (WorkflowStep step : workflow.getSteps()) {
            boolean stepSuccess = executeStep(step, execution);
            
            if (!stepSuccess) {
                if (step.isOptional()) {
                    // Continue with optional step failure
                    continue;
                } else if (workflow.getErrorHandling() == ErrorHandlingStrategy.CONTINUE_ON_ERROR) {
                    // Continue with non-optional step failure
                    continue;
                } else {
                    // Stop on error
                    execution.setErrorMessage("Step failed: " + step.getStepId());
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Executes workflow steps in parallel.
     * @param workflow The workflow
     * @param execution The execution context
     * @return True if successful
     */
    private boolean executeParallel(Workflow workflow, WorkflowExecution execution) {
        List<CompletableFuture<Boolean>> stepFutures = new ArrayList<>();
        
        for (WorkflowStep step : workflow.getSteps()) {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                return executeStep(step, execution);
            }, executor);
            stepFutures.add(future);
        }
        
        boolean allSuccessful = true;
        for (CompletableFuture<Boolean> future : stepFutures) {
            try {
                if (!future.get()) {
                    allSuccessful = false;
                }
            } catch (Exception e) {
                allSuccessful = false;
            }
        }
        
        return allSuccessful;
    }
    
    /**
     * Executes workflow steps conditionally.
     * @param workflow The workflow
     * @param execution The execution context
     * @return True if successful
     */
    private boolean executeConditional(Workflow workflow, WorkflowExecution execution) {
        // TODO: Implement conditional execution logic
        return executeSequential(workflow, execution);
    }
    
    /**
     * Executes a single step.
     * @param step The step to execute
     * @param execution The execution context
     * @return True if successful
     */
    private boolean executeStep(WorkflowStep step, WorkflowExecution execution) {
        StepExecution stepExecution = new StepExecution(step.getStepId());
        execution.addStepExecution(stepExecution);
        
        try {
            // Check conditions
            if (!checkConditions(step, execution.getContext())) {
                stepExecution.setStatus(StepExecutionStatus.SKIPPED);
                notifyStepSkipped(execution.getExecutionId(), step.getStepId());
                return true;
            }
            
            stepExecution.setStatus(StepExecutionStatus.RUNNING);
            notifyStepStarted(execution.getExecutionId(), step.getStepId());
            
            // Execute step with retry logic
            boolean success = executeStepWithRetry(step, execution, stepExecution);
            
            if (success) {
                stepExecution.setStatus(StepExecutionStatus.COMPLETED);
                notifyStepCompleted(execution.getExecutionId(), step.getStepId(), true);
                return true;
            } else {
                stepExecution.setStatus(StepExecutionStatus.FAILED);
                stepExecution.setErrorMessage("Step execution failed");
                notifyStepFailed(execution.getExecutionId(), step.getStepId(), stepExecution.getErrorMessage());
                
                // Execute compensation if needed
                if (!step.getCompensationSteps().isEmpty()) {
                    executeCompensationSteps(step, execution);
                }
                
                return false;
            }
            
        } catch (Exception e) {
            stepExecution.setStatus(StepExecutionStatus.FAILED);
            stepExecution.setErrorMessage("Step execution error: " + e.getMessage());
            notifyStepFailed(execution.getExecutionId(), step.getStepId(), stepExecution.getErrorMessage());
            return false;
        }
    }
    
    /**
     * Executes a step with retry logic.
     * @param step The step to execute
     * @param execution The execution context
     * @param stepExecution The step execution
     * @return True if successful
     */
    private boolean executeStepWithRetry(WorkflowStep step, WorkflowExecution execution, StepExecution stepExecution) {
        int maxAttempts = step.getRetryCount() + 1;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                StepExecutionResult result = executeStepLogic(step, execution.getContext());
                
                if (result.isSuccess()) {
                    // Add result to context
                    stepExecution.getResult().putAll(result.getResult());
                    return true;
                } else if (!result.shouldRetry() || attempt >= maxAttempts) {
                    stepExecution.setErrorMessage(result.getErrorMessage());
                    return false;
                } else {
                    // Retry
                    stepExecution.incrementAttemptCount();
                    Thread.sleep(config.getRetryDelay());
                }
                
            } catch (Exception e) {
                if (attempt >= maxAttempts) {
                    stepExecution.setErrorMessage("Step execution failed after " + maxAttempts + " attempts: " + e.getMessage());
                    return false;
                }
                // Retry
                stepExecution.incrementAttemptCount();
                try {
                    Thread.sleep(config.getRetryDelay());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Executes the actual step logic.
     * @param step The step to execute
     * @param context The execution context
     * @return Step execution result
     */
    private StepExecutionResult executeStepLogic(WorkflowStep step, Map<String, Object> context) {
        // TODO: Implement step execution logic based on step type
        // This would delegate to appropriate step executors
        
        switch (step.getType()) {
            case TASK:
                return executeTaskStep(step, context);
            case DECISION:
                return executeDecisionStep(step, context);
            case DELAY:
                return executeDelayStep(step, context);
            default:
                return StepExecutionResult.failure("Unknown step type: " + step.getType());
        }
    }
    
    /**
     * Executes a task step.
     */
    private StepExecutionResult executeTaskStep(WorkflowStep step, Map<String, Object> context) {
        // TODO: Implement task execution
        Map<String, Object> result = new HashMap<>();
        result.put("executed", true);
        return StepExecutionResult.success(result);
    }
    
    /**
     * Executes a decision step.
     */
    private StepExecutionResult executeDecisionStep(WorkflowStep step, Map<String, Object> context) {
        // TODO: Implement decision logic
        Map<String, Object> result = new HashMap<>();
        result.put("decision", "true");
        return StepExecutionResult.success(result);
    }
    
    /**
     * Executes a delay step.
     */
    private StepExecutionResult executeDelayStep(WorkflowStep step, Map<String, Object> context) {
        try {
            Object delayValue = step.getParameters().get("delay");
            long delay = delayValue instanceof Number ? ((Number) delayValue).longValue() : 1000;
            Thread.sleep(delay);
            
            Map<String, Object> result = new HashMap<>();
            result.put("delayed", delay);
            return StepExecutionResult.success(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return StepExecutionResult.failure("Delay interrupted");
        }
    }
    
    /**
     * Executes compensation steps.
     * @param step The original step
     * @param execution The execution context
     */
    private void executeCompensationSteps(WorkflowStep step, WorkflowExecution execution) {
        for (WorkflowStep compensationStep : step.getCompensationSteps()) {
            try {
                executeStep(compensationStep, execution);
            } catch (Exception e) {
                LOG.warning("Compensation step failed: " + compensationStep.getStepId() + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * Checks if step conditions are met.
     * @param step The step to check
     * @param context The execution context
     * @return True if conditions are met
     */
    private boolean checkConditions(WorkflowStep step, Map<String, Object> context) {
        for (String condition : step.getConditions()) {
            // TODO: Implement condition evaluation
            // For now, assume all conditions are met
        }
        return true;
    }
    
    /**
     * Cancels a workflow execution.
     * @param executionId The execution ID to cancel
     * @return True if cancelled successfully
     */
    public boolean cancelWorkflow(String executionId) {
        WorkflowExecution execution = activeExecutions.get(executionId);
        if (execution != null && execution.getStatus() == WorkflowExecutionStatus.RUNNING) {
            execution.setStatus(WorkflowExecutionStatus.CANCELLED);
            notifyWorkflowCancelled(executionId, execution.getWorkflowId());
            activeExecutions.remove(executionId);
            return true;
        }
        return false;
    }
    
    /**
     * Gets a workflow execution.
     * @param executionId The execution ID
     * @return The execution or null if not found
     */
    public WorkflowExecution getExecution(String executionId) {
        return activeExecutions.get(executionId);
    }
    
    /**
     * Gets all active executions.
     * @return Copy of active executions
     */
    public Map<String, WorkflowExecution> getActiveExecutions() {
        return new HashMap<>(activeExecutions);
    }
    
    /**
     * Gets all registered workflows.
     * @return Copy of all workflows
     */
    public Map<String, Workflow> getWorkflows() {
        return new HashMap<>(workflows);
    }
    
    /**
     * Adds a workflow listener.
     * @param listener The listener to add
     */
    public void addWorkflowListener(WorkflowListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a workflow listener.
     * @param listener The listener to remove
     */
    public void removeWorkflowListener(WorkflowListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Registers a step executor.
     * @param executor The step executor to register
     */
    public void registerStepExecutor(StepExecutor executor) {
        // TODO: Implement step executor registration
        LOG.info("Step executor registered: " + executor.getSupportedStepType());
    }
    
    // Notification methods
    
    private void notifyWorkflowStarted(String executionId, String workflowId) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.onWorkflowStarted(executionId, workflowId);
            } catch (Exception e) {
                LOG.warning("Error notifying workflow listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyWorkflowCompleted(String executionId, String workflowId, boolean success) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.onWorkflowCompleted(executionId, workflowId, success);
            } catch (Exception e) {
                LOG.warning("Error notifying workflow listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyWorkflowFailed(String executionId, String workflowId, String error) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.onWorkflowFailed(executionId, workflowId, error);
            } catch (Exception e) {
                LOG.warning("Error notifying workflow listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyWorkflowCancelled(String executionId, String workflowId) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.onWorkflowCancelled(executionId, workflowId);
            } catch (Exception e) {
                LOG.warning("Error notifying workflow listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyStepStarted(String executionId, String stepId) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.onStepStarted(executionId, stepId);
            } catch (Exception e) {
                LOG.warning("Error notifying workflow listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyStepCompleted(String executionId, String stepId, boolean success) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.onStepCompleted(executionId, stepId, success);
            } catch (Exception e) {
                LOG.warning("Error notifying workflow listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyStepFailed(String executionId, String stepId, String error) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.onStepFailed(executionId, stepId, error);
            } catch (Exception e) {
                LOG.warning("Error notifying workflow listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyStepSkipped(String executionId, String stepId) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.onStepSkipped(executionId, stepId);
            } catch (Exception e) {
                LOG.warning("Error notifying workflow listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Shuts down the workflow engine.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        LOG.info("WorkflowEngine shutdown completed");
    }
}
