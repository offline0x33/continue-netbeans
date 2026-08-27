package com.bajinho.continuebeans.ai;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class LMStudioFunctionCallingIntegrationTest {
 private HttpServer server; private String baseUrl;
 @BeforeEach void start() throws Exception { server=HttpServer.create(new InetSocketAddress(0),0); baseUrl="http://127.0.0.1:"+server.getAddress().getPort()+"/"; server.start(); }
 @AfterEach void stop(){server.stop(0);}
 @Test void textResponseAndPayload() throws Exception { server.createContext("/v1/chat/completions",e->{String b=new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8); assertTrue(b.contains("\"model\":\"m\"")); assertTrue(b.contains("\"functions\"")); send(e,200,"{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}");}); var i=new LMStudioFunctionCallingIntegration(baseUrl,"m"); assertEquals("ok",i.processRequest("hello").get()); }
 @Test void executesFunctionCall() throws Exception { server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"do\",\"function_call\":{\"name\":\"add_dependency\",\"arguments\":\"{\\\"groupId\\\":\\\"org.example\\\",\\\"artifactId\\\":\\\"demo\\\",\\\"version\\\":\\\"1.0\\\"}\"}}}]}}")); var r=new LMStudioFunctionCallingIntegration(baseUrl,"m").processRequest("x").get(); assertTrue(r.contains("Função NetBeans executada com sucesso")); assertTrue(r.contains("org.example")); }
 @Test void unknownFunctionIsReported() throws Exception { server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"x\",\"function_call\":{\"name\":\"missing_function\",\"arguments\":\"{}\"}}}] }")); var r=new LMStudioFunctionCallingIntegration(baseUrl,"m").processRequest("x").get(); assertTrue(r.contains("Unknown function")); }
 @Test void httpErrorIsReported() throws Exception { server.createContext("/v1/chat/completions",e->send(e,500,"boom")); var r=new LMStudioFunctionCallingIntegration(baseUrl,"m").processRequest("x").get(); assertTrue(r.contains("500")); }
 @Test void malformedResponseIsHandled() throws Exception { server.createContext("/v1/chat/completions",e->send(e,200,"{}")); var r=new LMStudioFunctionCallingIntegration(baseUrl,"m").processRequest("x").get(); assertTrue(r.contains("Erro ao extrair resposta")); }
 @Test void connectionAndModelsAreCovered() throws Exception { server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{}]}")); server.createContext("/v1/models",e->send(e,200,"{\"data\":[{\"id\":\"a\"},{\"id\":\"b\"}]}")); var i=new LMStudioFunctionCallingIntegration(baseUrl,"m"); assertTrue(i.testConnection().get()); assertEquals(java.util.List.of("a","b"),i.getAvailableModels().get()); }
 @Test void modelsErrorReturnsErrorEntry() throws Exception { server.createContext("/v1/models",e->send(e,503,"bad")); var m=new LMStudioFunctionCallingIntegration(baseUrl,"m").getAvailableModels().get(); assertEquals(1,m.size()); assertTrue(m.get(0).startsWith("Error:")); }
 @Test void directExecutionAndFunctionsAreAvailable() throws Exception { var i=new LMStudioFunctionCallingIntegration(baseUrl,"m"); assertFalse(i.getAvailableFunctions().isEmpty()); assertTrue(i.executeFunction("add_dependency",Map.of("groupId","g","artifactId","a","version","1")).get().isSuccess()); }
 private static void send(HttpExchange e,int status,String body)throws IOException{byte[] b=body.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type","application/json");e.sendResponseHeaders(status,b.length);try(OutputStream o=e.getResponseBody()){o.write(b);}}
}
