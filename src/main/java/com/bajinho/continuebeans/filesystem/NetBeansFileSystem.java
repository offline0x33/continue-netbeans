package com.bajinho.continuebeans.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Lookup;

/**
 * NetBeans FileSystem wrapper providing high-level file operations
 * with async support, change monitoring, and project integration.
 * 
 * @author Continue Beans Team
 */
public class NetBeansFileSystem {
    
    private static final Logger LOG = Logger.getLogger(NetBeansFileSystem.class.getName());
    
    private static NetBeansFileSystem instance;
    
    private final Map<String, FileWatcher> watchers;
    private final List<FileSystemListener> listeners;
    private final Map<String, FileOperation> pendingOperations;
    
    /**
     * Represents a file operation result.
     */
    public static class FileOperation {
        private final String operationId;
        private final String operationType;
        private final String filePath;
        private final long startTime;
        private long endTime;
        private boolean completed;
        private boolean success;
        private String errorMessage;
        private Object result;
        
        public FileOperation(String operationId, String operationType, String filePath) {
            this.operationId = operationId;
            this.operationType = operationType;
            this.filePath = filePath;
            this.startTime = System.currentTimeMillis();
            this.completed = false;
            this.success = false;
        }
        
        // Getters and setters
        public String getOperationId() { return operationId; }
        public String getOperationType() { return operationType; }
        public String getFilePath() { return filePath; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public boolean isCompleted() { return completed; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public Object getResult() { return result; }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
            if (completed) {
                this.endTime = System.currentTimeMillis();
            }
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
            this.completed = true;
            this.endTime = System.currentTimeMillis();
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            this.success = false;
            this.completed = true;
            this.endTime = System.currentTimeMillis();
        }
        
        public void setResult(Object result) {
            this.result = result;
        }
        
        public long getDuration() {
            return endTime - startTime;
        }
    }
    
    /**
     * File watcher for monitoring file changes.
     */
    public static class FileWatcher implements FileChangeListener {
        private final String watchPath;
        private final FileChangeListener listener;
        private final boolean recursive;
        private final List<FileWatchListener> watchListeners;
        private boolean active;
        
        public FileWatcher(String watchPath, FileChangeListener listener, boolean recursive) {
            this.watchPath = watchPath;
            this.listener = listener;
            this.recursive = recursive;
            this.watchListeners = new ArrayList<>();
            this.active = false;
        }
        
        @Override
        public void fileFolderCreated(FileEvent fe) {
            notifyListeners("created", fe.getFile());
        }
        
        @Override
        public void fileDataCreated(FileEvent fe) {
            notifyListeners("created", fe.getFile());
        }
        
        @Override
        public void fileDeleted(FileEvent fe) {
            notifyListeners("deleted", fe.getFile());
        }
        
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            notifyListeners("renamed", fe.getFile());
        }
        
        @Override
        public void fileChanged(FileEvent fe) {
            notifyListeners("changed", fe.getFile());
        }
        
        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            notifyListeners("attribute_changed", fe.getFile());
        }
        
        private void notifyListeners(String eventType, FileObject file) {
            for (FileWatchListener watchListener : watchListeners) {
                try {
                    watchListener.onFileEvent(eventType, file);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error notifying watch listener", e);
                }
            }
        }
        
        public void addWatchListener(FileWatchListener listener) {
            watchListeners.add(listener);
        }
        
        public void removeWatchListener(FileWatchListener listener) {
            watchListeners.remove(listener);
        }
        
        // Getters
        public String getWatchPath() { return watchPath; }
        public boolean isRecursive() { return recursive; }
        public boolean isActive() { return active; }
        
