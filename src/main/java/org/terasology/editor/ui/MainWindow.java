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
package org.terasology.editor.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.editor.TeraEd;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.shader.ShaderParametersBase;
import org.terasology.rendering.shader.ShaderProgram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
    private PropertyPanel propertyPanel;

    private JSplitPane verticalSplitPane;

    private JMenuBar mainMenuBar;

    private JMenu fileMenu;
    private JMenuItem fileMenuExitItem;

    private JMenu shaderPropertiesMenu;
    private ArrayList<JMenuItem> shaderPropertyMenuEntries = new ArrayList<JMenuItem>(64);

    private JMenu propertiesMenu;
    private JMenuItem propertiesMenuScene;

    //private JToolBar toolbar;
    //private JToggleButton activateGameModeButton;

    private JScrollPane propertyPanelScrollPane;

    public ViewPort getViewPort() {
        return viewPort;
    }

    public MainWindow() {
        this.addWindowListener(this);

        viewPort = new ViewPort();

        borderLayout = new BorderLayout();
        getContentPane().setLayout(borderLayout);

        // Build up the main window editor layout...
        propertyPanel = new PropertyPanel();

        propertyPanelScrollPane = new JScrollPane(
                propertyPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        propertyPanelScrollPane.setSize(350, 720);
        propertyPanelScrollPane.setMinimumSize(new Dimension(350, 720));
        propertyPanelScrollPane.setPreferredSize(new Dimension(350, 720));

        verticalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewPort, propertyPanelScrollPane);
        verticalSplitPane.setContinuousLayout(true);
        verticalSplitPane.setResizeWeight(1.0);
        getContentPane().add(verticalSplitPane, BorderLayout.CENTER);

        setTitle("TeraEd - Terasology" + " | " + "Pre Alpha");
        setSize(new Dimension(1280+350, 720));

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

//        toolbar = new JToolBar();
//        add(toolbar, BorderLayout.NORTH);
//
//        activateGameModeButton = new JToggleButton(UIManager.getIcon("OptionPane.warningIcon"));
//        activateGameModeButton.addActionListener(this);
//        toolbar.add(activateGameModeButton);

        setVisible(true);
    }

    public void initPostEngine() {
        HashMap<String, ShaderProgram> shaderPrograms = ShaderManager.getInstance().getShaderPrograms();
        Iterator<String> shaderIterator = shaderPrograms.keySet().iterator();
        while (shaderIterator.hasNext()) {
            String programName = shaderIterator.next();

            JMenuItem menuItem = new JMenuItem(programName);
            menuItem.addActionListener(this);

            shaderPropertyMenuEntries.add(menuItem);
            shaderPropertiesMenu.add(menuItem);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       if (e.getSource() == fileMenuExitItem) {
           TeraEd.getEngine().shutdown();
       } else if (e.getSource() == propertiesMenuScene) {
           propertyPanel.setActivePropertyProvider(TeraEd.getSceneProperties());
           propertyPanel.setTitle("Scene Properties");
       } else {
           for (int i=0; i<shaderPropertyMenuEntries.size(); ++i) {
                if (e.getSource() == shaderPropertyMenuEntries.get(i)) {
                    String shaderProgramName = shaderPropertyMenuEntries.get(i).getText();
                    propertyPanel.setActivePropertyProvider((ShaderParametersBase) ShaderManager.getInstance().getShaderProgram(shaderProgramName).getShaderParameters());
                    propertyPanel.setTitle(shaderProgramName);
                }
           }
       }// else if (e.getSource() == activateGameModeButton) {
//           TerasologyEngine.setEditorInFocus(!TerasologyEngine.isEditorInFocus());
//           Mouse.setGrabbed(!TerasologyEngine.isEditorInFocus());
//       }
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
