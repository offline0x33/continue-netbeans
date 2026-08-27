package com.bajinho.continuebeans.ai;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class OllamaIntegrationTest {
 private HttpServer server; private String baseUrl;
 @BeforeEach void start() throws Exception{server=HttpServer.create(new InetSocketAddress(0),0);baseUrl="http://127.0.0.1:"+server.getAddress().getPort()+"/";server.start();}
 @AfterEach void stop(){server.stop(0);}
 @Test void plainResponseAndPayload() throws Exception{server.createContext("/v1/chat/completions",e->{String b=new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);assertTrue(b.contains("\"model\":\"m\""));assertTrue(b.contains("\"stream\":false"));send(e,200,"{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}");});assertEquals("ok",new OllamaIntegration(baseUrl,"m").processRequest("hello").get());}
 @Test void executeTextFunction() throws Exception{server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"**EXECUTE:** add_dependency(groupId=org.example, artifactId=demo, version=1.0)\"}}] }"));String r=new OllamaIntegration(baseUrl,"m").processRequest("x").get();assertTrue(r.contains("Operação NetBeans executada com sucesso"));assertTrue(r.contains("org.example"));}
 @Test void unknownFunctionAndHttpError() throws Exception{server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"**EXECUTE:** missing_function()\"}}] }"));String r=new OllamaIntegration(baseUrl,"m").processRequest("x").get();assertTrue(r.contains("Unknown function"));}
 @Test void serverError() throws Exception{server.createContext("/v1/chat/completions",e->send(e,500,"boom"));String r=new OllamaIntegration(baseUrl,"m").processRequest("x").get();assertTrue(r.contains("500"));assertTrue(r.startsWith("❌ Erro:"));}
 @Test void malformedResponse() throws Exception{server.createContext("/v1/chat/completions",e->send(e,200,"{}"));String r=new OllamaIntegration(baseUrl,"m").processRequest("x").get();assertTrue(r.contains("Erro ao extrair resposta"));}
 @Test void connectionAndDirectFunction() throws Exception{server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{}]}"));var i=new OllamaIntegration(baseUrl,"m");assertTrue(i.testConnection().get());assertTrue(i.executeFunction("add_dependency",Map.of("groupId","g","artifactId","a","version","1")).get().isSuccess());}
 private static void send(com.sun.net.httpserver.HttpExchange e,int s,String body)throws IOException{byte[]b=body.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type","application/json");e.sendResponseHeaders(s,b.length);try(OutputStream o=e.getResponseBody()){o.write(b);}}
}
