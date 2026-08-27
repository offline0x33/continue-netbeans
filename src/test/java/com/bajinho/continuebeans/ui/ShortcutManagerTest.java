package com.bajinho.continuebeans.ui;

import org.junit.jupiter.api.Test;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class ShortcutManagerTest {
    @Test void valueObjectAndContextWork(){
        ShortcutManager.Shortcut s=new ShortcutManager.Shortcut("x","X","desc",KeyStroke.getKeyStroke("ctrl T"),e->{},new String[]{"editor"},10,false,true);
        assertEquals("x",s.getId()); assertTrue(s.isRelevantForContext("editor")); assertFalse(s.isRelevantForContext("chat"));
    }
    @Test void registerConflictTriggerAndUnregisterWork(){
        ShortcutManager m=ShortcutManager.getInstance(); String id="batch-shortcut"; KeyStroke k=KeyStroke.getKeyStroke("ctrl alt T");
        AtomicInteger count=new AtomicInteger(); m.unregisterShortcut(id);
        m.registerShortcut(new ShortcutManager.Shortcut(id,"Batch","Batch",k,e->count.incrementAndGet(),new String[]{"all"},200,false,true));
        assertNotNull(m.getShortcut(id));
        assertEquals(1,m.checkConflicts(new ShortcutManager.Shortcut("other","Other","",k,e->{},new String[]{"all"},1,false,true)).size());
        m.triggerShortcut(id); assertEquals(1,count.get()); m.unregisterShortcut(id); assertNull(m.getShortcut(id));
    }
    @Test void installAndRemoveComponentMappingsWork(){
        ShortcutManager m=ShortcutManager.getInstance(); String id="component-shortcut"; KeyStroke k=KeyStroke.getKeyStroke("ctrl alt Y"); m.unregisterShortcut(id);
        m.registerShortcut(new ShortcutManager.Shortcut(id,"Component","",k,e->{},new String[]{"editor"},500,false,true));
        List<ShortcutManager.Shortcut> list=m.getShortcutsForContext("editor"); assertEquals(id,list.get(0).getId());
        JPanel p=new JPanel(); m.installShortcuts(p,"editor"); assertNotNull(p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(k)); m.removeShortcuts(p); assertNull(p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(k)); m.unregisterShortcut(id);
    }
}