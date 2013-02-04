/*
* Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.terasology.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.editor.TeraEd;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.shader.ShaderParametersBase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * TeraEd main class.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
@SuppressWarnings("serial")
public final class MainWindow extends JFrame implements ActionListener, WindowListener {

    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private BorderLayout borderLayout;
    private ViewPort viewPort;
    private GenericPropertyPanel propertyEditor;

    private JSplitPane verticalSplitPane;

    private JMenuBar mainMenuBar;

    private JMenu fileMenu;
    private JMenuItem fileMenuExitItem;

    private JMenu propertyMenu;
    private JMenuItem propertyMenuShaderPrePost;
    private JMenuItem propertyMenuShaderSSAO;

    public ViewPort getViewPort() {
        return viewPort;
    }

    public MainWindow() {
        this.addWindowListener(this);

        viewPort = new ViewPort();

        borderLayout = new BorderLayout();
        getContentPane().setLayout(borderLayout);

        // Build up the main window editor layout...
        propertyEditor = new GenericPropertyPanel();
        propertyEditor.setSize(256, 720);
        propertyEditor.setMinimumSize(new Dimension(256, 720));
        propertyEditor.setPreferredSize(new Dimension(256, 720));

        verticalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewPort, propertyEditor);
        verticalSplitPane.setContinuousLayout(true);
        verticalSplitPane.setResizeWeight(1.0);
        getContentPane().add(verticalSplitPane, BorderLayout.CENTER);

        setTitle("TeraEd - Terasology" + " | " + "Pre Alpha");
        setSize(new Dimension(1280, 720));

        mainMenuBar = new JMenuBar();
        add(mainMenuBar, BorderLayout.NORTH);

        fileMenu = new JMenu("File");

        fileMenuExitItem = new JMenuItem("Exit");
        fileMenuExitItem.addActionListener(this);
        fileMenu.add(fileMenuExitItem);

        propertyMenu = new JMenu("Properties");

        propertyMenuShaderPrePost = new JMenuItem("Shader: PrePost");
        propertyMenuShaderPrePost.addActionListener(this);
        propertyMenu.add(propertyMenuShaderPrePost);

        propertyMenuShaderSSAO = new JMenuItem("Shader: SSAO");
        propertyMenuShaderSSAO.addActionListener(this);
        propertyMenu.add(propertyMenuShaderSSAO);

        mainMenuBar.add(fileMenu);
        mainMenuBar.add(propertyMenu);

        mainMenuBar.setVisible(true);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       if (e.getSource() == fileMenuExitItem) {
           TeraEd.getEngine().shutdown();
       } else if (e.getSource() == propertyMenuShaderPrePost) {
           propertyEditor.setActivePropertyProvider((ShaderParametersBase) ShaderManager.getInstance().getShaderProgram("prePost").getShaderParameters());
       } else if (e.getSource() == propertyMenuShaderSSAO) {
           propertyEditor.setActivePropertyProvider((ShaderParametersBase) ShaderManager.getInstance().getShaderProgram("ssao").getShaderParameters());
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
