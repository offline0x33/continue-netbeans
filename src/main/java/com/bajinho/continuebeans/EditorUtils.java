package com.bajinho.continuebeans;

import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.NbEditorUtilities;

public class EditorUtils {

    public static String getSelectedCode() {
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        if (editor != null) {
            String selectedText = editor.getSelectedText();
            if (selectedText != null && !selectedText.trim().isEmpty()) {
                return selectedText;
            }
        }
        return null;
    }

    public static void replaceSelection(String newText) {
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        if (editor != null && editor.isEditable()) {
            editor.replaceSelection(newText);
        }
    }

    public static void insertCodeAtCursor(String code) {
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        if (editor != null && editor.isEditable()) {
            int pos = editor.getCaretPosition();
            try {
                editor.getDocument().insertString(pos, code, null);
            } catch (Exception ex) {
                // Log or handle
            }
        }
    }

    public static String getCurrentProjectDirectory() {
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        if (editor != null) {
            FileObject fo = NbEditorUtilities.getFileObject(editor.getDocument());
            if (fo != null) {
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {
                    return FileUtil.toFile(p.getProjectDirectory()).getAbsolutePath();
                }
            }
        }
        return null;
    }
}
