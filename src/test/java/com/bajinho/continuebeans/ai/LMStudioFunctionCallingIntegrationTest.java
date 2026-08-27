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
import static org.junit.jupiter.api.Assertions.*;

class LMStudioFunctionCallingIntegrationTest {
 private HttpServer server; private String baseUrl;
 @BeforeEach void setUp() throws IOException{server=HttpServer.create(new InetSocketAddress(0),0);baseUrl="http://127.0.0.1:"+server.getAddress().getPort();server.start();}
 @AfterEach void tearDown(){server.stop(0);}
 @Test void processRequestReturnsNormalAssistantContent() throws Exception {server.createContext("/v1/chat/completions",e->{assertEquals("POST",e.getRequestMethod());String request=new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);assertTrue(request.contains("\"model\":\"test-model\""));assertTrue(request.contains("\"function_call\":\"auto\""));assertTrue(request.contains("\"stream\":false"));send(e,200,"{\"choices\":[{\"message\":{\"content\":\"hello from lm studio\"}}]}");});assertEquals("hello from lm studio",new LMStudioFunctionCallingIntegration(baseUrl,"test-model").processRequest("hello").get());}
 @Test void processRequestExecutesFunctionCallAndFormatsResult() throws Exception {server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"I'll add it\",\"function_call\":{\"name\":\"add_dependency\",\"arguments\":\"{\\\"groupId\\\":\\\"org.example\\\",\\\"artifactId\\\":\\\"demo\\\",\\\"version\\\":\\\"1.0.0\\\"}\"}}}] }"));String result=new LMStudioFunctionCallingIntegration(baseUrl,"test-model").processRequest("add dependency").get();assertTrue(result.contains("Função NetBeans executada com sucesso"));assertTrue(result.contains("groupId: org.example"));}
 @Test void functionErrorIsReported() throws Exception {server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"oops\",\"function_call\":{\"name\":\"missing_function\",\"arguments\":\"{}\"}}}] }"));String result=new LMStudioFunctionCallingIntegration(baseUrl,"test-model").processRequest("do it").get();assertTrue(result.contains("Erro ao executar função"));assertTrue(result.contains("Unknown function"));}
 @Test void malformedResponseDoesNotEscapeAsException() throws Exception {server.createContext("/v1/chat/completions",e->send(e,200,"{}"));String result=new LMStudioFunctionCallingIntegration(baseUrl,"test-model").processRequest("hello").get();assertTrue(result.contains("Erro ao extrair resposta"));}
 @Test void httpErrorIsReturnedAsDiagnostic() throws Exception {server.createContext("/v1/chat/completions",e->send(e,500,"failure"));String result=new LMStudioFunctionCallingIntegration(baseUrl,"test-model").processRequest("hello").get();assertTrue(result.startsWith("❌ Erro:"));assertTrue(result.contains("500"));}
 @Test void testConnectionReportsAvailability() throws Exception {server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}"));assertTrue(new LMStudioFunctionCallingIntegration(baseUrl+"/","test-model").testConnection().get());}
 @Test void testConnectionReturnsFalseOnFailure() throws Exception {server.createContext("/v1/chat/completions",e->send(e,503,"unavailable"));assertFalse(new LMStudioFunctionCallingIntegration(baseUrl,"test-model").testConnection().get());}
 @Test void availableFunctionsAreExposed(){var f=new LMStudioFunctionCallingIntegration(baseUrl,"test-model").getAvailableFunctions();assertNotNull(f);assertFalse(f.isEmpty());}
 private static void send(HttpExchange e,int status,String body)throws IOException{byte[] b=body.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type","application/json");e.sendResponseHeaders(status,b.length);try(OutputStream out=e.getResponseBody()){out.write(b);}}
}
