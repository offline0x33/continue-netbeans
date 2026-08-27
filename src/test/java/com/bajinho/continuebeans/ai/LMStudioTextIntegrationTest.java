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

class LMStudioTextIntegrationTest {
    private HttpServer server;
    private String baseUrl;
    @BeforeEach void setUp() throws IOException { server=HttpServer.create(new InetSocketAddress(0),0); baseUrl="http://127.0.0.1:"+server.getAddress().getPort()+"/"; server.start(); }
    @AfterEach void tearDown(){ server.stop(0); }
    @Test void constructorNormalizesTrailingSlashAndDirectFunctionExecutionWorks() throws Exception {
        LMStudioTextIntegration integration=new LMStudioTextIntegration(baseUrl,"test-model");
        var result=integration.executeFunction("add_dependency",Map.of("groupId","org.example","artifactId","demo","version","1.0.0")).get();
        assertTrue(result.isSuccess()); assertEquals("org.example",result.getData().get("groupId")); assertTrue(((String)result.getData().get("dependencyXml")).contains("demo"));
    }
    @Test void processRequestReturnsPlainAssistantContentAndSendsExpectedPayload() throws Exception {
        server.createContext("/v1/chat/completions",e->{ assertEquals("POST",e.getRequestMethod()); String request=new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8); assertTrue(request.contains("\"model\":\"test-model\"")); assertTrue(request.contains("\"temperature\":0.7")); assertTrue(request.contains("\"max_tokens\":2000")); assertTrue(request.contains("Create hello")); send(e,200,"{\"choices\":[{\"message\":{\"content\":\"Hello from LM Studio\"}}]}"); });
        assertEquals("Hello from LM Studio",new LMStudioTextIntegration(baseUrl,"test-model").processRequest("Create hello").get());
    }
    @Test void processRequestExecutesTextFunctionCallAndFormatsResult() throws Exception {
        server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"I'll create it.\\n**EXECUTE:** add_dependency(groupId=org.example, artifactId=demo, version=1.2.3)\"}}] }"));
        String result=new LMStudioTextIntegration(baseUrl,"test-model").processRequest("add dependency").get();
        assertTrue(result.contains("Operação NetBeans executada com sucesso")); assertTrue(result.contains("groupId: org.example")); assertTrue(result.contains("artifactId: demo")); assertFalse(result.contains("**EXECUTE:**"));
    }
    @Test void processRequestHandlesQuotedAndNestedFunctionArguments() throws Exception {
        server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"**EXECUTE:** create_file(filePath=\"nested/test.txt\", content=\"hello, world\")\"}}] }"));
        String result=new LMStudioTextIntegration(baseUrl,"test-model").processRequest("create file").get();
        assertNotNull(result); assertFalse(result.isBlank());
    }
    @Test void processRequestReturnsFunctionErrorForInvalidFunction() throws Exception {
        server.createContext("/v1/chat/completions",e->send(e,200,"{\"choices\":[{\"message\":{\"content\":\"**EXECUTE:** missing_function()\"}}] }"));
        String result=new LMStudioTextIntegration(baseUrl,"test-model").processRequest("do it").get(); assertTrue(result.contains("Erro ao executar função")); assertTrue(result.contains("Unknown function"));
    }
    @Test void processRequestHandlesHttpError() throws Exception { server.createContext("/v1/chat/completions",e->send(e,500,"server failure")); String result=new LMStudioTextIntegration(baseUrl,"test-model").processRequest("hello").get(); assertTrue(result.startsWith("❌ Erro:")); assertTrue(result.contains("500")); }
    @Test void processRequestHandlesMalformedResponse() throws Exception { server.createContext("/v1/chat/completions",e->send(e,200,"{}")); String result=new LMStudioTextIntegration(baseUrl,"test-model").processRequest("hello").get(); assertTrue(result.contains("Erro ao extrair resposta")); }
    @Test void testConnectionReturnsTrueForValidResponse() throws Exception { server.createContext("/v1/chat/completions",e->{ String request=new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8); assertTrue(request.contains("\"content\":\"Hello\"")); send(e,200,"{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}"); }); assertTrue(new LMStudioTextIntegration(baseUrl,"test-model").testConnection().get()); }
    @Test void testConnectionReturnsFalseForHttpError() throws Exception { server.createContext("/v1/chat/completions",e->send(e,503,"unavailable")); assertFalse(new LMStudioTextIntegration(baseUrl,"test-model").testConnection().get()); }
    private static void send(HttpExchange exchange,int status,String body)throws IOException{byte[] bytes=body.getBytes(StandardCharsets.UTF_8); exchange.getResponseHeaders().set("Content-Type","application/json"); exchange.sendResponseHeaders(status,bytes.length); try(OutputStream output=exchange.getResponseBody()){output.write(bytes);}}
}
