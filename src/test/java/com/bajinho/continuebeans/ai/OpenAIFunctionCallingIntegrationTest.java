package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class OpenAIFunctionCallingIntegrationTest {
    @Test void exposesFunctions() { var i=new OpenAIFunctionCallingIntegration("key","model"); assertFalse(i.getAvailableFunctions().isEmpty()); }
    @Test void directExecutionWorks() throws Exception { var i=new OpenAIFunctionCallingIntegration("key","model"); var r=i.executeFunction("add_dependency",Map.of("groupId","org.example","artifactId","demo","version","1")).get(); assertTrue(r.isSuccess()); }
    @Test void defaultIntentReturnsHelpfulResponse() throws Exception { var i=new OpenAIFunctionCallingIntegration("key","model"); assertTrue(i.processRequest("hello").get().contains("NetBeans")); }
    @Test void createClassIntentExecutesFunction() throws Exception { var i=new OpenAIFunctionCallingIntegration("key","model"); var r=i.processRequest("criar classe").get(); assertTrue(r.contains("UserService")); assertTrue(r.contains("Função executada com sucesso")); }
    @Test void readIntentExposesRealFileError() throws Exception { var i=new OpenAIFunctionCallingIntegration("key","model"); var r=i.processRequest("ler arquivo").get(); assertTrue(r.contains("File not found")); }
    @Test void englishCreateIntentExecutes() throws Exception { var i=new OpenAIFunctionCallingIntegration("key","model"); assertTrue(i.processRequest("generate class").get().contains("UserService")); }
    @Test void englishReadIntentExecutes() throws Exception { var i=new OpenAIFunctionCallingIntegration("key","model"); assertTrue(i.processRequest("read file").get().contains("File not found")); }
    @Test void projectIntentDoesNotThrow() throws Exception { var i=new OpenAIFunctionCallingIntegration("key","model"); assertNotNull(i.processRequest("project").get()); }
}
