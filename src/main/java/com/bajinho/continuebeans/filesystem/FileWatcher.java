package com.bajinho.continuebeans.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileAttributeEvent;

/**
 * Advanced file watcher with real-time monitoring, event filtering,
 * batch processing, and intelligent change detection.
 * 
 * @author Continue Beans Team
 */
public class FileWatcher {
    
    private static final Logger LOG = Logger.getLogger(FileWatcher.class.getName());
    
    private static FileWatcher instance;
    
    private final Map<String, WatchSession> watchSessions;
    private final List<FileWatchListener> listeners;
    private final ScheduledExecutorService scheduler;
    private final EventProcessor eventProcessor;
    private final Map<String, FileSnapshot> fileSnapshots;
    
    /**
     * Represents a watch session.
     */
    public static class WatchSession {
        private final String sessionId;
        private final String watchPath;
        private final boolean recursive;
        private final WatchConfiguration config;
        private final List<FileChangeListener> netBeansListeners;
        private final long startTime;
        private boolean active;
        private int eventCount;
        private long lastEventTime;
        
        public WatchSession(String sessionId, String watchPath, boolean recursive, 
                          WatchConfiguration config) {
            this.sessionId = sessionId;
            this.watchPath = watchPath;
            this.recursive = recursive;
            this.config = config;
            this.netBeansListeners = new ArrayList<>();
            this.startTime = System.currentTimeMillis();
            this.active = false;
            this.eventCount = 0;
            this.lastEventTime = 0;
        }
        
        // Getters and setters
        public String getSessionId() { return sessionId; }
        public String getWatchPath() { return watchPath; }
        public boolean isRecursive() { return recursive; }
        public WatchConfiguration getConfig() { return config; }
        public List<FileChangeListener> getNetBeansListeners() { return netBeansListeners; }
        public long getStartTime() { return startTime; }
        public boolean isActive() { return active; }
        public int getEventCount() { return eventCount; }
        public long getLastEventTime() { return lastEventTime; }
        
        public void setActive(boolean active) {
            this.active = active;
        }
        
        public void incrementEventCount() {
            this.eventCount++;
            this.lastEventTime = System.currentTimeMillis();
        }
        
        public long getDuration() {
            return System.currentTimeMillis() - startTime;
        }
    }
    
    /**
     * Watch configuration.
     */
    public static class WatchConfiguration {
        private final List<String> includePatterns;
        private final List<String> excludePatterns;
        private final boolean includeDirectories;
        private final boolean includeFiles;
        private final int batchSize;
        private final long batchTimeout;
        private final boolean enableDebouncing;
        private final long debounceDelay;
        private final boolean enableThrottling;
        private final int maxEventsPerSecond;
        
        public WatchConfiguration(List<String> includePatterns, List<String> excludePatterns,
                                boolean includeDirectories, boolean includeFiles,
                                int batchSize, long batchTimeout, boolean enableDebouncing,
                                long debounceDelay, boolean enableThrottling, int maxEventsPerSecond) {
            this.includePatterns = includePatterns != null ? includePatterns : new ArrayList<>();
            this.excludePatterns = excludePatterns != null ? excludePatterns : new ArrayList<>();
            this.includeDirectories = includeDirectories;
            this.includeFiles = includeFiles;
            this.batchSize = batchSize;
            this.batchTimeout = batchTimeout;
            this.enableDebouncing = enableDebouncing;
            this.debounceDelay = debounceDelay;
            this.enableThrottling = enableThrottling;
            this.maxEventsPerSecond = maxEventsPerSecond;
        }
        
        // Getters
        public List<String> getIncludePatterns() { return includePatterns; }
        public List<String> getExcludePatterns() { return excludePatterns; }
        public boolean isIncludeDirectories() { return includeDirectories; }
        public boolean isIncludeFiles() { return includeFiles; }
        public int getBatchSize() { return batchSize; }
        public long getBatchTimeout() { return batchTimeout; }
        public boolean isEnableDebouncing() { return enableDebouncing; }
        public long getDebounceDelay() { return debounceDelay; }
        public boolean isEnableThrottling() { return enableThrottling; }
        public int getMaxEventsPerSecond() { return maxEventsPerSecond; }
        
