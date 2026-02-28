package com.bajinho.continuebeans.netbeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.ModuleInstall;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;

/**
 * Deep NetBeans integration manager with advanced platform customization,
 * module management, service integration, and complete IDE control.
 * 
 * @author Continue Beans Team
 */
public class NetBeansIntegrationManager {
    
    private static final Logger LOG = Logger.getLogger(NetBeansIntegrationManager.class.getName());
    
    private static NetBeansIntegrationManager instance;
    
    private final Map<String, NetBeansModule> modules;
    private final Map<String, NetBeansService> services;
    private final Map<String, NetBeansAction> actions;
    private final List<IntegrationListener> listeners;
    private final ModuleManager moduleManager;
    private final ServiceManager serviceManager;
    private final ActionManager actionManager;
    private final WindowManagerIntegration windowManager;
    private final ProjectIntegration projectIntegration;
    private final LookupIntegration lookupIntegration;
    
    /**
     * Represents a NetBeans module.
     */
    public static class NetBeansModule {
        private final String moduleName;
        private final String displayName;
        private final String version;
        private final boolean enabled;
        private final List<String> dependencies;
        private final Map<String, Object> metadata;
        private final ModuleInfo moduleInfo;
        
        public NetBeansModule(String moduleName, String displayName, String version,
                            boolean enabled, List<String> dependencies, Map<String, Object> metadata,
                            ModuleInfo moduleInfo) {
            this.moduleName = moduleName;
            this.displayName = displayName;
            this.version = version;
            this.enabled = enabled;
            this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
            this.metadata = metadata != null ? metadata : new HashMap<>();
            this.moduleInfo = moduleInfo;
        }
        
        // Getters
        public String getModuleName() { return moduleName; }
        public String getDisplayName() { return displayName; }
        public String getVersion() { return version; }
        public boolean isEnabled() { return enabled; }
        public List<String> getDependencies() { return dependencies; }
        public Map<String, Object> getMetadata() { return metadata; }
        public ModuleInfo getModuleInfo() { return moduleInfo; }
    }
    
    /**
     * Represents a NetBeans service.
     */
    public static class NetBeansService {
        private final String serviceName;
        private final String interfaceName;
        private final String implementationClass;
        private final boolean active;
        private final Map<String, Object> properties;
        private final Object serviceInstance;
        
        public NetBeansService(String serviceName, String interfaceName, String implementationClass,
                              boolean active, Map<String, Object> properties, Object serviceInstance) {
            this.serviceName = serviceName;
            this.interfaceName = interfaceName;
            this.implementationClass = implementationClass;
            this.active = active;
            this.properties = properties != null ? properties : new HashMap<>();
            this.serviceInstance = serviceInstance;
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public String getInterfaceName() { return interfaceName; }
        public String getImplementationClass() { return implementationClass; }
        public boolean isActive() { return active; }
        public Map<String, Object> getProperties() { return properties; }
        public Object getServiceInstance() { return serviceInstance; }
    }
    
    /**
     * Represents a NetBeans action.
     */
    public static class NetBeansAction {
        private final String actionId;
        private final String displayName;
        private final String category;
        private final String menuPath;
        private final String shortcut;
        private final boolean enabled;
        private final Object actionInstance;
        private final Map<String, Object> properties;
        
        public NetBeansAction(String actionId, String displayName, String category,
                             String menuPath, String shortcut, boolean enabled, Object actionInstance,
                             Map<String, Object> properties) {
            this.actionId = actionId;
            this.displayName = displayName;
            this.category = category;
            this.menuPath = menuPath;
            this.shortcut = shortcut;
            this.enabled = enabled;
            this.actionInstance = actionInstance;
            this.properties = properties != null ? properties : new HashMap<>();
        }
        
