/*
 * Copyright 2013 Moving Blocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.editor.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.editor.TeraEd;
import org.terasology.editor.properties.ReflectionProvider;
import org.terasology.engine.StateChangeSubscriber;
import org.terasology.rendering.opengl.GLSLMaterial;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

/**
 * TeraEd main class.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
@SuppressWarnings("serial")
public final class MainWindow extends JFrame implements ActionListener, WindowListener, StateChangeSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private BorderLayout borderLayout;
    private Viewport viewport;
    private PropertyPanel propertyPanel;

    private JSplitPane verticalSplitPane;

    private JMenuBar mainMenuBar;

    private JMenu fileMenu;
    private JMenuItem fileMenuExitItem;

    private JMenu shaderPropertiesMenu;
    private ArrayList<JMenuItem> shaderPropertyMenuEntries = new ArrayList<>(64);

    private JMenu propertiesMenu;
    private JMenuItem propertiesMenuScene;

    private JScrollPane propertyPanelScrollPane;

    public Viewport getViewport() {
        return viewport;
    }

    public MainWindow() {
        this.addWindowListener(this);

        viewport = new Viewport();

        borderLayout = new BorderLayout();
        getContentPane().setLayout(borderLayout);

        // Build up the main window editor layout...
        propertyPanel = new PropertyPanel();

        propertyPanelScrollPane = new JScrollPane(
                propertyPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        propertyPanelScrollPane.setMinimumSize(new Dimension(350, 720));
        propertyPanelScrollPane.setPreferredSize(new Dimension(350, 720));

        verticalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewport, propertyPanelScrollPane);
        verticalSplitPane.setContinuousLayout(true);
        verticalSplitPane.setResizeWeight(0.5);
        getContentPane().add(verticalSplitPane, BorderLayout.CENTER);

        setTitle("TeraEd - Terasology" + " | " + "Pre Alpha");

        mainMenuBar = new JMenuBar();
        setJMenuBar(mainMenuBar);

        fileMenu = new JMenu("File");

        fileMenuExitItem = new JMenuItem("Exit");
        fileMenuExitItem.addActionListener(this);
        fileMenu.add(fileMenuExitItem);

        shaderPropertiesMenu = new JMenu("Shader Properties");

        propertiesMenu = new JMenu("Properties");

        propertiesMenuScene = new JMenuItem("Scene");
        propertiesMenuScene.addActionListener(this);
        propertiesMenu.add(propertiesMenuScene);

        mainMenuBar.add(fileMenu);
        mainMenuBar.add(shaderPropertiesMenu);
        mainMenuBar.add(propertiesMenu);

        pack();
        setVisible(true);
    }

    public void onStateChange() {
        shaderPropertyMenuEntries.clear();
        shaderPropertiesMenu.removeAll();
        for (GLSLMaterial material : AssetManager.getInstance().listLoadedAssets(AssetType.MATERIAL, GLSLMaterial.class)) {
            if (material.getShaderParameters() != null) {
                final GLSLMaterial finalMat = material;
                String programName = material.getURI().toString();
                JMenuItem menuItem = new JMenuItem(programName);
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        propertyPanel.setActivePropertyProvider(new ReflectionProvider(finalMat.getShaderParameters()));
                        propertyPanel.setTitle(finalMat.getURI().toString());
                    }
                });
                shaderPropertyMenuEntries.add(menuItem);
                shaderPropertiesMenu.add(menuItem);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == fileMenuExitItem) {
            TeraEd.getEngine().shutdown();
        } else if (e.getSource() == propertiesMenuScene) {
            propertyPanel.setActivePropertyProvider(TeraEd.getSceneProperties());
            propertyPanel.setTitle("Scene Properties");
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        TeraEd.getEngine().shutdown();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

}
