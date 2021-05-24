// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.editor.ui;

import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * TeraEd main class.
 */
@SuppressWarnings("serial")
public final class Viewport extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(Viewport.class);
    private JLabel startingLabel = new JLabel("Starting Terasology...");

    public Viewport() {
        setLayout(new BorderLayout());
        setSize(1280, 720);
        setMinimumSize(new Dimension(640, 480));
        setPreferredSize(new Dimension(1280, 720));
        setOpaque(false);
        startingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(startingLabel, BorderLayout.CENTER);
    }

    public void setTerasology(AWTGLCanvas canvas) {
        remove(startingLabel);
        add(canvas, BorderLayout.CENTER);
        revalidate();
    }
}
