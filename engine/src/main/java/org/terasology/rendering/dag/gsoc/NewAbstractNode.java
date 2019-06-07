/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.dag.gsoc;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.naming.Name;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.NewNode;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.opengl.BaseFboManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FboConfig;
import org.terasology.utilities.Assets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class implements a number of default methods for the convenience of classes
 * wishing to implement the Node interface.
 * <p>
 * It provides the default functionality to identify a node, handle its status (enabled/disabled),
 * deal with StateChange objects and Frame Buffer objects.
 */
public abstract class NewAbstractNode implements NewNode {
    protected static final Logger logger = LoggerFactory.getLogger(NewAbstractNode.class);

    protected boolean enabled = true;
    private Set<StateChange> desiredStateChanges = Sets.newLinkedHashSet();
    private Map<SimpleUri, BaseFboManager> fboUsages = Maps.newHashMap();
    private Map<String, DependencyConnection> inputConnections = Maps.newHashMap();
    private Map<String, DependencyConnection> outputConnections = Maps.newHashMap();
    private final SimpleUri nodeUri;

    /**
     * Constructor to be used by inheriting classes.
     * <p>
     * The nodeId provided in input will become part of the nodeUri uniquely identifying the node in the RenderGraph,
     * i.e. "engine:hazeNode".
     *
     * @param nodeId  a String representing the id of the node, namespace -excluded-: that's added automatically.
     * @param context a Context object.
     */
    protected NewAbstractNode(String nodeId, Context context) {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        Name providingModule = moduleManager.getEnvironment().getModuleProviding(this.getClass());

        this.nodeUri = new SimpleUri(providingModule.toString() + ":" + nodeId);
        //TODO Check for empty list of either in or out
    }

    /**
     * Each node must implement this method to be called when the node is fully connected.
     * This method should call other private node methods to use dependencies.
     */
    public abstract void setDependencies(Context context);

    /**
     *
     * @param input
     * @return true if successful insertion, false otherwise
     */
    private boolean addInputConnection(DependencyConnection input) {
        return (this.inputConnections.putIfAbsent(input.getName(), input) == null);
    }

    /**
     *
     * @param output
     * @return true if successful insertion, false otherwise
     */
    private boolean addOutputConnection(DependencyConnection output) {
        return (this.outputConnections.putIfAbsent(output.getName(), output) == null);
    }

    /**
     * TODO String to SimpleUri or make ConnectionUri and change Strings for names to ConnectionUris
     */

    protected FBO getInputFboData(int number) {
        return ((FboConnection) this.inputConnections.get((new StringBuilder("FBO").append(number).toString()))).getFboData();
    }

    protected FBO getOutputFboData(int number) {
        return ((FboConnection) this.outputConnections.get((new StringBuilder("FBO").append(number).toString()))).getFboData();
    }

    /**
     *
     * @param id
     * @param from
     * @return true if inserted, false otherwise
     */
    protected boolean addInputFboConnection(int id, FboConnection from) {
        DependencyConnection fboConnection = new FboConnection(FboConnection.getFboName(id), DependencyConnection.Type.INPUT, from.getFboData(), this.getUri());
        return addInputConnection(fboConnection);
    }

    /**
     * TODO This connection is not necessarily getting its new FBO, should be setting connectedNode if not
     * @param id
     * @param fboData
     * @return true if inserted, false otherwise
     */
    protected boolean addInputFboConnection(int id, FBO fboData) {
        DependencyConnection fboConnection = new FboConnection(FboConnection.getFboName(id), DependencyConnection.Type.INPUT, fboData, this.getUri());
        return addInputConnection(fboConnection);
    }

    /**
     * TODO do something if could not insert
     * @param id
     * @param fboData
     * @return true if inserted, false otherwise
     */
    protected boolean addOutputFboConnection(int id, FBO fboData) {
        DependencyConnection fboConnection = new FboConnection(FboConnection.getFboName(id), DependencyConnection.Type.OUTPUT, fboData, this.getUri());
        return addOutputConnection(fboConnection);
    }

    /**
     *
     * @param inputFboId Input FBO id is a number of the input connection on this node.
     *                   Chosen arbitrarily, integers starting by 1 typically.
     * @param fromConnection FboConnection obtained form another node's output.
     */
    public void connectFbo(int inputFboId, FboConnection fromConnection) {
        // TODO To Change OR NOT To Change parent node for the fromConnection being stored as toConnection?
        // Is not yet connected? TODO this will have to be caught by a try-catch or redone if we were going use gui to tamper with dag
        if (fromConnection.getConnectedNode() == null) {
            if (addInputFboConnection(inputFboId, fromConnection)) {
                // Upon successful insertion - save connected node. If node is already connected, throw an exception.
                fromConnection.setConnectedNode(this.getUri());
            }
            else {
                throw new RuntimeException(this.getUri() + ".connectFbo(" + inputFboId + ", " + fromConnection.getName() + "):" +
                                            " Could not add connection for " + this + ", inputConnection with id " + inputFboId + " already exists.");
            }
        } else {
            throw new RuntimeException("connectFbo(" + inputFboId + ", " + fromConnection.getName() + "): Connection " + fromConnection + " is already connected.");
        }
    }

    public FboConnection getOutputFboConnection(int outputFboId) {
        return (FboConnection) getOutputConnection(FboConnection.getFboName(outputFboId));
    }