        /**
         * Creates default configuration.
         * @return Default WatchConfiguration
         */
        public static WatchConfiguration getDefault() {
            return new WatchConfiguration(
                new ArrayList<>(), // include all
                new ArrayList<>(), // exclude none
                true,  // include directories
                true,  // include files
                50,    // batch size
                1000,  // batch timeout (1 second)
                true,  // enable debouncing
                500,   // debounce delay (500ms)
                false, // disable throttling
                100    // max events per second
            );
        }
    }
    
    /**
     * File snapshot for change detection.
     */
    public static class FileSnapshot {
        private final String path;
        private final long lastModified;
        private final long size;
        private final String checksum;
        private final boolean exists;
        
        public FileSnapshot(String path, long lastModified, long size, String checksum, boolean exists) {
            this.path = path;
            this.lastModified = lastModified;
            this.size = size;
            this.checksum = checksum;
            this.exists = exists;
        }
        
        // Getters
        public String getPath() { return path; }
        public long getLastModified() { return lastModified; }
        public long getSize() { return size; }
        public String getChecksum() { return checksum; }
        public boolean isExists() { return exists; }
        
        /**
         * Checks if this snapshot differs from another.
         * @param other The other snapshot
         * @return True if different
         */
        public boolean differsFrom(FileSnapshot other) {
            if (other == null) return true;
            return this.lastModified != other.lastModified ||
                   this.size != other.size ||
                   this.exists != other.exists;
        }
    }
    
    /**
     * File watch event.
     */
    public static class FileWatchEvent {
        private final String eventType;
        private final String filePath;
        private final long timestamp;
        private final Map<String, Object> metadata;
        private final String sessionId;
        
        public FileWatchEvent(String eventType, String filePath, String sessionId) {
            this.eventType = eventType;
            this.filePath = filePath;
            this.sessionId = sessionId;
            this.timestamp = System.currentTimeMillis();
            this.metadata = new HashMap<>();
        }
        
        // Getters and setters
        public String getEventType() { return eventType; }
        public String getFilePath() { return filePath; }
        public long getTimestamp() { return timestamp; }
        public Map<String, Object> getMetadata() { return metadata; }
        public String getSessionId() { return sessionId; }
        
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
    }
    
    /**
     * File watch listener interface.
     */
    public interface FileWatchListener {
        void onFileEvent(FileWatchEvent event);
        void onBatchEvents(List<FileWatchEvent> events);
        void onWatchSessionStarted(String sessionId, String watchPath);
        void onWatchSessionStopped(String sessionId, String watchPath);
        void onWatchError(String sessionId, String error);
    }
    
    /**
     * Event processor for batching and debouncing.
     */
    private class EventProcessor {
        private final Map<String, List<FileWatchEvent>> eventBatches;
        private final Map<String, Long> lastEventTimes;
        
        public EventProcessor() {
            this.eventBatches = new ConcurrentHashMap<>();
            this.lastEventTimes = new ConcurrentHashMap<>();
        }
        
        /**
         * Processes a single event.
         * @param event The event to process
         */
        public void processEvent(FileWatchEvent event) {
            String sessionId = event.getSessionId();
            WatchSession session = watchSessions.get(sessionId);
            
            if (session == null || !session.isActive()) {
                return;
            }
            
            WatchConfiguration config = session.getConfig();
            
            // Apply filtering
            if (!shouldProcessEvent(event, config)) {
                return;
            }
            
            // Apply debouncing
            if (config.isEnableDebouncing()) {
                if (isDebounced(sessionId, event)) {
                    return;
                }
            }
            
            // Add to batch
            addToBatch(sessionId, event);
            
            // Check if batch should be processed
            if (shouldProcessBatch(sessionId, config)) {
                processBatch(sessionId);
            }
        }
        
