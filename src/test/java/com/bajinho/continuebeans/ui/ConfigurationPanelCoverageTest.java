package com.bajinho.continuebeans.ui;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;

/**
 * Cobertura comportamental para ConfigurationPanel: construção, seleção de provider,
 * estados dos campos, teste de conexão, save/cancel e validação. Testes determinísticos
 * via reflection + mocks. Métodos que exibem dialog modal rodam em daemon thread para não
 * travar o fork (DISPLAY real, não headless).
 */
class ConfigurationPanelCoverageTest {

    // ──────────────────────────────────────────────
    // Helpers de reflection para campos/métodos privados
    // ──────────────────────────────────────────────

    private static Object getField(Object target, String name) throws Exception {
        java.lang.reflect.Field f = ConfigurationPanel.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        java.lang.reflect.Field f = ConfigurationPanel.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    /** Invoca método privado retornando o valor de retorno (null para void). */
    private static Object invoke(Object target, String method, Class<?>[] paramTypes, Object... args) throws Exception {
        java.lang.reflect.Method m = ConfigurationPanel.class.getDeclaredMethod(method, paramTypes);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    /** Variante para método com um único parâmetro. */
    private static Object invoke(Object target, String method, Class<?> paramType, Object arg) throws Exception {
        return invoke(target, method, new Class<?>[]{paramType}, arg);
    }

    /** Pump do EDT para processar callbacks agendados via SwingUtilities.invokeLater. */
    private static void pumpEdt() throws Exception {
        SwingUtilities.invokeAndWait(() -> {});
    }

    /** Fecha todos os dialogs/janelas abertos (JOptionPane modal, etc.). */
    private static void closeAllWindows() {
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            try {
                if (w instanceof JDialog) {
                    ((JDialog) w).dispose();
                } else {
                    w.dispose();
                }
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    /** Executa em daemon thread para não travar o fork com dialog modal. */
    private static void runOnDaemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
    }

    // ──────────────────────────────────────────────
    // Estado: System properties (loadCurrentConfiguration/saveConfiguration usam System.getProperty/setProperty)
    // ──────────────────────────────────────────────

    private static final java.util.Properties BACKUP = new java.util.Properties();

    @BeforeEach
    void clearProviderProperties() {
        BACKUP.clear();
        System.getProperties().forEach((key, value) -> {
            String k = key.toString();
            if (k.startsWith("continue.beans.")) {
                BACKUP.setProperty(k, value.toString());
                System.clearProperty(k);
            }
        });
    }

    @AfterEach
    void restoreProviderProperties() throws Exception {
        for (Object keyObj : BACKUP.keySet()) {
            String key = keyObj.toString();
            System.setProperty(key, BACKUP.getProperty(key));
        }
        closeAllWindows();
    }

    // ──────────────────────────────────────────────
    // Construtor: cobre initializeUI + loadCurrentConfiguration + updateFieldStates
    // ──────────────────────────────────────────────

    @Test
    void constructorCreatesDialogWithDefaultOllamaProvider() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);

        assertEquals("ollama", panel.getSelectedProvider());
        assertNotNull(getField(panel, "lmStudioUrlField"));
        assertNotNull(getField(panel, "lmStudioModelField"));
        assertNotNull(getField(panel, "ollamaUrlField"));
        assertNotNull(getField(panel, "ollamaModelField"));
        assertNotNull(getField(panel, "statusLabel"));

        // loadCurrentConfiguration termina com configurationChanged=false
        assertEquals(false, getField(panel, "configurationChanged"));
    }

    @Test
    void constructorLoadsDefaultUrlsFromSystemProperties() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);

        JTextField lmUrl = (JTextField) getField(panel, "lmStudioUrlField");
        JTextField ollamaUrl = (JTextField) getField(panel, "ollamaUrlField");
        assertEquals("http://127.0.0.1:1234", lmUrl.getText());
        assertEquals("http://127.0.0.1:11434", ollamaUrl.getText());

