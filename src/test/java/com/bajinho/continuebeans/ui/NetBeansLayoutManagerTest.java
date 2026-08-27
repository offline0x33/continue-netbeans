package com.bajinho.continuebeans.ui;

import org.junit.jupiter.api.Test;
import org.openide.windows.TopComponent;

import java.awt.Container;
import java.awt.Dimension;

import static org.junit.jupiter.api.Assertions.*;

class NetBeansLayoutManagerTest {
    @Test
    void constraintFactoriesClampAndExposeExpectedDefaults() {
        NetBeansLayoutManager.LayoutConstraint north = NetBeansLayoutManager.northConstraint();
        assertEquals(NetBeansLayoutManager.ConstraintType.NORTH, north.getType());
        assertEquals(0.0, north.getXRatio());
        assertEquals(1.0, north.getWidthRatio());
        assertEquals(0.25, north.getHeightRatio());
        assertEquals(100, north.getMinHeight());
        assertEquals(300, north.getMaxHeight());

        NetBeansLayoutManager.LayoutConstraint floating =
                NetBeansLayoutManager.floatingConstraint(-1, 2, 0, 2);
        assertEquals(0.0, floating.getXRatio());
        assertEquals(1.0, floating.getYRatio());
        assertEquals(0.1, floating.getWidthRatio());
        assertEquals(1.0, floating.getHeightRatio());

        NetBeansLayoutManager.LayoutConstraint docked =
                NetBeansLayoutManager.dockedConstraint("editor");
        assertEquals(NetBeansLayoutManager.ConstraintType.DOCKED, docked.getType());
        assertEquals("editor", docked.getModeId());
        assertEquals(NetBeansLayoutManager.ConstraintType.TABBED,
                NetBeansLayoutManager.tabbedConstraint("output").getType());
    }

    @Test
    void constraintsCanBeAddedUpdatedRemovedAndCleared() {
        NetBeansLayoutManager manager = new NetBeansLayoutManager();
        NetBeansLayoutManager.LayoutConstraint center = NetBeansLayoutManager.centerConstraint();

        assertNull(manager.getConstraint("chat"));
        manager.setConstraint("chat", center);
        assertSame(center, manager.getConstraint("chat"));
        assertEquals(1, manager.getAllConstraints().size());
        assertSame(center, manager.removeConstraint("chat"));
        assertNull(manager.getConstraint("chat"));
        assertNull(manager.removeConstraint("chat"));

        manager.setConstraint("a", center);
        manager.setConstraint("b", NetBeansLayoutManager.southConstraint());
        manager.clearConstraints();
        assertTrue(manager.getAllConstraints().isEmpty());
    }

    @Test
    void layoutManagerSizesTopComponentForCenterConstraint() {
        NetBeansLayoutManager manager = new NetBeansLayoutManager();
        TopComponent component = new TopComponent() { };
        component.setName("chat");
        manager.setConstraint("chat", NetBeansLayoutManager.centerConstraint());

        Container parent = new Container();
        parent.setLayout(manager);
        parent.add(component);
        manager.layoutContainer(parent);

        assertTrue(component.getWidth() >= 400);
        assertTrue(component.getHeight() >= 300);
        assertEquals(new Dimension(component.getWidth(), component.getHeight()), component.getPreferredSize());
        assertEquals(0.5f, manager.getLayoutAlignmentX(parent));
        assertEquals(0.5f, manager.getLayoutAlignmentY(parent));
        assertEquals(new Dimension(800, 600), manager.minimumLayoutSize(parent));
        assertTrue(manager.maximumLayoutSize(parent).width > 0);
    }

    @Test
    void layoutManagerImplementsLegacyAndComponentRemovalPaths() {
        NetBeansLayoutManager manager = new NetBeansLayoutManager();
        TopComponent component = new TopComponent() { };
        component.setName("window");
        manager.addLayoutComponent(component, NetBeansLayoutManager.westConstraint());
        assertNotNull(manager.getConstraint("window"));
        manager.addLayoutComponent("legacy", component);
        manager.removeLayoutComponent(component);
        assertNull(manager.getConstraint("window"));
        manager.invalidateLayout(new Container());
    }
}