        // Getters
        public String getActionId() { return actionId; }
        public String getDisplayName() { return displayName; }
        public String getCategory() { return category; }
        public String getMenuPath() { return menuPath; }
        public String getShortcut() { return shortcut; }
        public boolean isEnabled() { return enabled; }
        public Object getActionInstance() { return actionInstance; }
        public Map<String, Object> getProperties() { return properties; }
    }
    
    /**
     * Module manager.
     */
    public static class ModuleManager {
        private final Map<String, NetBeansModule> modules;
        
        public ModuleManager() {
            this.modules = new ConcurrentHashMap<>();
            initializeModuleTracking();
        }
        
        /**
         * Initializes module tracking.
         */
        private void initializeModuleTracking() {
            // TODO: Initialize module tracking from NetBeans ModuleSystem
        }
        
        /**
         * Gets all modules.
         * @return List of all modules
         */
        public List<NetBeansModule> getAllModules() {
            return new ArrayList<>(modules.values());
        }
        
        /**
         * Gets enabled modules.
         * @return List of enabled modules
         */
        public List<NetBeansModule> getEnabledModules() {
            List<NetBeansModule> enabled = new ArrayList<>();
            for (NetBeansModule module : modules.values()) {
                if (module.isEnabled()) {
                    enabled.add(module);
                }
            }
            return enabled;
        }
        
        /**
         * Enables a module.
         * @param moduleName The module name
         * @return True if successful
         */
        public boolean enableModule(String moduleName) {
            // TODO: Implement module enabling
            return false;
        }
        
        /**
         * Disables a module.
         * @param moduleName The module name
         * @return True if successful
         */
        public boolean disableModule(String moduleName) {
            // TODO: Implement module disabling
            return false;
        }
        
        /**
         * Installs a module.
         * @param modulePath The module path
         * @return CompletableFuture with installation result
         */
        public CompletableFuture<Boolean> installModule(String modulePath) {
            return CompletableFuture.supplyAsync(() -> {
                // TODO: Implement module installation
                return false;
            });
        }
        
        /**
         * Uninstalls a module.
         * @param moduleName The module name
         * @return CompletableFuture with uninstallation result
         */
        public CompletableFuture<Boolean> uninstallModule(String moduleName) {
            return CompletableFuture.supplyAsync(() -> {
                // TODO: Implement module uninstallation
                return false;
            });
        }
    }
    
    /**
     * Service manager.
     */
    public static class ServiceManager {
        private final Map<String, NetBeansService> services;
        
        public ServiceManager() {
            this.services = new ConcurrentHashMap<>();
            initializeServiceTracking();
        }
        
        /**
         * Initializes service tracking.
         */
        private void initializeServiceTracking() {
            // Track all services in the global Lookup
            Lookup globalLookup = Lookup.getDefault();
            for (Object service : globalLookup.lookupAll(Object.class)) {
                registerService(service);
            }
        }
        
        /**
         * Registers a service.
         * @param serviceInstance The service instance
         */
        public void registerService(Object serviceInstance) {
            if (serviceInstance != null) {
                String serviceName = serviceInstance.getClass().getSimpleName();
                String interfaceName = serviceInstance.getClass().getInterfaces().length > 0 ?
                    serviceInstance.getClass().getInterfaces()[0].getSimpleName() : "Object";
                
                NetBeansService service = new NetBeansService(
                    serviceName, interfaceName, serviceInstance.getClass().getName(),
                    true, new HashMap<>(), serviceInstance
                );
                
                services.put(serviceName, service);
            }
        }
        
        /**
         * Gets all services.
         * @return List of all services
         */
        public List<NetBeansService> getAllServices() {
            return new ArrayList<>(services.values());
        }
        
        /**
         * Gets active services.
         * @return List of active services
         */
        public List<NetBeansService> getActiveServices() {
            List<NetBeansService> active = new ArrayList<>();
            for (NetBeansService service : services.values()) {
                if (service.isActive()) {
                    active.add(service);
                }
            }
            return active;
        }
        