    public FboConnection getInputFboConnection(int inputFboId) {
        return (FboConnection) getInputConnection(FboConnection.getFboName(inputFboId));
    }

    public void removeInputConnection(String name) {
        inputConnections.remove(name);
    }

    public void removeOutputConnection(String name) {
        outputConnections.remove(name);
    }

    public int getNumberOfInputConnections() {
        return this.inputConnections.size();
    }

    public int getNumberOfOutputConnections() {
        return this.outputConnections.size();
    }

    public DependencyConnection getInputConnection(String name) {
        DependencyConnection connection = inputConnections.get(name);
        if (connection == null) {
            String errorMessage = String.format("Getting input connection named %s returned null." +
                    " No such input connection in %s", name, this.toString());
            logger.error(errorMessage);
            throw new NullPointerException(errorMessage);
        }
        return connection;
    }

    public DependencyConnection getOutputConnection(String name) {
        DependencyConnection connection = outputConnections.get(name);
        if (connection == null) {
            String errorMessage = String.format("Getting output connection named %s returned null." +
                                                " No such output connection in %s", name, this.toString());
            logger.error(errorMessage);
            throw new NullPointerException(errorMessage);
        }
        return connection;
    }

    public List<String> getInputConnections() {
        List<String> inputConnectionNameList = new ArrayList<>();
        this.inputConnections.forEach((name, connection) -> inputConnectionNameList.add(name));
        return inputConnectionNameList;
    }

    public List<String> getOutputConnections() {
        List<String> outputConnectionNameList = new ArrayList<>();
        this.inputConnections.forEach((name, connection) -> outputConnectionNameList.add(name));
        return outputConnectionNameList;
    }

    /**
     * Used by inheriting classes to declare the need for a specific Frame Buffer Object and obtain it.
     *
     * The characteristics of the required FBO are described in the fboConfig provided in input.
     * Within the context of the given fboManager an fboConfig uniquely identifies the FBO:
     * if the FBO exists already it is returned, if it doesn't, the FBO is first created and then returned.
     *
     * If the fboManager already contains an FBO with the same fboUri but different characteristics from
     * those described in the fboConfig object, an IllegalArgumentException is thrown.
     *
     * @param fboConfig an FboConfig object describing the required FBO.
     * @param fboManager a BaseFboManager object from which to obtain the FBO.
     * @return the requested FBO object, either newly created or a pre-existing one.
     * @throws IllegalArgumentException if the fboUri in fboConfig already in use by FBO with different characteristics.
     */
    protected FBO requiresFbo(FboConfig fboConfig, BaseFboManager fboManager) {
        SimpleUri fboName = fboConfig.getName();

        if (!fboUsages.containsKey(fboName)) {
            fboUsages.put(fboName, fboManager);
        } else {
            logger.warn("FBO " + fboName + " is already requested.");
            return fboManager.get(fboName);
        }

        return fboManager.request(fboConfig);
    }

    /**
     * Inheriting classes must call this method to ensure that any FBO requested and acquired by a node
     * is automatically released upon the node's disposal. This way FBOs that aren't used by any node
     * are also disposed.
     */
    @Override
    public void dispose() {
        for (Map.Entry<SimpleUri, BaseFboManager> entry : fboUsages.entrySet()) {
            SimpleUri fboName = entry.getKey();
            BaseFboManager baseFboManager = entry.getValue();
            baseFboManager.release(fboName);
        }
        fboUsages.clear();
    }

    /**
     * Adds a StateChange object to the set of state changes desired by a node.
     * <p>
     * This method is normally used within the constructor of a concrete node class, to set the OpenGL state
     * a node requires. However, it can also be used when runtime conditions change, i.e. a switch from
     * a normal rendering style to wireframe and viceversa.
     * <p>
     * Only StateChange objects that are in the set and are not redundant make it into the TaskList
     * and actually modify the OpenGL state every frame.
     *
     * @param stateChange a StateChange object used by the node to modify the OpenGL state in which it operates.
     */
    protected void addDesiredStateChange(StateChange stateChange) {
        if (stateChange.isTheDefaultInstance()) {
            logger.error("Attempted to add default state change {} to the set of desired state changes. (Node: {})",
                    stateChange.getClass().getSimpleName(), this.toString());
        }
        desiredStateChanges.add(stateChange);
    }

    /**
     * Removes a StateChange object from the set of desired state changes.
     *
     * @param stateChange a StateChange object representing a state change no longer required by the node.
     */
    protected void removeDesiredStateChange(StateChange stateChange) {
        desiredStateChanges.remove(stateChange);
    }

    public Set<StateChange> getDesiredStateChanges() {
        return desiredStateChanges;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getUri(), this.getClass().getSimpleName());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void handleCommand(String command, String... arguments) {
    }

    @Override
    public SimpleUri getUri() {
        return nodeUri;
    }

    /**
     * Utility method to conveniently retrieve materials from the Assets system,
     * hiding the relative complexity of the exception handling.
     *
     * @param materialUrn a ResourceUrn instance providing the name of the material to be obtained.
     * @return a Material instance
     * @throws RuntimeException if the material couldn't be resolved through the asset system.
     */
    public static Material getMaterial(ResourceUrn materialUrn) {
        String materialName = materialUrn.toString();
        return Assets.getMaterial(materialName).orElseThrow(() ->
                new RuntimeException("Failed to resolve required asset: '" + materialName + "'"));
    }
}