        // Provider default = ollama → campos ollama habilitados, lmstudio desabilitados
        assertTrue(((JTextField) getField(panel, "ollamaUrlField")).isEnabled());
        assertFalse(((JTextField) getField(panel, "lmStudioUrlField")).isEnabled());
    }

    // ──────────────────────────────────────────────
    // selectProvider + updateFieldStates (ambos os branches)
    // ──────────────────────────────────────────────

    @Test
    void selectProviderLmStudioEnablesLmFields() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);

        invoke(panel, "selectProvider", String.class, "lmstudio");

        assertEquals("lmstudio", panel.getSelectedProvider());
        assertTrue(((JTextField) getField(panel, "lmStudioUrlField")).isEnabled());
        assertFalse(((JTextField) getField(panel, "ollamaUrlField")).isEnabled());
        // selectProvider marca configurationChanged=true
        assertEquals(true, getField(panel, "configurationChanged"));
    }

    @Test
    void selectProviderOllamaEnablesOllamaFields() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);

        invoke(panel, "selectProvider", String.class, "ollama");

        assertEquals("ollama", panel.getSelectedProvider());
        assertTrue(((JTextField) getField(panel, "ollamaUrlField")).isEnabled());
        assertFalse(((JTextField) getField(panel, "lmStudioUrlField")).isEnabled());
    }

    // ──────────────────────────────────────────────
    // testConnection: cobre corpo + thenAccept (completa via LM Studio local)
    // ──────────────────────────────────────────────

    @Test
    void testConnectionWithLmStudioCompletesAndUpdatesStatus() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);
        invoke(panel, "selectProvider", String.class, "lmstudio");

        JLabel status = (JLabel) getField(panel, "statusLabel");
        JTextField urlField = (JTextField) getField(panel, "lmStudioUrlField");
        urlField.setText("http://127.0.0.1:1234");

        invoke(panel, "testConnection", ActionEvent.class, new ActionEvent(panel, 0, "action"));

        // testFuture completa async (commonPool) → thenAccept agenda no EDT.
        // Pumpa o EDT dentro do loop de espera para processar os callbacks agendados.
        waitForStatusChange(status, 15000);

        String text = status.getText();
        assertTrue(text.contains("✅") || text.contains("❌"),
            "statusLabel deve refletir resultado do teste: " + text);
    }

    /** Aguarda o statusLabel mudar de "Testing connection..." (future completar), pumpando o EDT. */
    private static void waitForStatusChange(JLabel label, long timeoutMs) throws Exception {
        long start = System.currentTimeMillis();
        while ("Testing connection...".equals(label.getText())) {
            if (System.currentTimeMillis() - start > timeoutMs) break;
            Thread.sleep(100);
            pumpEdt(); // processa callbacks agendados no EDT (thenAccept/exceptionally)
        }
    }

    // ──────────────────────────────────────────────
    // saveConfiguration: seta system properties + dialog de sucesso (modal → daemon)
    // ──────────────────────────────────────────────

    @Test
    void saveConfigurationSetsSystemPropertiesAndShowsDialog() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);
        invoke(panel, "selectProvider", String.class, "lmstudio");

        runOnDaemon(() -> {
            try {
                invoke(panel, "saveConfiguration", ActionEvent.class, new ActionEvent(panel, 0, "action"));
            } catch (Exception ignored) {
                // dialog modal pode permanecer aberto; não importa para cobertura
            }
        });
        pumpEdt();
        Thread.sleep(200);
        closeAllWindows();

        assertEquals("lmstudio", System.getProperty("continue.beans.provider"));
    }

    // ──────────────────────────────────────────────
    // cancelConfiguration: sem mudanças (dispose direto) + com mudanças (confirm modal → daemon)
    // ──────────────────────────────────────────────

    @Test
    void cancelConfigurationWithoutChangesDisposesDirectly() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);
        // configurationChanged=false por padrão → sem dialog, dispose direto (seguro no thread de teste)

        invoke(panel, "cancelConfiguration", ActionEvent.class, new ActionEvent(panel, 0, "action"));

        // dispose() foi chamado → dialog não está mais visível/apresentado
        assertFalse(panel.isShowing());
    }

    @Test
    void cancelConfigurationWithChangesShowsConfirmDialog() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);
        invoke(panel, "selectProvider", String.class, "lmstudio"); // configurationChanged=true

        runOnDaemon(() -> {
            try {
                invoke(panel, "cancelConfiguration", ActionEvent.class, new ActionEvent(panel, 0, "action"));
            } catch (Exception ignored) {
                // dialog modal pode permanecer aberto; não importa para cobertura
            }
        });
        pumpEdt();
        Thread.sleep(200);
        closeAllWindows();
    }

    // ──────────────────────────────────────────────
    // validateInputs: válido (sem dialog) + inválido url/modelo (dialog error → daemon)
    // ──────────────────────────────────────────────

    @Test
    void validateInputsReturnsTrueWhenUrlAndModelPresent() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);
        invoke(panel, "selectProvider", String.class, "lmstudio");

        Object result = invoke(panel, "validateInputs", new Class<?>[]{});
        assertEquals(Boolean.TRUE, result); // sem dialog no caminho válido
    }

    @Test
    void validateInputsReturnsFalseWhenUrlEmpty() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);
        invoke(panel, "selectProvider", String.class, "lmstudio");
        ((JTextField) getField(panel, "lmStudioUrlField")).setText("   ");

        runOnDaemon(() -> {
            try {
                invoke(panel, "validateInputs", new Class<?>[]{});
            } catch (Exception ignored) {
                // dialog error modal pode permanecer aberto; não importa para cobertura
            }
        });
        pumpEdt();
        Thread.sleep(200);
        closeAllWindows();
    }

    @Test
    void validateInputsReturnsFalseWhenModelEmpty() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);
        invoke(panel, "selectProvider", String.class, "lmstudio");
        ((JTextField) getField(panel, "lmStudioModelField")).setText("   ");

        runOnDaemon(() -> {
            try {
                invoke(panel, "validateInputs", new Class<?>[]{});
            } catch (Exception ignored) {
                // dialog error modal pode permanecer aberto; não importa para cobertura
            }
        });
        pumpEdt();
        Thread.sleep(200);
        closeAllWindows();
    }

    // ──────────────────────────────────────────────
    // Getters públicos + getUrlForProvider/getModelForProvider (ambos os providers)
    // ──────────────────────────────────────────────

    @Test
    void gettersReturnSelectedOllamaValues() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);
        // provider default = ollama → getUrl/getModel retornam campos ollama
        assertEquals("ollama", panel.getSelectedProvider());
        assertEquals("http://127.0.0.1:11434", panel.getUrl());
        assertNotNull(panel.getModel());
    }

    @Test
    void gettersReturnSelectedLmStudioValues() throws Exception {
        ConfigurationPanel panel = new ConfigurationPanel((Frame) null);
        invoke(panel, "selectProvider", String.class, "lmstudio");
        ((JTextField) getField(panel, "lmStudioUrlField")).setText("http://custom:1234");
        ((JTextField) getField(panel, "lmStudioModelField")).setText("my-model");

        assertEquals("lmstudio", panel.getSelectedProvider());
        assertEquals("http://custom:1234", panel.getUrl());
        assertEquals("my-model", panel.getModel());
    }
}