        /**
         * Gets a service by name.
         * @param serviceName The service name
         * @return The service or null
         */
        public NetBeansService getService(String serviceName) {
            return services.get(serviceName);
        }
        
        /**
         * Gets a service implementation.
         * @param <T> The service type
         * @param serviceClass The service class
         * @return The service instance or null
         */
        public <T> T getService(Class<T> serviceClass) {
            Lookup globalLookup = Lookup.getDefault();
            return globalLookup.lookup(serviceClass);
        }
        
        /**
         * Gets all service implementations.
         * @param <T> The service type
         * @param serviceClass The service class
         * @return All service instances
         */
        public <T> Collection<? extends T> getServices(Class<T> serviceClass) {
            Lookup globalLookup = Lookup.getDefault();
            return globalLookup.lookupAll(serviceClass);
        }
    }
    
    /**
     * Action manager.
     */
    public static class ActionManager {
        private final Map<String, NetBeansAction> actions;
        
        public ActionManager() {
            this.actions = new ConcurrentHashMap<>();
            initializeActionTracking();
        }
        
        /**
         * Initializes action tracking.
         */
        private void initializeActionTracking() {
            // Track all system actions
            // TODO: Implement comprehensive action tracking
        }
        
        /**
         * Registers an action.
         * @param actionId The action ID
         * @param actionInstance The action instance
         */
        public void registerAction(String actionId, Object actionInstance) {
            // TODO: Implement action registration
        }
        
        /**
         * Gets all actions.
         * @return List of all actions
         */
        public List<NetBeansAction> getAllActions() {
            return new ArrayList<>(actions.values());
        }
        
        /**
         * Gets actions by category.
         * @param category The category
         * @return List of actions in category
         */
        public List<NetBeansAction> getActionsByCategory(String category) {
            List<NetBeansAction> categoryActions = new ArrayList<>();
            for (NetBeansAction action : actions.values()) {
                if (category.equals(action.getCategory())) {
                    categoryActions.add(action);
                }
            }
            return categoryActions;
        }
        
        /**
         * Executes an action.
         * @param actionId The action ID
         * @return True if successful
         */
        public boolean executeAction(String actionId) {
            try {
                NetBeansAction action = actions.get(actionId);
                if (action != null && action.isEnabled()) {
                    // TODO: Execute action
                    return true;
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to execute action: " + actionId, e);
            }
            return false;
        }
        
        /**
         * Enables an action.
         * @param actionId The action ID
         * @return True if successful
         */
        public boolean enableAction(String actionId) {
            // TODO: Implement action enabling
            return false;
        }
        
        /**
         * Disables an action.
         * @param actionId The action ID
         * @return True if successful
         */
        public boolean disableAction(String actionId) {
            // TODO: Implement action disabling
            return false;
        }
    }
    
    /**
     * Window manager integration.
     */
    public static class WindowManagerIntegration {
        private final WindowManager windowManager;
        
        public WindowManagerIntegration() {
            this.windowManager = WindowManager.getDefault();
        }
        
        /**
         * Gets all open windows.
         * @return List of open windows
         */
        public List<Object> getOpenWindows() {
            // TODO: Implement window enumeration
            return new ArrayList<>();
        }
        
        /**
         * Gets active window.
         * @return The active window or null
         */
        public Object getActiveWindow() {
            // TODO: Implement active window detection
            return null;
        }
        
        /**
         * Opens a window.
         * @param windowClass The window class
         * @return True if successful
         */
        public boolean openWindow(Class<?> windowClass) {
            try {
                // TODO: Implement window opening
                return true;
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to open window: " + windowClass.getName(), e);
                return false;
            }
        }
        
        /**
         * Closes a window.
         * @param window The window to close
         * @return True if successful
         */
        public boolean closeWindow(Object window) {
            try {
                // TODO: Implement window closing
                return true;
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to close window", e);
                return false;
            }
        }
    }
    
    /**
     * OpenProjects placeholder.
     */
    public static class OpenProjects {
        private static OpenProjects instance;
        
