/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.bajinho.continuebeans.mcp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages MCP (Model Context Protocol) tools.
 * Handles loading, saving, and managing MCP tool configurations.
 * 
 * @author Continue Beans Team
 */
public class McpToolManager {
    
    private static final Logger LOG = Logger.getLogger(McpToolManager.class.getName());
    private static final String TOOLS_CONFIG_FILE = "mcp-tools.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final Path configPath;
    private List<McpTool> tools;
    
    public McpToolManager() {
        this.configPath = getConfigPath();
        this.tools = new ArrayList<>();
    }
    
    /**
     * Get the configuration file path.
     */
    private Path getConfigPath() {
        String userHome = System.getProperty("user.home");
        Path configDir = Paths.get(userHome, ".continue-beans");
        
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException e) {
            LOG.warning("Could not create config directory: " + e.getMessage());
        }
        
        return configDir.resolve(TOOLS_CONFIG_FILE);
    }
    
    /**
     * Load MCP tools from configuration file.
     */
    public List<McpTool> loadTools() {
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                Type listType = new TypeToken<ArrayList<McpTool>>(){}.getType();
                tools = GSON.fromJson(reader, listType);
                LOG.info("Loaded " + tools.size() + " MCP tools from configuration");
            } catch (IOException e) {
                LOG.warning("Could not load MCP tools: " + e.getMessage());
                tools = new ArrayList<>();
            }
        } else {
            tools = new ArrayList<>();
            LOG.info("No MCP tools configuration found, starting with empty list");
        }
        
        return tools;
    }
    
    /**
     * Save MCP tools to configuration file.
     */
    public void saveTools() {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(tools, writer);
            LOG.info("Saved " + tools.size() + " MCP tools to configuration");
        } catch (IOException e) {
            LOG.severe("Could not save MCP tools: " + e.getMessage());
        }
    }
    
    /**
     * Add a new MCP tool.
     */
    public void addTool(McpTool tool) {
        if (tool != null && !tools.contains(tool)) {
            tools.add(tool);
            saveTools();
            LOG.info("Added MCP tool: " + tool.getName());
        }
    }
    
    /**
     * Save a single MCP tool (alias for addTool).
     */
    public void saveTool(McpTool tool) {
        addTool(tool);
    }
    
    /**
     * Remove an MCP tool.
     */
    public void removeTool(McpTool tool) {
        if (tools.remove(tool)) {
            saveTools();
            LOG.info("Removed MCP tool: " + tool.getName());
        }
    }
    
    /**
     * Update an existing MCP tool.
     */
    public void updateTool(McpTool oldTool, McpTool newTool) {
        int index = tools.indexOf(oldTool);
        if (index != -1) {
            tools.set(index, newTool);
            saveTools();
            LOG.info("Updated MCP tool: " + newTool.getName());
        }
    }
    
    /**
     * Get all MCP tools.
     */
    public List<McpTool> getTools() {
        return new ArrayList<>(tools);
    }
    
    /**
     * Get enabled MCP tools only.
     */
    public List<McpTool> getEnabledTools() {
        List<McpTool> enabledTools = new ArrayList<>();
        for (McpTool tool : tools) {
            if (tool.isEnabled()) {
                enabledTools.add(tool);
            }
        }
        return enabledTools;
    }
    
    /**
     * Get tools by provider.
     */
    public List<McpTool> getToolsByProvider(String provider) {
        List<McpTool> providerTools = new ArrayList<>();
        for (McpTool tool : tools) {
            if (provider.equals(tool.getProvider())) {
                providerTools.add(tool);
            }
        }
        return providerTools;
    }
    
    /**
     * Enable/disable an MCP tool.
     */
    public void setToolEnabled(String toolName, boolean enabled) {
        for (McpTool tool : tools) {
            if (toolName.equals(tool.getName())) {
                tool.setEnabled(enabled);
                saveTools();
                LOG.info("Tool " + toolName + " " + (enabled ? "enabled" : "disabled"));
                return;
            }
        }
    }
    
    /**
     * Find a tool by name.
     */
    public McpTool findTool(String name) {
        for (McpTool tool : tools) {
            if (name.equals(tool.getName())) {
                return tool;
            }
        }
        return null;
    }
    
    /**
     * Clear all MCP tools.
     */
    public void clearTools() {
        tools.clear();
        saveTools();
        LOG.info("Cleared all MCP tools");
    }
    
    /**
     * Import tools from a JSON string.
     */
    public void importToolsFromJson(String json) {
        try {
            Type listType = new TypeToken<ArrayList<McpTool>>(){}.getType();
            List<McpTool> importedTools = GSON.fromJson(json, listType);
            
            for (McpTool tool : importedTools) {
                if (!tools.contains(tool)) {
                    tools.add(tool);
                }
            }
            
            saveTools();
            LOG.info("Imported " + importedTools.size() + " MCP tools from JSON");
        } catch (Exception e) {
            LOG.severe("Could not import MCP tools from JSON: " + e.getMessage());
        }
    }
    
    /**
     * Export tools to JSON string.
     */
    public String exportToolsToJson() {
        return GSON.toJson(tools);
    }
}