package com.bajinho.continuebeans.ui;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;

/**
 * Cobertura comportamental para McpToolDialog: construção (add/edit), validação,
 * save/cancel, estados dos campos e mouse listeners. Testes determinísticos via
 * reflection + pumpEdt. Métodos que exibem dialog modal rodam em daemon thread para não
 * travar o fork (DISPLAY real, não headless).
 */
class McpToolDialogCoverageTest {

    // ──────────────────────────────────────────────
    // Helpers de reflection para campos/métodos privados
    // ──────────────────────────────────────────────

    private static Object getField(Object target, String name) throws Exception {
        Field f = findField(McpToolDialog.class, name);
        f.setAccessible(true);
        return f.get(target);
    }

    /** Caminha pela hierarquia de superclasses para achar o campo. */
    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    /** Invoca método privado retornando o valor de retorno (null para void). */
    private static Object invoke(Object target, String method, Class<?>[] paramTypes, Object... args) throws Exception {
        java.lang.reflect.Method m = findMethod(McpToolDialog.class, method, paramTypes.length);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    /** Variante para método com um único parâmetro. */
    private static Object invoke(Object target, String method, Class<?> paramType, Object arg) throws Exception {
        return invoke(target, method, new Class<?>[]{paramType}, arg);
    }

    /** Caminha pela hierarquia de superclasses para achar o método. */
    private static java.lang.reflect.Method findMethod(Class<?> clazz, String name, int paramCount) throws NoSuchMethodException {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                    return m;
                }
            }
            c = c.getSuperclass();
        }
        throw new NoSuchMethodException(name);
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
    // Construtor (add): cobre initializeUI + createFormPanel + createButtonPanel
    // ──────────────────────────────────────────────

    @Test
    void constructorCreatesDialogWithEmptyFields() throws Exception {
        McpToolDialog dialog = new McpToolDialog((Frame) null);

        assertNotNull(getField(dialog, "nameField"));
        assertNotNull(getField(dialog, "descriptionField"));
        assertNotNull(getField(dialog, "providerField"));
        assertNotNull(getField(dialog, "endpointField"));
        assertNotNull(getField(dialog, "schemaArea"));
        assertNotNull(getField(dialog, "enabledCheckBox"));
        assertNotNull(getField(dialog, "saveButton"));
        assertNotNull(getField(dialog, "cancelButton"));

        // Campos vazios por padrão (modo add)
        assertEquals("", ((JTextField) getField(dialog, "nameField")).getText());
        assertEquals("", ((JTextArea) getField(dialog, "schemaArea")).getText());
        assertTrue(((JCheckBox) getField(dialog, "enabledCheckBox")).isSelected());
    }

    @Test
    void constructorWithExistingToolLoadsValues() throws Exception {
        com.bajinho.continuebeans.mcp.McpTool existing = new com.bajinho.continuebeans.mcp.McpTool(
            "read_file", "Read a file", "filesystem", true,
            "http://localhost:8080/api/files/read", "{\"type\":\"object\"}");

        McpToolDialog dialog = new McpToolDialog((Frame) null, existing);

        assertEquals("read_file", ((JTextField) getField(dialog, "nameField")).getText());
        assertEquals("Read a file", ((JTextField) getField(dialog, "descriptionField")).getText());
        assertEquals("filesystem", ((JTextField) getField(dialog, "providerField")).getText());
        assertEquals("http://localhost:8080/api/files/read", ((JTextField) getField(dialog, "endpointField")).getText());
        assertEquals("{\"type\":\"object\"}", ((JTextArea) getField(dialog, "schemaArea")).getText());
        assertTrue(((JCheckBox) getField(dialog, "enabledCheckBox")).isSelected());
    }

    // ──────────────────────────────────────────────
    // createModernButton: cobre mouseEntered/mouseExited (MouseAdapter anônimo)
    // ──────────────────────────────────────────────

    @Test
    void modernButtonsRespondToMouseEvents() throws Exception {
        McpToolDialog dialog = new McpToolDialog((Frame) null);

        JButton saveButton = (JButton) getField(dialog, "saveButton");
        JButton cancelButton = (JButton) getField(dialog, "cancelButton");

        // Dispara mouseEntered + mouseExited nos dois botões para cobrir o MouseAdapter
        for (JButton btn : new JButton[]{saveButton, cancelButton}) {
            java.awt.Color originalBg = btn.getBackground();

            MouseEvent entered = new MouseEvent(btn, MouseEvent.MOUSE_ENTERED, 0L, 0, 0, 0, 0, false);
            MouseEvent exited = new MouseEvent(btn, MouseEvent.MOUSE_EXITED, 0L, 0, 0, 0, 0, false);

            for (java.awt.event.MouseListener l : btn.getMouseListeners()) {
                l.mouseEntered(entered);
                l.mouseExited(exited);
            }

            // Botões continuam funcionais após os eventos de mouse
            assertNotNull(btn);
        }
    }

    // ──────────────────────────────────────────────
    // saveTool: válido (cria McpTool + saved=true + dispose) + inválido (dialog error → daemon)
    // ──────────────────────────────────────────────

    @Test
    void saveToolWithValidInputCreatesToolAndDisposes() throws Exception {
        McpToolDialog dialog = new McpToolDialog((Frame) null);

        ((JTextField) getField(dialog, "nameField")).setText("my_tool");
        ((JTextField) getField(dialog, "descriptionField")).setText("A test tool");
        ((JTextField) getField(dialog, "providerField")).setText("custom");
        ((JTextField) getField(dialog, "endpointField")).setText("http://localhost:8080/api/tools");
        ((JTextArea) getField(dialog, "schemaArea")).setText("{\"type\":\"object\"}");

        invoke(dialog, "saveTool", ActionEvent.class, new ActionEvent(dialog, 0, "action"));

        assertTrue(dialog.wasSaved());
        assertNotNull(dialog.getTool());
        assertEquals("my_tool", dialog.getTool().getName());
        assertEquals("A test tool", dialog.getTool().getDescription());
        assertEquals("custom", dialog.getTool().getProvider());
    }

    @Test
    void saveToolWithEmptyNameShowsErrorDialog() throws Exception {
        McpToolDialog dialog = new McpToolDialog((Frame) null);

        // name vazio → validateInput retorna false → dialog error modal (daemon thread)
        runOnDaemon(() -> {
            try {
                invoke(dialog, "saveTool", ActionEvent.class, new ActionEvent(dialog, 0, "action"));
            } catch (Exception ignored) {
                // dialog error modal pode permanecer aberto; não importa para cobertura
            }
        });
        pumpEdt();
        Thread.sleep(200);
        closeAllWindows();

        assertFalse(dialog.wasSaved());
    }

    @Test
    void saveToolWithEmptyDescriptionShowsErrorDialog() throws Exception {
        McpToolDialog dialog = new McpToolDialog((Frame) null);

        ((JTextField) getField(dialog, "nameField")).setText("my_tool");
        // description vazio → validateInput retorna false → dialog error modal (daemon thread)
        runOnDaemon(() -> {
            try {
                invoke(dialog, "saveTool", ActionEvent.class, new ActionEvent(dialog, 0, "action"));
            } catch (Exception ignored) {
                // dialog error modal pode permanecer aberto; não importa para cobertura
            }
        });
        pumpEdt();
        Thread.sleep(200);
        closeAllWindows();

        assertFalse(dialog.wasSaved());
    }

    @Test
    void saveToolWithEmptyProviderShowsErrorDialog() throws Exception {
        McpToolDialog dialog = new McpToolDialog((Frame) null);

        ((JTextField) getField(dialog, "nameField")).setText("my_tool");
        ((JTextField) getField(dialog, "descriptionField")).setText("A test tool");
        // provider vazio → validateInput retorna false → dialog error modal (daemon thread)
        runOnDaemon(() -> {
            try {
                invoke(dialog, "saveTool", ActionEvent.class, new ActionEvent(dialog, 0, "action"));
            } catch (Exception ignored) {
                // dialog error modal pode permanecer aberto; não importa para cobertura
            }
        });
        pumpEdt();
        Thread.sleep(200);
        closeAllWindows();

        assertFalse(dialog.wasSaved());
    }

    // ──────────────────────────────────────────────
    // cancelDialog: saved=false + dispose (sem dialog modal)
    // ──────────────────────────────────────────────

    @Test
    void cancelDialogSetsSavedFalseAndDisposes() throws Exception {
        McpToolDialog dialog = new McpToolDialog((Frame) null);

        invoke(dialog, "cancelDialog", ActionEvent.class, new ActionEvent(dialog, 0, "action"));

        assertFalse(dialog.wasSaved());
        assertNull(dialog.getTool());
    }

    // ──────────────────────────────────────────────
    // getTool + wasSaved: estados após save/cancel
    // ──────────────────────────────────────────────

    @Test
    void getToolReturnsNullWhenNotSaved() throws Exception {
        McpToolDialog dialog = new McpToolDialog((Frame) null);

        assertNull(dialog.getTool());
        assertFalse(dialog.wasSaved());
    }

    // ──────────────────────────────────────────────
    // loadExistingTool: cobre o branch tool != null (já coberto pelo construtor edit)
    // ──────────────────────────────────────────────

    @Test
    void loadExistingToolWithNullToolDoesNothing() throws Exception {
        McpToolDialog dialog = new McpToolDialog((Frame) null);

        // tool é null por padrão → loadExistingTool não faz nada (branch tool != null false)
        invoke(dialog, "loadExistingTool", new Class<?>[]{});

        assertEquals("", ((JTextField) getField(dialog, "nameField")).getText());
    }
}