        public static OpenProjects getDefault() {
            if (instance == null) {
                instance = new OpenProjects();
            }
            return instance;
        }
        
        public Project[] getOpenProjects() {
            return new Project[0];
        }
        
        public Project getMainProject() {
            return null;
        }
        
        public void open(Project[] projects, boolean openRequiredProjects) {
            // TODO: Implement project opening
        }
        
        public void close(Project[] projects) {
            // TODO: Implement project closing
        }
    }
    public static class ProjectIntegration {
        private final OpenProjects openProjects;
        
        public ProjectIntegration() {
            this.openProjects = OpenProjects.getDefault();
        }
        
        /**
         * Gets all open projects.
         * @return List of open projects
         */
        public List<Project> getOpenProjects() {
            List<Project> projects = new ArrayList<>();
            for (Project project : openProjects.getOpenProjects()) {
                projects.add(project);
            }
            return projects;
        }
        
        /**
         * Gets the main project.
         * @return The main project or null
         */
        public Project getMainProject() {
            return openProjects.getMainProject();
        }
        
        /**
         * Opens a project.
         * @param projectDir The project directory
         * @return CompletableFuture with the opened project
         */
        public CompletableFuture<Project> openProject(FileObject projectDir) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Project project = ProjectManager.getDefault().findProject(projectDir);
                    if (project != null) {
                        openProjects.open(new Project[]{project}, false);
                        return project;
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to open project: " + projectDir.getPath(), e);
                }
                return null;
            });
        }
        
        /**
         * Closes a project.
         * @param project The project to close
         * @return True if successful
         */
        public boolean closeProject(Project project) {
            try {
                openProjects.close(new Project[]{project});
                return true;
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to close project", e);
                return false;
            }
        }
        
