package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EditorUtils to achieve 100% coverage.
 */
class EditorUtilsTest {

    @Test
    void testGetSelectedCodeWithNullEditor() {
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(null);
            
            String result = EditorUtils.getSelectedCode();
            assertNull(result);
        }
    }

    @Test
    void testGetSelectedCodeWithNullSelection() {
        JTextComponent editor = mock(JTextComponent.class);
        when(editor.getSelectedText()).thenReturn(null);
        
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(editor);
            
            String result = EditorUtils.getSelectedCode();
            assertNull(result);
        }
    }

    @Test
    void testGetSelectedCodeWithEmptySelection() {
        JTextComponent editor = mock(JTextComponent.class);
        when(editor.getSelectedText()).thenReturn("   ");
        
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(editor);
            
            String result = EditorUtils.getSelectedCode();
            assertNull(result);
        }
    }

    @Test
    void testGetSelectedCodeWithValidSelection() {
        JTextComponent editor = mock(JTextComponent.class);
        when(editor.getSelectedText()).thenReturn("selected code");
        
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(editor);
            
            String result = EditorUtils.getSelectedCode();
            assertEquals("selected code", result);
        }
    }

    @Test
    void testReplaceSelectionWithNullEditor() {
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(null);
            
            // Should not throw exceptions
            assertDoesNotThrow(() -> {
                EditorUtils.replaceSelection("new text");
            });
        }
    }

    @Test
    void testReplaceSelectionWithNonEditableEditor() {
        JTextComponent editor = mock(JTextComponent.class);
        when(editor.isEditable()).thenReturn(false);
        
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(editor);
            
            // Should not throw exceptions
            assertDoesNotThrow(() -> {
                EditorUtils.replaceSelection("new text");
            });
            
            verify(editor, never()).replaceSelection(any());
        }
    }

    @Test
    void testReplaceSelectionWithEditableEditor() {
        JTextComponent editor = mock(JTextComponent.class);
        when(editor.isEditable()).thenReturn(true);
        
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(editor);
            
            EditorUtils.replaceSelection("new text");
            
            verify(editor).replaceSelection("new text");
        }
    }

    @Test
    void testInsertCodeAtCursorWithNullEditor() {
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(null);
            
            // Should not throw exceptions
            assertDoesNotThrow(() -> {
                EditorUtils.insertCodeAtCursor("code");
            });
        }
    }

    @Test
    void testInsertCodeAtCursorWithNonEditableEditor() {
        JTextComponent editor = mock(JTextComponent.class);
        when(editor.isEditable()).thenReturn(false);
        
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(editor);
            
            // Should not throw exceptions
            assertDoesNotThrow(() -> {
                EditorUtils.insertCodeAtCursor("code");
            });
            
            verify(editor, never()).getDocument();
        }
    }

    @Test
    void testInsertCodeAtCursorWithEditableEditor() throws Exception {
        JTextComponent editor = mock(JTextComponent.class);
        javax.swing.text.Document document = mock(javax.swing.text.Document.class);
        when(editor.isEditable()).thenReturn(true);
        when(editor.getCaretPosition()).thenReturn(10);
        when(editor.getDocument()).thenReturn(document);
        
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(editor);
            
            EditorUtils.insertCodeAtCursor("code");
            
            verify(document).insertString(10, "code", null);
        }
    }

    @Test
    void testInsertCodeAtCursorWithEditableEditorException() throws Exception {
        JTextComponent editor = mock(JTextComponent.class);
        javax.swing.text.Document document = mock(javax.swing.text.Document.class);
        when(editor.isEditable()).thenReturn(true);
        when(editor.getCaretPosition()).thenReturn(10);
        when(editor.getDocument()).thenReturn(document);
        doThrow(new RuntimeException("Test exception")).when(document).insertString(10, "code", null);
        
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(editor);
            
            // Should not throw exceptions (exception should be caught)
            assertDoesNotThrow(() -> {
                EditorUtils.insertCodeAtCursor("code");
            });
        }
    }

    @Test
    void testGetCurrentProjectDirectoryWithNullEditor() {
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(null);
            
            String result = EditorUtils.getCurrentProjectDirectory();
            assertNull(result);
        }
    }

    @Test
    void testGetCurrentProjectDirectoryWithNullFileObject() {
        JTextComponent editor = mock(JTextComponent.class);
        javax.swing.text.Document document = mock(javax.swing.text.Document.class);
        when(editor.getDocument()).thenReturn(document);
        
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class);
             MockedStatic<NbEditorUtilities> mockedUtilities = mockStatic(NbEditorUtilities.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(editor);
            mockedUtilities.when(() -> NbEditorUtilities.getFileObject(document)).thenReturn(null);
            
            String result = EditorUtils.getCurrentProjectDirectory();
            assertNull(result);
        }
    }

    @Test
    void testGetCurrentProjectDirectoryWithNullProject() {
        JTextComponent editor = mock(JTextComponent.class);
        javax.swing.text.Document document = mock(javax.swing.text.Document.class);
        FileObject fileObject = mock(FileObject.class);
        when(editor.getDocument()).thenReturn(document);
        
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class);
             MockedStatic<NbEditorUtilities> mockedUtilities = mockStatic(NbEditorUtilities.class);
             MockedStatic<FileOwnerQuery> mockedOwnerQuery = mockStatic(FileOwnerQuery.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(editor);
            mockedUtilities.when(() -> NbEditorUtilities.getFileObject(document)).thenReturn(fileObject);
            mockedOwnerQuery.when(() -> FileOwnerQuery.getOwner(fileObject)).thenReturn(null);
            
            String result = EditorUtils.getCurrentProjectDirectory();
            assertNull(result);
        }
    }

    @Test
    void testGetCurrentProjectDirectoryWithValidProject() {
        JTextComponent editor = mock(JTextComponent.class);
        javax.swing.text.Document document = mock(javax.swing.text.Document.class);
        FileObject fileObject = mock(FileObject.class);
        Project project = mock(Project.class);
        File projectDir = mock(File.class);
        FileObject projectDirFileObject = mock(FileObject.class);
        
        when(editor.getDocument()).thenReturn(document);
        when(project.getProjectDirectory()).thenReturn(projectDirFileObject);
        
        try (MockedStatic<EditorRegistry> mockedRegistry = mockStatic(EditorRegistry.class);
             MockedStatic<NbEditorUtilities> mockedUtilities = mockStatic(NbEditorUtilities.class);
             MockedStatic<FileOwnerQuery> mockedOwnerQuery = mockStatic(FileOwnerQuery.class);
             MockedStatic<FileUtil> mockedFileUtil = mockStatic(FileUtil.class)) {
            mockedRegistry.when(EditorRegistry::lastFocusedComponent).thenReturn(editor);
            mockedUtilities.when(() -> NbEditorUtilities.getFileObject(document)).thenReturn(fileObject);
            mockedOwnerQuery.when(() -> FileOwnerQuery.getOwner(fileObject)).thenReturn(project);
            mockedFileUtil.when(() -> FileUtil.toFile(projectDirFileObject)).thenReturn(projectDir);
            when(projectDir.getAbsolutePath()).thenReturn("/path/to/project");
            
            String result = EditorUtils.getCurrentProjectDirectory();
            assertEquals("/path/to/project", result);
        }
    }
}
