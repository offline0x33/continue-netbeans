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

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Official Continue Beans chat top component.
 * Uses the implementation described by the Dark Theme UI specification.
 */
@TopComponent.Description(
    preferredID = "ProfessionalContinueBeansTopComponent",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
public final class ProfessionalTopComponent extends TopComponent {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "com.bajinho.continuebeans.ui.Bundle");

    private DarkChatPanel chatPanel;

    public ProfessionalTopComponent() {
        initComponents();
        setName(BUNDLE.getString("CTL_ProfessionalContinueBeansTopComponent"));
        setToolTipText(BUNDLE.getString("HINT_ProfessionalContinueBeansTopComponent"));
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        chatPanel = new DarkChatPanel();
        add(chatPanel, BorderLayout.CENTER);
    }

    @Override
    public void componentOpened() {
        // UI is initialized in the constructor.
    }

    @Override
    public void componentClosed() {
        // Reserved for future lifecycle cleanup.
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "2.1");
    }

    void readProperties(java.util.Properties p) {
        // Properties can be read here if needed.
    }

    static ProfessionalTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(
                "ProfessionalContinueBeansTopComponent");
        if (win == null) {
            return new ProfessionalTopComponent();
        }
        return (ProfessionalTopComponent) win;
    }
}