        /**
         * Creates a new project.
         * @param projectType The project type
         * @param projectDir The project directory
         * @param projectName The project name
         * @return CompletableFuture with the created project
         */
        public CompletableFuture<Project> createProject(String projectType, FileObject projectDir, String projectName) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // TODO: Implement project creation
                    return null;
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to create project: " + projectName, e);
                    return null;
                }
            });
        }
    }
    
    /**
     * Lookup integration.
     */
    public static class LookupIntegration {
        private final Lookup globalLookup;
        
        public LookupIntegration() {
            this.globalLookup = Lookup.getDefault();
        }
        
        /**
         * Gets the global lookup.
         * @return The global lookup
         */
        public Lookup getGlobalLookup() {
            return globalLookup;
        }
        
        /**
         * Looks up a service.
         * @param <T> The service type
         * @param serviceClass The service class
         * @return The service or null
         */
        public <T> T lookup(Class<T> serviceClass) {
            return globalLookup.lookup(serviceClass);
        }
        
        /**
         * Looks up all services.
         * @param <T> The service type
         * @param serviceClass The service class
         * @return All services
         */
        public <T> Collection<? extends T> lookupAll(Class<T> serviceClass) {
            return globalLookup.lookupAll(serviceClass);
        }
        
        /**
         * Creates a lookup with specific services.
         * @param services The services to include
         * @return The created lookup
         */
        public Lookup createLookup(Object... services) {
            // TODO: Implement custom lookup creation
            return Lookup.EMPTY;
        }
        
        /**
         * Registers a service in the global lookup.
         * @param service The service to register
         */
        public void registerService(Object service) {
            // TODO: Implement service registration
        }
    }
    
    /**
     * Integration listener interface.
     */
    public interface IntegrationListener {
        void onModuleLoaded(String moduleName);
        void onModuleUnloaded(String moduleName);
        void onServiceRegistered(String serviceName);
        void onServiceUnregistered(String serviceName);
        void onActionRegistered(String actionId);
        void onActionUnregistered(String actionId);
        void onProjectOpened(Project project);
        void onProjectClosed(Project project);
    }
    
    /**
     * Private constructor for singleton.
     */
    private NetBeansIntegrationManager() {
        this.modules = new ConcurrentHashMap<>();
        this.services = new ConcurrentHashMap<>();
        this.actions = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.moduleManager = new ModuleManager();
        this.serviceManager = new ServiceManager();
        this.actionManager = new ActionManager();
        this.windowManager = new WindowManagerIntegration();
        this.projectIntegration = new ProjectIntegration();
        this.lookupIntegration = new LookupIntegration();
        
        initializeIntegration();
        
        LOG.info("NetBeansIntegrationManager initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The NetBeansIntegrationManager instance
     */
    public static synchronized NetBeansIntegrationManager getInstance() {
        if (instance == null) {
            instance = new NetBeansIntegrationManager();
        }
        return instance;
    }
    
    /**
     * Initializes the integration.
     */
    private void initializeIntegration() {
        // Initialize module tracking
        initializeModuleTracking();
        
        // Initialize service tracking
        initializeServiceTracking();
        
        // Initialize action tracking
        initializeActionTracking();
        
        // Initialize project tracking
        initializeProjectTracking();
    }
    
    /**
     * Initializes module tracking.
     */
    private void initializeModuleTracking() {
        // TODO: Initialize module tracking from NetBeans ModuleSystem
    }
    
    /**
     * Initializes service tracking.
     */
    private void initializeServiceTracking() {
        // Track all existing services
        serviceManager.initializeServiceTracking();
    }
    
    /**
     * Initializes action tracking.
     */
    private void initializeActionTracking() {
        // TODO: Initialize action tracking from NetBeans Action system
    }
    
    /**
     * Initializes project tracking.
     */
    private void initializeProjectTracking() {
        // Track existing open projects
        for (Project project : projectIntegration.getOpenProjects()) {
            notifyProjectOpened(project);
        }
    }
    
    /**
     * Gets the module manager.
     * @return Module manager
     */
    public ModuleManager getModuleManager() {
        return moduleManager;
    }
    
    /**
     * Gets the service manager.
     * @return Service manager
     */
    public ServiceManager getServiceManager() {
        return serviceManager;
    }
    
    /**
     * Gets the action manager.
     * @return Action manager
     */
    public ActionManager getActionManager() {
        return actionManager;
    }
    
    /**
     * Gets the window manager integration.
     * @return Window manager integration
     */
    public WindowManagerIntegration getWindowManager() {
        return windowManager;
    }
    
    /**
     * Gets the project integration.
     * @return Project integration
     */
    public ProjectIntegration getProjectIntegration() {
        return projectIntegration;
    }
    
    /**
     * Gets the lookup integration.
     * @return Lookup integration
     */
    public LookupIntegration getLookupIntegration() {
        return lookupIntegration;
    }
    
    /**
     * Gets NetBeans platform information.
     * @return Platform information
     */
    public Map<String, Object> getPlatformInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Basic platform info
        info.put("netbeansVersion", System.getProperty("netbeans.version"));
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        
        // Module info
        info.put("totalModules", moduleManager.getAllModules().size());
        info.put("enabledModules", moduleManager.getEnabledModules().size());
        
        // Service info
        info.put("totalServices", serviceManager.getAllServices().size());
        info.put("activeServices", serviceManager.getActiveServices().size());
        
        // Action info
        info.put("totalActions", actionManager.getAllActions().size());
        
        // Project info
        info.put("openProjects", projectIntegration.getOpenProjects().size());
        
        return info;
    }
    
    /**
     * Performs platform health check.
     * @return CompletableFuture with health status
     */
    public CompletableFuture<Map<String, Object>> performHealthCheck() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> health = new HashMap<>();
            
            // Check module system
            health.put("moduleSystemHealthy", checkModuleSystemHealth());
            
            // Check service system
            health.put("serviceSystemHealthy", checkServiceSystemHealth());
            
            // Check action system
            health.put("actionSystemHealthy", checkActionSystemHealth());
            
            // Check window system
            health.put("windowSystemHealthy", checkWindowSystemHealth());
            
            // Check project system
            health.put("projectSystemHealthy", checkProjectSystemHealth());
            
            // Overall health
            boolean overallHealthy = (boolean) health.get("moduleSystemHealthy") &&
                                   (boolean) health.get("serviceSystemHealthy") &&
                                   (boolean) health.get("actionSystemHealthy") &&
                                   (boolean) health.get("windowSystemHealthy") &&
                                   (boolean) health.get("projectSystemHealthy");
            health.put("overallHealthy", overallHealthy);
            
            return health;
        });
    }
    
    /**
     * Checks module system health.
     * @return True if healthy
     */
    private boolean checkModuleSystemHealth() {
        try {
            // Check if we can access module information
            List<NetBeansModule> modules = moduleManager.getAllModules();
            return modules != null && !modules.isEmpty();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Module system health check failed", e);
            return false;
        }
    }
    
    /**
     * Checks service system health.
     * @return True if healthy
     */
    private boolean checkServiceSystemHealth() {
        try {
            // Check if we can access global lookup
            Lookup globalLookup = lookupIntegration.getGlobalLookup();
            return globalLookup != null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Service system health check failed", e);
            return false;
        }
    }
    
    /**
     * Checks action system health.
     * @return True if healthy
     */
    private boolean checkActionSystemHealth() {
        try {
            // Check if we can access system actions
            List<NetBeansAction> actions = actionManager.getAllActions();
            return actions != null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Action system health check failed", e);
            return false;
        }
    }
    
    /**
     * Checks window system health.
     * @return True if healthy
     */
    private boolean checkWindowSystemHealth() {
        try {
            // Check if we can access window manager
            WindowManager wm = windowManager.windowManager;
            return wm != null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Window system health check failed", e);
            return false;
        }
    }
    
    /**
     * Checks project system health.
     * @return True if healthy
     */
    private boolean checkProjectSystemHealth() {
        try {
            // Check if we can access open projects
            List<Project> projects = projectIntegration.getOpenProjects();
            return projects != null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Project system health check failed", e);
            return false;
        }
    }
    
    /**
     * Adds an integration listener.
     * @param listener The listener to add
     */
    public void addIntegrationListener(IntegrationListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes an integration listener.
     * @param listener The listener to remove
     */
    public void removeIntegrationListener(IntegrationListener listener) {
        listeners.remove(listener);
    }
    
    // Notification methods
    
    private void notifyModuleLoaded(String moduleName) {
        for (IntegrationListener listener : listeners) {
            try {
                listener.onModuleLoaded(moduleName);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyModuleUnloaded(String moduleName) {
        for (IntegrationListener listener : listeners) {
            try {
                listener.onModuleUnloaded(moduleName);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyServiceRegistered(String serviceName) {
        for (IntegrationListener listener : listeners) {
            try {
                listener.onServiceRegistered(serviceName);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyServiceUnregistered(String serviceName) {
        for (IntegrationListener listener : listeners) {
            try {
                listener.onServiceUnregistered(serviceName);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyActionRegistered(String actionId) {
        for (IntegrationListener listener : listeners) {
            try {
                listener.onActionRegistered(actionId);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyActionUnregistered(String actionId) {
        for (IntegrationListener listener : listeners) {
            try {
                listener.onActionUnregistered(actionId);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyProjectOpened(Project project) {
        for (IntegrationListener listener : listeners) {
            try {
                listener.onProjectOpened(project);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyProjectClosed(Project project) {
        for (IntegrationListener listener : listeners) {
            try {
                listener.onProjectClosed(project);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    /**
     * Gets integration statistics.
     * @return Statistics map
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("modules", modules.size());
        stats.put("services", services.size());
        stats.put("actions", actions.size());
        stats.put("listeners", listeners.size());
        return stats;
    }
}
