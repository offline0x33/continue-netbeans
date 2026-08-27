package com.bajinho.continuebeans.ui;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StatusIntegrationTest {
    @Test void initialStateAndStatusLineWork(){
        StatusIntegration s=new StatusIntegration();
        assertEquals("Ready",s.getCurrentStatus()); assertNotNull(s.getStatusLineElement()); assertTrue(s.getStatusItems().isEmpty());
    }
    @Test void statusItemsSortAndCap(){
        StatusIntegration s=new StatusIntegration();
        for(int i=0;i<12;i++) s.addStatusItem(new StatusIntegration.StatusItem("id"+i,"item"+i,Color.BLACK,null,i,false));
        List<StatusIntegration.StatusItem> items=s.getStatusItems(); assertEquals(10,items.size()); assertEquals(11,items.get(0).getPriority()); assertEquals(2,items.get(9).getPriority());
        s.clearStatusItems(); assertTrue(s.getStatusItems().isEmpty());
    }
    @Test void progressTaskClampsAndCompletes(){
        StatusIntegration.ProgressTask t=new StatusIntegration.ProgressTask("t","Build",100,false,"running");
        t.setCurrent(40); assertEquals(40,t.getCurrent()); assertEquals(.4,t.getProgress(),.0001); t.setCurrent(200); assertEquals(100,t.getCurrent()); assertTrue(t.isCompleted()); t.setCompleted(false); assertFalse(t.isCompleted()); t.setCurrent(-10); assertEquals(0,t.getCurrent());
    }
    @Test void progressMapLifecycleWorks() throws Exception {
        StatusIntegration s=new StatusIntegration(); StatusIntegration.ProgressTask t=s.startProgress("t","Task",10,false,"running");
        assertSame(t,s.getProgressTasks().get("t")); s.updateProgress("t",5); assertEquals(5,s.getProgressTasks().get("t").getCurrent()); s.clearCompletedTasks(); assertTrue(s.getProgressTasks().containsKey("t")); s.completeProgress("t"); s.clearCompletedTasks(); assertFalse(s.getProgressTasks().containsKey("t"));
    }
}