        /**
         * Checks if event should be processed based on configuration.
         * @param event The event
         * @param config The configuration
         * @return True if should process
         */
        private boolean shouldProcessEvent(FileWatchEvent event, WatchConfiguration config) {
            String filePath = event.getFilePath();
            
            // Check include patterns
            if (!config.getIncludePatterns().isEmpty()) {
                boolean matches = false;
                for (String pattern : config.getIncludePatterns()) {
                    if (filePath.matches(pattern)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches) return false;
            }
            
            // Check exclude patterns
            for (String pattern : config.getExcludePatterns()) {
                if (filePath.matches(pattern)) {
                    return false;
                }
            }
            
            return true;
        }
        
        /**
         * Checks if event is debounced.
         * @param sessionId The session ID
         * @param event The event
         * @return True if debounced
         */
        private boolean isDebounced(String sessionId, FileWatchEvent event) {
            Long lastTime = lastEventTimes.get(sessionId);
            if (lastTime != null) {
                WatchSession session = watchSessions.get(sessionId);
                long debounceDelay = session.getConfig().getDebounceDelay();
                if (event.getTimestamp() - lastTime < debounceDelay) {
                    return true;
                }
            }
            lastEventTimes.put(sessionId, event.getTimestamp());
            return false;
        }
        
        /**
         * Adds event to batch.
         * @param sessionId The session ID
         * @param event The event
         */
        private void addToBatch(String sessionId, FileWatchEvent event) {
            eventBatches.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(event);
        }
        
        /**
         * Checks if batch should be processed.
         * @param sessionId The session ID
         * @param config The configuration
         * @return True if should process
         */
        private boolean shouldProcessBatch(String sessionId, WatchConfiguration config) {
            List<FileWatchEvent> batch = eventBatches.get(sessionId);
            if (batch == null) return false;
            
            // Check batch size
            if (batch.size() >= config.getBatchSize()) {
                return true;
            }
            
            // Check batch timeout
            if (!batch.isEmpty()) {
                long firstEventTime = batch.get(0).getTimestamp();
                if (System.currentTimeMillis() - firstEventTime >= config.getBatchTimeout()) {
                    return true;
                }
            }
            
            return false;
        }
        
        /**
         * Processes a batch of events.
         * @param sessionId The session ID
         */
        private void processBatch(String sessionId) {
            List<FileWatchEvent> batch = eventBatches.remove(sessionId);
            if (batch == null || batch.isEmpty()) {
                return;
            }
            
            // Notify listeners
            for (FileWatchListener listener : listeners) {
                try {
                    listener.onBatchEvents(batch);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error notifying batch listener", e);
                }
            }
            
            // Also notify individual events
            for (FileWatchEvent event : batch) {
                for (FileWatchListener listener : listeners) {
                    try {
                        listener.onFileEvent(event);
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Error notifying event listener", e);
                    }
                }
            }
        }
        
        /**
         * Forces processing of all pending batches.
         */
        public void processAllBatches() {
            for (String sessionId : new ArrayList<>(eventBatches.keySet())) {
                processBatch(sessionId);
            }
        }
    }
    
    /**
     * NetBeans FileChangeListener implementation.
     */
    private class NetBeansFileWatcher implements FileChangeListener {
        private final String sessionId;
        
        public NetBeansFileWatcher(String sessionId) {
            this.sessionId = sessionId;
        }
        
        @Override
        public void fileFolderCreated(FileEvent fe) {
            handleFileEvent("folder_created", fe.getFile());
        }
        
        @Override
        public void fileDataCreated(FileEvent fe) {
            handleFileEvent("file_created", fe.getFile());
        }
        
        @Override
        public void fileDeleted(FileEvent fe) {
            handleFileEvent("deleted", fe.getFile());
        }
        
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            FileWatchEvent event = new FileWatchEvent("renamed", fe.getFile().getPath(), sessionId);
            event.addMetadata("oldPath", fe.getFile().getPath()); // FileRenameEvent doesn't have getOldName()
            event.addMetadata("newPath", fe.getFile().getPath());
            eventProcessor.processEvent(event);
        }
        
        @Override
        public void fileChanged(FileEvent fe) {
            handleFileEvent("changed", fe.getFile());
        }
        
        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            handleFileEvent("attribute_changed", fe.getFile());
        }
        