        public void setActive(boolean active) {
            this.active = active;
        }
    }
    
    /**
     * File watch listener interface.
     */
    public interface FileWatchListener {
        void onFileEvent(String eventType, FileObject file);
    }
    
    /**
     * File system listener interface.
     */
    public interface FileSystemListener {
        void onFileOperation(FileOperation operation);
        void onFileWatchStarted(String watchPath);
        void onFileWatchStopped(String watchPath);
        void onFileSystemError(String operation, String error);
    }
    
    /**
     * Private constructor for singleton.
     */
    private NetBeansFileSystem() {
        this.watchers = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.pendingOperations = new ConcurrentHashMap<>();
        
        LOG.info("NetBeansFileSystem initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The NetBeansFileSystem instance
     */
    public static synchronized NetBeansFileSystem getInstance() {
        if (instance == null) {
            instance = new NetBeansFileSystem();
        }
        return instance;
    }
    
    /**
     * Gets a FileObject from a file path.
     * @param filePath The file path
     * @return The FileObject or null if not found
     */
    public FileObject getFileObject(String filePath) {
        try {
            File file = new File(filePath);
            return FileUtil.toFileObject(file);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get FileObject for: " + filePath, e);
            return null;
        }
    }
    
    /**
     * Gets a FileObject from a file.
     * @param file The file
     * @return The FileObject or null if not found
     */
    public FileObject getFileObject(File file) {
        try {
            return FileUtil.toFileObject(file);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get FileObject for: " + file, e);
            return null;
        }
    }
    
    /**
     * Creates a new file asynchronously.
     * @param parentPath The parent directory path
     * @param fileName The file name
     * @param content The initial content (can be null)
     * @return CompletableFuture with the operation result
     */
    public CompletableFuture<FileOperation> createFileAsync(String parentPath, String fileName, String content) {
        return CompletableFuture.supplyAsync(() -> {
            String operationId = "create_" + System.currentTimeMillis();
            FileOperation operation = new FileOperation(operationId, "create", parentPath + "/" + fileName);
            pendingOperations.put(operationId, operation);
            
            try {
                FileObject parent = getFileObject(parentPath);
                if (parent == null) {
                    operation.setErrorMessage("Parent directory not found: " + parentPath);
                    return operation;
                }
                
                FileObject file = parent.createData(fileName);
                
                if (content != null && !content.isEmpty()) {
                    try (OutputStream os = file.getOutputStream()) {
                        os.write(content.getBytes());
                    }
                }
                
                operation.setResult(file);
                operation.setSuccess(true);
                notifyListeners(operation);
                
                LOG.info("File created successfully: " + file.getPath());
                return operation;
                
            } catch (IOException e) {
                if (e.getMessage().contains("already exists")) {
                    operation.setErrorMessage("File already exists: " + fileName);
                    LOG.log(Level.WARNING, "File already exists: " + fileName, e);
                } else {
                    operation.setErrorMessage("IO Error: " + e.getMessage());
                    LOG.log(Level.SEVERE, "Failed to create file: " + fileName, e);
                }
            } catch (Exception e) {
                operation.setErrorMessage("Unexpected error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Unexpected error creating file: " + fileName, e);
            }
            
            return operation;
        });
    }
    
    /**
     * Creates a new directory asynchronously.
     * @param parentPath The parent directory path
     * @param dirName The directory name
     * @return CompletableFuture with the operation result
     */
    public CompletableFuture<FileOperation> createDirectoryAsync(String parentPath, String dirName) {
        return CompletableFuture.supplyAsync(() -> {
            String operationId = "mkdir_" + System.currentTimeMillis();
            FileOperation operation = new FileOperation(operationId, "mkdir", parentPath + "/" + dirName);
            pendingOperations.put(operationId, operation);
            
            try {
                FileObject parent = getFileObject(parentPath);
                if (parent == null) {
                    operation.setErrorMessage("Parent directory not found: " + parentPath);
                    return operation;
                }
                
                FileObject dir = parent.createFolder(dirName);
                operation.setResult(dir);
                operation.setSuccess(true);
                notifyListeners(operation);
                
                LOG.info("Directory created successfully: " + dir.getPath());
                return operation;
                
            } catch (IOException e) {
                if (e.getMessage().contains("already exists")) {
                    operation.setErrorMessage("Directory already exists: " + dirName);
                    LOG.log(Level.WARNING, "Directory already exists: " + dirName, e);
                } else {
                    operation.setErrorMessage("IO Error: " + e.getMessage());
                    LOG.log(Level.SEVERE, "Failed to create directory: " + dirName, e);
                }
            } catch (Exception e) {
                operation.setErrorMessage("Unexpected error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Unexpected error creating directory: " + dirName, e);
            }
            
            return operation;
        });
    }
    
    /**
     * Deletes a file or directory asynchronously.
     * @param filePath The file/directory path
     * @return CompletableFuture with the operation result
     */
    public CompletableFuture<FileOperation> deleteAsync(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            String operationId = "delete_" + System.currentTimeMillis();
            FileOperation operation = new FileOperation(operationId, "delete", filePath);
            pendingOperations.put(operationId, operation);
            
            try {
                FileObject file = getFileObject(filePath);
                if (file == null) {
                    operation.setErrorMessage("File not found: " + filePath);
                    return operation;
                }
                
                file.delete();
                operation.setResult(true);
                operation.setSuccess(true);
                notifyListeners(operation);
                
                LOG.info("File deleted successfully: " + filePath);
                return operation;
                
            } catch (IOException e) {
                operation.setErrorMessage("IO Error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Failed to delete file: " + filePath, e);
            } catch (Exception e) {
                operation.setErrorMessage("Unexpected error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Unexpected error deleting file: " + filePath, e);
            }
            
            return operation;
        });
    }
    
    /**
     * Renames a file or directory asynchronously.
     * @param filePath The current file path
     * @param newName The new name
     * @return CompletableFuture with the operation result
     */
    public CompletableFuture<FileOperation> renameAsync(String filePath, String newName) {
        return CompletableFuture.supplyAsync(() -> {
            String operationId = "rename_" + System.currentTimeMillis();
            FileOperation operation = new FileOperation(operationId, "rename", filePath);
            pendingOperations.put(operationId, operation);
            
            try {
                FileObject file = getFileObject(filePath);
                if (file == null) {
                    operation.setErrorMessage("File not found: " + filePath);
                    return operation;
                }
                
                FileLock lock = file.lock();
                try {
                    file.rename(lock, newName, file.getExt());
                    operation.setResult(file.getPath());
                    operation.setSuccess(true);
                    notifyListeners(operation);
                    
                    LOG.info("File renamed successfully: " + filePath + " -> " + newName);
                    return operation;
                } finally {
                    lock.releaseLock();
                }
                
            } catch (IOException e) {
                operation.setErrorMessage("IO Error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Failed to rename file: " + filePath, e);
            } catch (Exception e) {
                operation.setErrorMessage("Unexpected error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Unexpected error renaming file: " + filePath, e);
            }
            
            return operation;
        });
    }
    
    /**
     * Copies a file or directory asynchronously.
     * @param sourcePath The source path
     * @param targetPath The target path
     * @return CompletableFuture with the operation result
     */
    public CompletableFuture<FileOperation> copyAsync(String sourcePath, String targetPath) {
        return CompletableFuture.supplyAsync(() -> {
            String operationId = "copy_" + System.currentTimeMillis();
            FileOperation operation = new FileOperation(operationId, "copy", sourcePath + " -> " + targetPath);
            pendingOperations.put(operationId, operation);
            
            try {
                FileObject source = getFileObject(sourcePath);
                if (source == null) {
                    operation.setErrorMessage("Source file not found: " + sourcePath);
                    return operation;
                }
                
                FileObject targetParent = getFileObject(new File(targetPath).getParent());
                if (targetParent == null) {
                    operation.setErrorMessage("Target parent directory not found: " + targetPath);
                    return operation;
                }
                
                FileObject target = FileUtil.copyFile(source, targetParent, new File(targetPath).getName());
                operation.setResult(target);
                operation.setSuccess(true);
                notifyListeners(operation);
                
                LOG.info("File copied successfully: " + sourcePath + " -> " + targetPath);
                return operation;
                
            } catch (IOException e) {
                operation.setErrorMessage("IO Error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Failed to copy file: " + sourcePath + " -> " + targetPath, e);
            } catch (Exception e) {
                operation.setErrorMessage("Unexpected error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Unexpected error copying file: " + sourcePath + " -> " + targetPath, e);
            }
            
            return operation;
        });
    }
    
    /**
     * Moves a file or directory asynchronously.
     * @param sourcePath The source path
     * @param targetPath The target path
     * @return CompletableFuture with the operation result
     */
    public CompletableFuture<FileOperation> moveAsync(String sourcePath, String targetPath) {
        return CompletableFuture.supplyAsync(() -> {
            String operationId = "move_" + System.currentTimeMillis();
            FileOperation operation = new FileOperation(operationId, "move", sourcePath + " -> " + targetPath);
            pendingOperations.put(operationId, operation);
            
            try {
                FileObject source = getFileObject(sourcePath);
                if (source == null) {
                    operation.setErrorMessage("Source file not found: " + sourcePath);
                    return operation;
                }
                
                FileObject targetParent = getFileObject(new File(targetPath).getParent());
                if (targetParent == null) {
                    operation.setErrorMessage("Target parent directory not found: " + targetPath);
                    return operation;
                }
                
                FileObject target = FileUtil.moveFile(source, targetParent, new File(targetPath).getName());
                operation.setResult(target);
                operation.setSuccess(true);
                notifyListeners(operation);
                
                LOG.info("File moved successfully: " + sourcePath + " -> " + targetPath);
                return operation;
                
            } catch (IOException e) {
                operation.setErrorMessage("IO Error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Failed to move file: " + sourcePath + " -> " + targetPath, e);
            } catch (Exception e) {
                operation.setErrorMessage("Unexpected error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Unexpected error moving file: " + sourcePath + " -> " + targetPath, e);
            }
            
            return operation;
        });
    }
    
    /**
     * Reads file content asynchronously.
     * @param filePath The file path
     * @return CompletableFuture with the file content
     */
    public CompletableFuture<String> readFileAsync(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                FileObject file = getFileObject(filePath);
                if (file == null) {
                    throw new IOException("File not found: " + filePath);
                }
                
                if (file.isFolder()) {
                    throw new IOException("Path is a directory: " + filePath);
                }
                
                try (InputStream is = file.getInputStream()) {
                    byte[] buffer = is.readAllBytes();
                    return new String(buffer);
                }
                
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Failed to read file: " + filePath, e);
                throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Unexpected error reading file: " + filePath, e);
                throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Writes content to a file asynchronously.
     * @param filePath The file path
     * @param content The content to write
     * @param append Whether to append to existing content
     * @return CompletableFuture with the operation result
     */
    public CompletableFuture<FileOperation> writeFileAsync(String filePath, String content, boolean append) {
        return CompletableFuture.supplyAsync(() -> {
            String operationId = "write_" + System.currentTimeMillis();
            FileOperation operation = new FileOperation(operationId, "write", filePath);
            pendingOperations.put(operationId, operation);
            
            try {
                FileObject file = getFileObject(filePath);
                if (file == null) {
                    // Create file if it doesn't exist
                    File parentFile = new File(filePath).getParentFile();
                    if (parentFile != null) {
                        FileObject parent = getFileObject(parentFile.getAbsolutePath());
                        if (parent != null) {
                            file = parent.createData(new File(filePath).getName());
                        } else {
                            operation.setErrorMessage("Parent directory not found");
                            return operation;
                        }
                    } else {
                        operation.setErrorMessage("Invalid file path");
                        return operation;
                    }
                }
                
                try (OutputStream os = file.getOutputStream()) {
                    if (append) {
                        // For append, we need to read existing content first
                        String existingContent = "";
                        try (InputStream is = file.getInputStream()) {
                            byte[] buffer = is.readAllBytes();
                            existingContent = new String(buffer);
                        }
                        os.write((existingContent + content).getBytes());
                    } else {
                        os.write(content.getBytes());
                    }
                }
                
                operation.setResult(content.length());
                operation.setSuccess(true);
                notifyListeners(operation);
                
                LOG.info("File written successfully: " + filePath);
                return operation;
                
            } catch (IOException e) {
                operation.setErrorMessage("IO Error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Failed to write file: " + filePath, e);
            } catch (Exception e) {
                operation.setErrorMessage("Unexpected error: " + e.getMessage());
                LOG.log(Level.SEVERE, "Unexpected error writing file: " + filePath, e);
            }
            
            return operation;
        });
    }
    
    /**
     * Starts watching a file or directory for changes.
     * @param watchPath The path to watch
     * @param recursive Whether to watch recursively
     * @return The FileWatcher instance
     */
    public FileWatcher startWatching(String watchPath, boolean recursive) {
        try {
            FileObject file = getFileObject(watchPath);
            if (file == null) {
                LOG.warning("Cannot watch non-existent path: " + watchPath);
                return null;
            }
            
            FileWatcher watcher = new FileWatcher(watchPath, null, recursive);
            file.addFileChangeListener(watcher);
            watcher.setActive(true);
            
            watchers.put(watchPath, watcher);
            
            for (FileSystemListener listener : listeners) {
                try {
                    listener.onFileWatchStarted(watchPath);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error notifying listener", e);
                }
            }
            
            LOG.info("Started watching: " + watchPath);
            return watcher;
            
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to start watching: " + watchPath, e);
            return null;
        }
    }
    
    /**
     * Stops watching a file or directory.
     * @param watchPath The path to stop watching
     */
    public void stopWatching(String watchPath) {
        FileWatcher watcher = watchers.remove(watchPath);
        if (watcher != null) {
            try {
                FileObject file = getFileObject(watchPath);
                if (file != null) {
                    file.removeFileChangeListener(watcher);
                }
                
                watcher.setActive(false);
                
                for (FileSystemListener listener : listeners) {
                    try {
                        listener.onFileWatchStopped(watchPath);
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Error notifying listener", e);
                    }
                }
                
                LOG.info("Stopped watching: " + watchPath);
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to stop watching: " + watchPath, e);
            }
        }
    }
    
    /**
     * Gets the project for a given file.
     * @param filePath The file path
     * @return The Project or null if not found
     */
    public Project getProjectForFile(String filePath) {
        try {
            FileObject file = getFileObject(filePath);
            if (file != null) {
                return FileOwnerQuery.getOwner(file);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get project for file: " + filePath, e);
        }
        return null;
    }
    
    /**
     * Lists files in a directory asynchronously.
     * @param dirPath The directory path
     * @param recursive Whether to list recursively
     * @return CompletableFuture with the list of files
     */
    public CompletableFuture<List<FileObject>> listFilesAsync(String dirPath, boolean recursive) {
        return CompletableFuture.supplyAsync(() -> {
            List<FileObject> files = new ArrayList<>();
            
            try {
                FileObject dir = getFileObject(dirPath);
                if (dir == null || !dir.isFolder()) {
                    return files;
                }
                
                if (recursive) {
                    listFilesRecursive(dir, files);
                } else {
                    for (FileObject child : dir.getChildren()) {
                        files.add(child);
                    }
                }
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to list files: " + dirPath, e);
            }
            
            return files;
        });
    }
    
    /**
     * Recursively lists files in a directory.
     * @param dir The directory to list
     * @param files The list to add files to
     */
    private void listFilesRecursive(FileObject dir, List<FileObject> files) {
        try {
            for (FileObject child : dir.getChildren()) {
                files.add(child);
                if (child.isFolder()) {
                    listFilesRecursive(child, files);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to list files recursively", e);
        }
    }
    
    /**
     * Adds a file system listener.
     * @param listener The listener to add
     */
    public void addFileSystemListener(FileSystemListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a file system listener.
     * @param listener The listener to remove
     */
    public void removeFileSystemListener(FileSystemListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifies listeners about a file operation.
     * @param operation The operation
     */
    private void notifyListeners(FileOperation operation) {
        for (FileSystemListener listener : listeners) {
            try {
                listener.onFileOperation(operation);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    /**
     * Gets all pending operations.
     * @return Copy of pending operations
     */
    public Map<String, FileOperation> getPendingOperations() {
        return new HashMap<>(pendingOperations);
    }
    
    /**
     * Gets a pending operation by ID.
     * @param operationId The operation ID
     * @return The operation or null if not found
     */
    public FileOperation getPendingOperation(String operationId) {
        return pendingOperations.get(operationId);
    }
    
    /**
     * Clears completed operations.
     */
    public void clearCompletedOperations() {
        pendingOperations.entrySet().removeIf(entry -> entry.getValue().isCompleted());
    }
    
    /**
     * Gets all active watchers.
     * @return Copy of active watchers
     */
    public Map<String, FileWatcher> getActiveWatchers() {
        Map<String, FileWatcher> active = new HashMap<>();
        for (Map.Entry<String, FileWatcher> entry : watchers.entrySet()) {
            if (entry.getValue().isActive()) {
                active.put(entry.getKey(), entry.getValue());
            }
        }
        return active;
    }
}
