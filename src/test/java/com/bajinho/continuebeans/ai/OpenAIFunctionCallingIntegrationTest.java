package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class OpenAIFunctionCallingIntegrationTest {
 @Test void exposesAllAvailableNetBeansFunctions(){OpenAIFunctionCallingIntegration i=new OpenAIFunctionCallingIntegration("test-key","test-model");List<NetBeansFunctionDefinitions.FunctionDefinition> f=i.getAvailableFunctions();assertNotNull(f);assertFalse(f.isEmpty());assertTrue(f.stream().anyMatch(x->"read_file".equals(x.getName())));assertTrue(f.stream().anyMatch(x->"generate_class".equals(x.getName())));}
 @Test void directFunctionExecutionIsDelegated() throws Exception {var r=new OpenAIFunctionCallingIntegration("test-key","test-model").executeFunction("add_dependency",Map.of("groupId","org.example","artifactId","demo","version","1.0.0")).get();assertTrue(r.isSuccess());assertEquals("org.example",r.getData().get("groupId"));assertEquals("demo",r.getData().get("artifactId"));}
 @Test void unknownIntentUsesDefaultAssistantResponse() throws Exception {String r=new OpenAIFunctionCallingIntegration("test-key","test-model").processRequest("tell me something unrelated").get();assertTrue(r.contains("assistente AI"));assertTrue(r.contains("NetBeans Platform"));}
 @Test void createClassAndEnglishIntentAreRecognized() throws Exception {assertFalse(new OpenAIFunctionCallingIntegration("test-key","test-model").processRequest("criar classe").get().isBlank());assertFalse(new OpenAIFunctionCallingIntegration("test-key","test-model").processRequest("generate class").get().isBlank());}
 @Test void readFileAndProjectIntentsAreRecognized() throws Exception {var i=new OpenAIFunctionCallingIntegration("test-key","test-model");assertFalse(i.processRequest("ler arquivo").get().isBlank());assertFalse(i.processRequest("read file").get().isBlank());assertFalse(i.processRequest("projeto").get().isBlank());assertFalse(i.processRequest("project").get().isBlank());}
 @Test void functionDefinitionExposesDescriptionAndParameters(){var f=new OpenAIFunctionCallingIntegration("test-key","test-model").getAvailableFunctions().stream().filter(x->"add_dependency".equals(x.getName())).findFirst().orElseThrow();assertNotNull(f.getDescription());assertFalse(f.getDescription().isBlank());assertNotNull(f.getParameters());assertFalse(f.getParameters().isEmpty());}
}
