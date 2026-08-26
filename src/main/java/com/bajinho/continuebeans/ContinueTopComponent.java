package com.bajinho.continuebeans;

import com.bajinho.continuebeans.ui.ChatTransportSelector;
import java.awt.BorderLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Primary Continue Beans window.
 *
 * <p>The NetBeans entry point delegates to the single production ChatPanel,
 * keeping the dark developer UI as the canonical experience instead of
 * maintaining a second legacy chat implementation.</p>
 */
@TopComponent.Description(
        preferredID = "ContinueTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "com.bajinho.continuebeans.ContinueTopComponent")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ContinueTopComponentAction",
        preferredID = "ContinueTopComponent")
@Messages("CTL_ContinueTopComponentAction=Open Continue Beans")
public final class ContinueTopComponent extends TopComponent {

    public ContinueTopComponent() {
        setName("Continue Beans");
        setLayout(new BorderLayout());
        add(new ChatTransportSelector(), BorderLayout.NORTH);
        add(new ChatPanel(), BorderLayout.CENTER);
    }
}
