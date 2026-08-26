/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.bajinho.continuebeans.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Toolbar action for Continue Beans AI Assistant.
 * Provides quick access to the AI assistant through the NetBeans toolbar.
 *
 * <p>This class follows Apache NetBeans toolbar integration patterns,
 * implementing proper icon handling, localization, and action registration.</p>
 *
 * @see MenuIntegration
 * @see ProfessionalTopComponent
 */
@ActionID(
    category = "Build",
    id = "com.bajinho.continuebeans.ui.ToolbarAction"
)
@ActionRegistration(
    displayName = "#CTL_Toolbar_Access"
)
@ActionReference(
    path = "Toolbars/Build",
    position = 200
)
@NbBundle.Messages({
    "CTL_Toolbar_Access=Continue Beans AI",
    "CTL_Toolbar_Description=Open AI-powered development assistant"
})
public final class ToolbarAction implements ActionListener {

    public static JButton create() {
        ImageIcon icon;
        try {
            icon = new ImageIcon(
                Utilities.loadImage("com/bajinho/continuebeans/ui/continue_beans_logo.png", true));
        } catch (Exception e) {
            icon = new ImageIcon();
        }

        JButton button = new JButton(icon);
        button.setToolTipText(
            NbBundle.getMessage(ToolbarAction.class, "CTL_Toolbar_Description"));
        button.setFocusable(false);
        button.addActionListener(new ToolbarAction());
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProfessionalTopComponent window = ProfessionalTopComponent.findInstance();
        window.open();
        window.requestActive();
    }
}