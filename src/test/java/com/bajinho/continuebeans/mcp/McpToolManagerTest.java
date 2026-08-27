package com.bajinho.continuebeans.mcp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class McpToolManagerTest {
 private Path configFile;
 @BeforeEach void setUp() throws Exception { Path dir=Paths.get(System.getProperty("user.home"),".continue-beans"); Files.createDirectories(dir); configFile=dir.resolve("mcp-tools.json"); Files.deleteIfExists(configFile); }
 @AfterEach void tearDown() throws Exception { Files.deleteIfExists(configFile); }
 @Test void startsEmptyWhenConfigurationDoesNotExist(){McpToolManager m=new McpToolManager(); assertTrue(m.loadTools().isEmpty()); assertTrue(m.getTools().isEmpty());}
 @Test void addSaveLoadAndFindTool(){McpTool t=tool("read","filesystem",true); McpToolManager m=new McpToolManager(); m.addTool(t); assertEquals(List.of(t),m.getTools()); assertSame(t,m.findTool("read")); assertNull(m.findTool("missing")); assertTrue(Files.exists(configFile)); McpToolManager r=new McpToolManager(); assertEquals(List.of(t),r.loadTools());}
 @Test void duplicateAndNullToolsAreIgnored(){McpToolManager m=new McpToolManager(); McpTool t=tool("read","filesystem",true); m.addTool(null); m.addTool(t); m.saveTool(t); assertEquals(1,m.getTools().size());}
 @Test void enabledAndProviderFiltersWork(){McpTool a=tool("read","filesystem",true),b=tool("write","filesystem",false),c=tool("build","maven",true); McpToolManager m=new McpToolManager(); m.addTool(a);m.addTool(b);m.addTool(c); assertEquals(List.of(a,c),m.getEnabledTools()); assertEquals(List.of(a,b),m.getToolsByProvider("filesystem")); assertTrue(m.getToolsByProvider("missing").isEmpty());}
 @Test void updateRemoveAndEnableOperationsPersistState(){McpTool a=tool("read","filesystem",true),b=tool("read-v2","filesystem",false); McpToolManager m=new McpToolManager();m.addTool(a);m.updateTool(a,b);assertEquals(List.of(b),m.getTools());assertNull(m.findTool("read"));m.setToolEnabled("read-v2",true);assertTrue(m.findTool("read-v2").isEnabled());m.setToolEnabled("missing",true);m.removeTool(new McpTool("missing","x","x",true));m.removeTool(b);assertTrue(m.getTools().isEmpty());}
 @Test void exportAndImportJsonRoundTrip(){McpTool a=new McpTool("read","Read files","filesystem",true,"http://localhost/read","{\"type\":\"object\"}"),b=tool("build","maven",false);McpToolManager s=new McpToolManager();s.addTool(a);s.addTool(b);String json=s.exportToolsToJson();assertTrue(json.contains("read"));assertTrue(json.contains("localhost/read"));McpToolManager t=new McpToolManager();t.importToolsFromJson(json);assertEquals(s.getTools(),t.getTools());t.importToolsFromJson(json);assertEquals(2,t.getTools().size());}
 @Test void invalidJsonDoesNotDestroyExistingTools(){McpToolManager m=new McpToolManager();McpTool t=tool("read","filesystem",true);m.addTool(t);m.importToolsFromJson("not-json");assertEquals(List.of(t),m.getTools());}
 @Test void clearToolsPersistsEmptyConfiguration() throws Exception{McpToolManager m=new McpToolManager();m.addTool(tool("read","filesystem",true));m.clearTools();assertTrue(m.getTools().isEmpty());assertTrue(Files.readString(configFile).contains("[]"));}
 @Test void getToolsReturnsDefensiveList(){McpToolManager m=new McpToolManager();m.addTool(tool("read","filesystem",true));List<McpTool> copy=m.getTools();copy.clear();assertEquals(1,m.getTools().size());}
 private static McpTool tool(String name,String provider,boolean enabled){return new McpTool(name,name+" description",provider,enabled);}
}
