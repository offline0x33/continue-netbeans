package com.bajinho.continuebeans;

import com.bajinho.continuebeans.netbeans.NetBeansLanguageService;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class NetBeansLanguageServiceTest {
    @Test
    void rejectsMissingJavaSource() {
        assertThrows(IOException.class,
                () -> NetBeansLanguageService.analyzeJavaFile("/path/that/does/not/exist.java"));
    }
}