        private void handleFileEvent(String eventType, FileObject file) {
            FileWatchEvent event = new FileWatchEvent(eventType, file.getPath(), sessionId);
            event.addMetadata("isFolder", file.isFolder());
            event.addMetadata("size", file.getSize());
            event.addMetadata("lastModified", file.lastModified().getTime());
            eventProcessor.processEvent(event);
        }
    }
    
    /**
     * Private constructor for singleton.
     */
    private FileWatcher() {
        this.watchSessions = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.eventProcessor = new EventProcessor();
        this.fileSnapshots = new ConcurrentHashMap<>();
        
        // Schedule periodic batch processing
        scheduler.scheduleAtFixedRate(() -> {
            eventProcessor.processAllBatches();
        }, 1, 1, TimeUnit.SECONDS);
        
        LOG.info("FileWatcher initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The FileWatcher instance
     */
    public static synchronized FileWatcher getInstance() {
        if (instance == null) {
            instance = new FileWatcher();
        }
        return instance;
    }
    
    /**
     * Starts watching a path.
     * @param watchPath The path to watch
     * @param recursive Whether to watch recursively
     * @param config The watch configuration
     * @return The session ID
     */
    public String startWatching(String watchPath, boolean recursive, WatchConfiguration config) {
        String sessionId = "watch_" + System.currentTimeMillis() + "_" + watchPath.hashCode();
        
        try {
            FileObject file = FileUtil.toFileObject(new File(watchPath));
            if (file == null) {
                LOG.warning("Cannot watch non-existent path: " + watchPath);
                return null;
            }
            
            WatchSession session = new WatchSession(sessionId, watchPath, recursive, config);
            session.setActive(true);
            
            // Create NetBeans watcher
            NetBeansFileWatcher netBeansWatcher = new NetBeansFileWatcher(sessionId);
            file.addFileChangeListener(netBeansWatcher);
            session.getNetBeansListeners().add(netBeansWatcher);
            
            // If recursive, add listeners to subdirectories
            if (recursive) {
                addRecursiveListeners(file, netBeansWatcher);
            }
            
            // Create initial file snapshots
            createFileSnapshots(file, recursive);
            
            watchSessions.put(sessionId, session);
            
            // Notify listeners
            for (FileWatchListener listener : listeners) {
                try {
                    listener.onWatchSessionStarted(sessionId, watchPath);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error notifying listener", e);
                }
            }
            
            LOG.info("Started watching: " + watchPath + " (session: " + sessionId + ")");
            return sessionId;
            
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to start watching: " + watchPath, e);
            return null;
        }
    }
    
    /**
     * Starts watching with default configuration.
     * @param watchPath The path to watch
     * @param recursive Whether to watch recursively
     * @return The session ID
     */
    public String startWatching(String watchPath, boolean recursive) {
        return startWatching(watchPath, recursive, WatchConfiguration.getDefault());
    }
    
    /**
     * Adds recursive listeners to a directory.
     * @param folder The folder
     * @param watcher The watcher
     */
    private void addRecursiveListeners(FileObject folder, NetBeansFileWatcher watcher) {
        try {
            for (FileObject child : folder.getChildren()) {
                if (child.isFolder()) {
                    child.addFileChangeListener(watcher);
                    addRecursiveListeners(child, watcher);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to add recursive listeners", e);
        }
    }
    
    /**
     * Creates initial file snapshots.
     * @param folder The folder
     * @param recursive Whether to create snapshots recursively
     */
    private void createFileSnapshots(FileObject folder, boolean recursive) {
        try {
            createSnapshot(folder);
            
            if (recursive) {
                for (FileObject child : folder.getChildren()) {
                    if (child.isFolder()) {
                        createFileSnapshots(child, true);
                    } else {
                        createSnapshot(child);
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to create file snapshots", e);
        }
    }
    
    /**
     * Creates a snapshot for a file.
     * @param file The file
     */
    private void createSnapshot(FileObject file) {
        try {
            String checksum = calculateChecksum(file);
            FileSnapshot snapshot = new FileSnapshot(
                file.getPath(),
                file.lastModified().getTime(),
                file.getSize(),
                checksum,
                file.isValid()
            );
            fileSnapshots.put(file.getPath(), snapshot);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to create snapshot: " + file.getPath(), e);
        }
    }
    
    /**
     * Calculates a simple checksum for a file.
     * @param file The file
     * @return The checksum
     */
    private String calculateChecksum(FileObject file) {
        // Simple checksum based on size and last modified
        return file.getSize() + "_" + file.lastModified().getTime();
    }
    
    /**
     * Stops watching a session.
     * @param sessionId The session ID to stop
     */
    public void stopWatching(String sessionId) {
        WatchSession session = watchSessions.remove(sessionId);
        if (session != null) {
            try {
                FileObject file = FileUtil.toFileObject(new File(session.getWatchPath()));
                if (file != null) {
                    // Remove NetBeans listeners
                    for (FileChangeListener listener : session.getNetBeansListeners()) {
                        file.removeFileChangeListener(listener);
                    }
                }
                
                session.setActive(false);
                
                // Notify listeners
                for (FileWatchListener listener : listeners) {
                    try {
                        listener.onWatchSessionStopped(sessionId, session.getWatchPath());
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Error notifying listener", e);
                    }
                }
                
                LOG.info("Stopped watching: " + session.getWatchPath() + " (session: " + sessionId + ")");
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to stop watching: " + session.getWatchPath(), e);
            }
        }
    }
    
    /**
     * Stops all watching sessions.
     */
    public void stopAllWatching() {
        for (String sessionId : new ArrayList<>(watchSessions.keySet())) {
            stopWatching(sessionId);
        }
    }
    
    /**
     * Gets a watch session.
     * @param sessionId The session ID
     * @return The session or null if not found
     */
    public WatchSession getWatchSession(String sessionId) {
        return watchSessions.get(sessionId);
    }
    
    /**
     * Gets all active watch sessions.
     * @return Copy of active sessions
     */
    public Map<String, WatchSession> getActiveSessions() {
        Map<String, WatchSession> active = new HashMap<>();
        for (Map.Entry<String, WatchSession> entry : watchSessions.entrySet()) {
            if (entry.getValue().isActive()) {
                active.put(entry.getKey(), entry.getValue());
            }
        }
        return active;
    }
    
    /**
     * Gets file snapshot.
     * @param filePath The file path
     * @return The snapshot or null if not found
     */
    public FileSnapshot getFileSnapshot(String filePath) {
        return fileSnapshots.get(filePath);
    }
    
    /**
     * Updates file snapshot.
     * @param filePath The file path
     */
    public void updateFileSnapshot(String filePath) {
        try {
            FileObject file = FileUtil.toFileObject(new File(filePath));
            if (file != null) {
                createSnapshot(file);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to update snapshot: " + filePath, e);
        }
    }
    
    /**
     * Adds a file watch listener.
     * @param listener The listener to add
     */
    public void addFileWatchListener(FileWatchListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a file watch listener.
     * @param listener The listener to remove
     */
    public void removeFileWatchListener(FileWatchListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Gets statistics for all sessions.
     * @return Statistics map
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        int totalSessions = watchSessions.size();
        int activeSessions = 0;
        int totalEvents = 0;
        long totalDuration = 0;
        
        for (WatchSession session : watchSessions.values()) {
            if (session.isActive()) {
                activeSessions++;
            }
            totalEvents += session.getEventCount();
            totalDuration += session.getDuration();
        }
        
        stats.put("totalSessions", totalSessions);
        stats.put("activeSessions", activeSessions);
        stats.put("totalEvents", totalEvents);
        stats.put("averageSessionDuration", totalSessions > 0 ? totalDuration / totalSessions : 0);
        stats.put("fileSnapshots", fileSnapshots.size());
        
        return stats;
    }
    
    /**
     * Clears file snapshots.
     */
    public void clearFileSnapshots() {
        fileSnapshots.clear();
    }
    
    /**
     * Shuts down the file watcher.
     */
    public void shutdown() {
        stopAllWatching();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        LOG.info("FileWatcher shutdown completed");
    }
}
