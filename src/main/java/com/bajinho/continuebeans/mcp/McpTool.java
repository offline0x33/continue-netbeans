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

import java.util.Objects;

/**
 * Represents an MCP (Model Context Protocol) tool.
 * 
 * <p>MCP tools allow AI models to interact with external systems and APIs in a
 * standardized way. This class encapsulates all the metadata and configuration
 * needed to integrate an external tool with the AI assistant.</p>
 * 
 * <h3>Tool Structure:</h3>
 * <ul>
 *   <li><b>name:</b> Unique identifier for the tool</li>
 *   <li><b>description:</b> Human-readable description for the AI model</li>
 *   <li><b>provider:</b> Tool provider/category (e.g., "netbeans", "filesystem", "api")</li>
 *   <li><b>enabled:</b> Whether the tool is currently active</li>
 *   <li><b>endpoint:</b> Optional HTTP endpoint for remote tools</li>
 *   <li><b>schema:</b> JSON schema defining tool parameters</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * McpTool fileTool = new McpTool(
 *     "read_file",
 *     "Read the contents of a file",
 *     "filesystem",
 *     true,
 *     "http://localhost:8080/api/files/read",
 *     "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\"}}}"
 * );
 * }</pre>
 * 
 * <h3>Integration:</h3>
 * <p>Tools are managed by {@link McpToolManager} and can be automatically
 * integrated with NetBeans function definitions for seamless IDE integration.</p>
 * 
 * @author Continue Beans Team
 * @version 1.0
 * @see McpToolManager
 */
public class McpTool {
    
    private String name;
    private String description;
    private String provider;
    private boolean enabled;
    private String endpoint;
    private String schema;
    
    public McpTool(String name, String description, String provider, boolean enabled) {
        this.name = name;
        this.description = description;
        this.provider = provider;
        this.enabled = enabled;
    }
    
    public McpTool(String name, String description, String provider, boolean enabled, 
                   String endpoint, String schema) {
        this.name = name;
        this.description = description;
        this.provider = provider;
        this.enabled = enabled;
        this.endpoint = endpoint;
        this.schema = schema;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        McpTool mcpTool = (McpTool) o;
        return enabled == mcpTool.enabled && 
               Objects.equals(name, mcpTool.name) && 
               Objects.equals(description, mcpTool.description) && 
               Objects.equals(provider, mcpTool.provider);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, description, provider, enabled);
    }
    
    @Override
    public String toString() {
        return "McpTool{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", provider='" + provider + '\'' +
               ", enabled=" + enabled +
               '}';
    }
}