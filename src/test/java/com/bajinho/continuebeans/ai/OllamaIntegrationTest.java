package com.bajinho.continuebeans.ai;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class OllamaIntegrationTest {
 private HttpServer server; private String baseUrl;
 @BeforeEach void setUp() throws IOException{server=HttpServer.create(new InetSocketAddress(0),0);baseUrl="http://127.0.0.1:"+server.getAddress().getPort()+"/";server.start();}
 @AfterEach void tearDown(){server.stop(0);}
 @Test void processRequestReturnsAssistantContentAndBuildsOllamaPayload() throws Exception {server.createContext("/v1/chat/completions",e->{assertEquals("POST",e.getRequestMethod());String body=new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);assertTrue(body.contains("\"model\":\"test-model\""));assertTrue(body.contains("\"temperature\":0.7"));assertTrue(body.contains("\"max_tokens\":2000"));assertTrue(body.contains("\"stream\":false"));assertTrue(body.contains("hello ollama"));send(e,200,"{\"choices\":[{\"message\":{\"content\":\"Hello from Ollama\"}}]}");});assertEquals("Hello from Ollama",new OllamaIntegration(baseUrl,"test-model").processRequest("hello ollama").get());}
 @Test void processRequestExecutesTextFunctionCall() throws Exception {server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"**EXECUTE:** add_dependency(groupId=org.example, artifactId=demo, version=1.0.0)\"}}] }"));String result=new OllamaIntegration(baseUrl,"test-model").processRequest("add dependency").get();assertTrue(result.contains("Operação NetBeans executada com sucesso"));assertTrue(result.contains("groupId: org.example"));}
 @Test void processRequestReportsUnknownFunction() throws Exception {server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"**EXECUTE:** missing_function()\"}}] }"));String result=new OllamaIntegration(baseUrl,"test-model").processRequest("execute something").get();assertTrue(result.contains("Erro ao executar função"));assertTrue(result.contains("Unknown function"));}
 @Test void processRequestHandlesHttpFailure() throws Exception {server.createContext("/v1/chat/completions",e->send(e,500,"failure"));String result=new OllamaIntegration(baseUrl,"test-model").processRequest("hello").get();assertTrue(result.startsWith("❌ Erro:"));assertTrue(result.contains("500"));}
 @Test void processRequestHandlesMalformedResponse() throws Exception {server.createContext("/v1/chat/completions",e->send(e,200,"{}"));String result=new OllamaIntegration(baseUrl,"test-model").processRequest("hello").get();assertTrue(result.contains("Erro ao extrair resposta"));}
 @Test void testConnectionReturnsTrueWhenChoicesArePresent() throws Exception {server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"ok\"}}] }"));assertTrue(new OllamaIntegration(baseUrl,"test-model").testConnection().get());}
 @Test void testConnectionReturnsFalseOnServerError() throws Exception {server.createContext("/v1/chat/completions",e->send(e,503,"down"));assertFalse(new OllamaIntegration(baseUrl,"test-model").testConnection().get());}
 @Test void directFunctionExecutionIsAvailable() throws Exception {var result=new OllamaIntegration(baseUrl,"test-model").executeFunction("add_dependency",Map.of("groupId","org.example","artifactId","demo","version","1.0.0")).get();assertTrue(result.isSuccess());}
 private static void send(HttpExchange e,int status,String body)throws IOException{byte[] b=body.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type","application/json");e.sendResponseHeaders(status,b.length);try(OutputStream out=e.getResponseBody()){out.write(b);}}
}
