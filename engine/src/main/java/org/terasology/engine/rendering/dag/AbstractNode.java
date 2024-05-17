// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.dag.dependencyConnections.BufferPair;
import org.terasology.engine.rendering.dag.dependencyConnections.BufferPairConnection;
import org.terasology.engine.rendering.dag.dependencyConnections.DependencyConnection;
import org.terasology.engine.rendering.dag.dependencyConnections.FboConnection;
import org.terasology.engine.rendering.dag.dependencyConnections.RunOrderConnection;
import org.terasology.engine.rendering.opengl.BaseFboManager;
import org.terasology.engine.rendering.opengl.FBO;
import org.terasology.engine.rendering.opengl.FboConfig;
import org.terasology.gestalt.naming.Name;
import org.terasology.engine.utilities.Assets;

import javax.annotation.Nullable;
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
public abstract class AbstractNode implements Node {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractNode.class);

    protected boolean enabled = true;
//    protected BufferPairConnection bufferPairConnection;
    private Set<StateChange> desiredStateChanges = Sets.newLinkedHashSet();
    private Map<SimpleUri, BaseFboManager> fboUsages = Maps.newHashMap();
    private Map<String, DependencyConnection> inputConnections = Maps.newHashMap();
    private Map<String, DependencyConnection> outputConnections = Maps.newHashMap();
    private final SimpleUri nodeUri;
    private final Name nodeAka;
    private Context context;

    /**
     * Constructor to be used by inheriting classes.
     * <p>
     * The nodeId provided in input will become part of the nodeUri uniquely identifying the node in the RenderGraph,
     * i.e. "engine:hazeNode".
     *
     * @param nodeId  a String representing the id of the node, namespace -excluded-: that's added automatically.
     * @param context a Context object.
     */
    protected AbstractNode(String nodeId, String nodeAka, Name providingModule, Context context) {
        String newNodeAka = nodeAka;
        ModuleManager moduleManager = context.get(ModuleManager.class);
        // Name providingModule = moduleManager.getEnvironment().getModuleProviding(this.getClass());

        this.context = context;

        this.nodeUri = new SimpleUri(providingModule.toString(), nodeId);

        if (nodeAka.endsWith("Node")) {
            newNodeAka = nodeAka.substring(0, nodeAka.length() - "Node".length());
        }
        this.nodeAka = new Name(newNodeAka);

        addOutputBufferPairConnection(1);
    }

    protected AbstractNode(String nodeId, Name providingModule, Context context) {
        this(nodeId, nodeId, providingModule, context);
    }

    public Map<String, DependencyConnection> getInputConnections() {
        return inputConnections;
    }

    public Map<String, DependencyConnection> getOutputConnections() {
        return outputConnections;
    }

    public void setInputConnections(Map<String, DependencyConnection> inputConnections) {
        this.inputConnections = inputConnections;
    }

    public void setOutputConnections(Map<String, DependencyConnection> outputConnections) {
        this.outputConnections = outputConnections;
    }

    public final void postInit(Context context) {
        tryBufferPairPass();
        setDependencies(context);
    }

    public void tryBufferPairPass() {
        BufferPairConnection bufferPairConnection = getInputBufferPairConnection(1);
        if (bufferPairConnection != null && bufferPairConnection.getData() != null) {
            addOutputBufferPairConnection(1, bufferPairConnection);
        }
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

    @Nullable
    protected FBO getInputFboData(int number) {
        return ((FboConnection) this.inputConnections.get(FboConnection.getConnectionName(number, this.nodeUri))).getData();
    }

    @Nullable
    protected FBO getOutputFboData(int number) {
        return ((FboConnection) this.outputConnections.get(FboConnection.getConnectionName(number, this.nodeUri))).getData();
    }

    public boolean addInputConnection(int id, DependencyConnection connection) {
        if (connection instanceof FboConnection) {
            return addInputFboConnection(id, (FboConnection) connection);
        }
        if (connection instanceof BufferPairConnection) {
            return addInputBufferPairConnection(id, (BufferPairConnection) connection);
        } else {
            throw new RuntimeException("addInputConnection failed on unknown connection type");
        }
    }

    public boolean addInputRunOrderConnection(RunOrderConnection from, int inputId) {
        DependencyConnection runOrderconnection = new RunOrderConnection(RunOrderConnection.getConnectionName(inputId, this.nodeUri),
                DependencyConnection.Type.INPUT, this.getUri());
        runOrderconnection.setConnectedConnection(from);
        return addInputConnection(runOrderconnection);
    }

    public boolean addOutputRunOrderConnection(int outputId) {
        DependencyConnection runOrderConnection = new RunOrderConnection(RunOrderConnection.getConnectionName(outputId, this.nodeUri),
                DependencyConnection.Type.OUTPUT, this.getUri());
        return addOutputConnection(runOrderConnection);
    }

    public boolean addInputBufferPairConnection(int id, BufferPairConnection from) {
        DependencyConnection bufferPairConenction = new BufferPairConnection(BufferPairConnection.getConnectionName(id, this.nodeUri),
                DependencyConnection.Type.INPUT, from.getData(), this.getUri());
        bufferPairConenction.setConnectedConnection(from);
        return addInputConnection(bufferPairConenction);
    }

    public boolean addInputBufferPairConnection(int id, BufferPair bufferPair) {
        BufferPairConnection bufferPairConnection = new BufferPairConnection(BufferPairConnection.getConnectionName(id, this.nodeUri),
                DependencyConnection.Type.INPUT, bufferPair, this.getUri());
        return addInputConnection(bufferPairConnection);
    }

    /**
     * TODO do something if could not insert
     * @param id
     * @param bufferPair
     * @return true if inserted, false otherwise
     */
    public boolean addOutputBufferPairConnection(int id, BufferPair bufferPair) {
        boolean success = false;
        String connectionUri = BufferPairConnection.getConnectionName(id, this.nodeUri);
        if (outputConnections.containsKey(connectionUri)) {
            BufferPairConnection localBufferPairConnection = (BufferPairConnection) outputConnections.get(connectionUri);

            // set data for all connected connections
            if (!localBufferPairConnection.getConnectedConnections().isEmpty()) {
                logger.debug("Propagating bufferPair data to all connected connections of {}: ", localBufferPairConnection);
                localBufferPairConnection.getConnectedConnections().forEach((k, v) -> {
                    logger.debug("setting data for: {} ,", v);
                    v.setData(bufferPair);
                });
                logger.debug("data propagated.\n");
            }

            if (localBufferPairConnection.getData() != null) {
                logger.warn("Adding output buffer pair to slot id {} of {} node overwrites data of existing connection: {}",
                        id, this.nodeUri, localBufferPairConnection);
            }
            localBufferPairConnection.setData(bufferPair);
            success = true;
        } else {
            DependencyConnection localBufferPairConnection =
                    new BufferPairConnection(BufferPairConnection.getConnectionName(id, this.nodeUri),
                            DependencyConnection.Type.OUTPUT, bufferPair, this.getUri());
            success = addOutputConnection(localBufferPairConnection);
        }
        return success;
    }

    /**
     * TODO do something if could not insert
     * @param id
     * @param from
     * @return true if inserted, false otherwise
     */
    public boolean addOutputBufferPairConnection(int id, BufferPairConnection from) {
        boolean success = false;
        String connectionUri = BufferPairConnection.getConnectionName(id, this.nodeUri);
        if (outputConnections.containsKey(connectionUri)) {
            BufferPairConnection localBufferPairConnection = (BufferPairConnection) outputConnections.get(connectionUri);

            // set data for all connected connections
            if (!localBufferPairConnection.getConnectedConnections().isEmpty()) {
                logger.info("Propagating data from {} to all connected connections of {}: ", from, localBufferPairConnection);
                localBufferPairConnection.getConnectedConnections().forEach((k, v) -> {
                    logger.info("setting data for: {} ,", v);
                    v.setData(from.getData());
                });
                logger.info("data propagated.\n");
            }

            if (localBufferPairConnection.getData() != null) {
                logger.warn("Adding output buffer pair connection to slot id {} of {} node overwrites data of existing connection: {}",
                        id, this.nodeUri, localBufferPairConnection);
            }
            localBufferPairConnection.setData(from.getData());

            success = true;
        } else {
            DependencyConnection localBufferPairConnection =
                    new BufferPairConnection(BufferPairConnection.getConnectionName(id, this.nodeUri),
                            DependencyConnection.Type.OUTPUT, from.getData(), this.getUri());
            success = addOutputConnection(localBufferPairConnection);
        }
        return success;
    }

    public boolean addOutputBufferPairConnection(int id) {
        DependencyConnection localBufferPairConnection =
                new BufferPairConnection(BufferPairConnection.getConnectionName(id, this.nodeUri),
                        DependencyConnection.Type.OUTPUT, this.getUri());
        return addOutputConnection(localBufferPairConnection);
    }

    /**
     *
     * @param id
     * @param from
     * @return true if inserted, false otherwise
     */
    protected boolean addInputFboConnection(int id, FboConnection from) {
        DependencyConnection fboConnection =
                new FboConnection(FboConnection.getConnectionName(id, this.nodeUri),
                        DependencyConnection.Type.INPUT, from.getData(), this.getUri());
        fboConnection.setConnectedConnection(from); // must remember where I'm connected from
        return addInputConnection(fboConnection);
    }

    /**
     * This connection must be getting a NEW FBO which is not from another node otherwise it's going to break things!
     * This is going to be private : TODO WHEN #3680 IS DONE, MUST BE PRIVATE ONLY !!!
     * @param fboData
     * @return true if inserted, false otherwise
     */
    public boolean addInputFboConnection(int id, FBO fboData) {
        DependencyConnection fboConnection =
                new FboConnection(FboConnection.getConnectionName(id, this.nodeUri),
                        DependencyConnection.Type.INPUT, fboData, this.getUri());
        return addInputConnection(fboConnection);
    }

    /**
     * TODO do something if could not insert
     * @param id
     * @param fboData
     * @return true if inserted, false otherwise
     */
    protected boolean addOutputFboConnection(int id, FBO fboData) {
        boolean success = false;
        String connectionUri = FboConnection.getConnectionName(id, this.nodeUri);
        if (outputConnections.containsKey(connectionUri)) {
            FboConnection fboConnection = (FboConnection) outputConnections.get(connectionUri);

            if (fboConnection.getData() != null) {
                logger.warn("Adding output fbo data to slot id {} of {} node overwrites data of existing connection: {}",
                        id, this.nodeUri, fboConnection);
            }
            fboConnection.setData(fboData);

            // set data for all connected connections
            if (!fboConnection.getConnectedConnections().isEmpty()) {
                logger.info("Propagating fbo data to all connected connections of {}: ", fboConnection);
                fboConnection.getConnectedConnections().forEach((k, v) -> {
                    logger.info("setting data for: {} ,", v);
                    v.setData(fboData);
                });
                logger.info("data propagated.\n");
            }

            success = true;
        } else {
            DependencyConnection fboConnection =
                    new FboConnection(FboConnection.getConnectionName(id, this.nodeUri),
                            DependencyConnection.Type.OUTPUT, fboData, this.getUri());
            success = addOutputConnection(fboConnection);
        }
        return success;
    }

    public boolean addOutputFboConnection(int id) {
        DependencyConnection fboConnection =
                new FboConnection(FboConnection.getConnectionName(id, this.nodeUri),
                        DependencyConnection.Type.OUTPUT, this.getUri());
        return addOutputConnection(fboConnection);
    }


    /**
     * Is {@code thisNode} dependent on {@code anotherNode}?
     * @param anotherNode
     * @return If this node has at least one {@code anotherNode}'s connection on input - true. Otherwise false.
     */
    public boolean isDependentOn(Node anotherNode) {
        boolean isDependent = false;
        // for all my input connections
        for (DependencyConnection connection: inputConnections.values()) {
            if (!connection.getConnectedConnections().isEmpty()) {
                Map<String, DependencyConnection> connectedConnections = connection.getConnectedConnections();
                SimpleUri anotherNodeUri = anotherNode.getUri();
                // SimpleUri connectedNodeUri;
                // for all connection's connected connections get parent node and see if it matches anotherNode
                for (DependencyConnection connectedConnection: connectedConnections.values()) {
                    if (connectedConnection.getParentNode().equals(anotherNodeUri)) {
                        isDependent = true;
                    }
                }
            }
        }
        return isDependent;
    }

    @Nullable
    public FboConnection getOutputFboConnection(int outputFboId) {
        return (FboConnection) getOutputConnection(FboConnection.getConnectionName(outputFboId, this.nodeUri));
    }

    @Nullable
    public FboConnection getInputFboConnection(int inputFboId) {
        return (FboConnection) getInputConnection(FboConnection.getConnectionName(inputFboId, this.nodeUri));
    }

    @Nullable
    public BufferPairConnection getOutputBufferPairConnection(int outputBufferPairId) {
        return (BufferPairConnection) getOutputConnection(BufferPairConnection.getConnectionName(outputBufferPairId, this.nodeUri));
    }

    @Nullable
    public BufferPairConnection getInputBufferPairConnection(int inputBufferPairId) {
        return (BufferPairConnection) getInputConnection(BufferPairConnection.getConnectionName(inputBufferPairId, this.nodeUri));
    }

    @Nullable
    public RunOrderConnection getOutputRunOrderConnection(int outputRunOrderConnectionId) {
        return (RunOrderConnection) getOutputConnection(RunOrderConnection.getConnectionName(outputRunOrderConnectionId, this.nodeUri));
    }

    @Nullable
    public RunOrderConnection getInputRunOrderConnection(int inputRunOrderConnectionId) {
        return (RunOrderConnection) getInputConnection(RunOrderConnection.getConnectionName(inputRunOrderConnectionId, this.nodeUri));
    }

    protected void removeInputConnection(String name) {
        // TODO add check - what do if connected
        inputConnections.remove(name);
    }

    protected void removeOutputConnection(String name) {
        // TODO add check - what do if connected
        outputConnections.remove(name);
    }

    /**
     * Remove said connection.
     * @param id
     * @param type
     */
    public void removeFboConnection(int id, DependencyConnection.Type type) {
        if (type.equals(DependencyConnection.Type.OUTPUT)) {
            // remove output
            outputConnections.remove(FboConnection.getConnectionName(id, getUri()));
        } else if (type.equals(DependencyConnection.Type.INPUT)) {
            // remove input connection
            inputConnections.remove(FboConnection.getConnectionName(id, getUri()));
        }
    }

    public void removeBufferPairConnection(int id, DependencyConnection.Type type) {
        if (type.equals(DependencyConnection.Type.OUTPUT)) {
            outputConnections.remove(BufferPairConnection.getConnectionName(id, getUri()));
        } else if (type.equals(DependencyConnection.Type.INPUT)) {
            inputConnections.remove(BufferPairConnection.getConnectionName(id, getUri()));
        }
    }

    public void removeRunOrderConnection(int id, DependencyConnection.Type type) {
        if (type.equals(DependencyConnection.Type.OUTPUT)) {
            outputConnections.remove(RunOrderConnection.getConnectionName(id, getUri()));
        } else if (type.equals(DependencyConnection.Type.INPUT)) {
            inputConnections.remove(RunOrderConnection.getConnectionName(id, getUri()));
        }
    }

    public int getNumberOfInputConnections() {
        return this.inputConnections.size();
    }

    public int getNumberOfOutputConnections() {
        return this.outputConnections.size();
    }

    @Nullable
    public DependencyConnection getInputConnection(String name) {
        DependencyConnection connection = inputConnections.get(name);
        if (connection == null) {
            logger.error("Getting input connection named {} returned null. No such input connection in {}", name, this);
            // throw new NullPointerException(errorMessage);
        }
        return connection;
    }

    @Nullable
    public DependencyConnection getOutputConnection(String name) {
        DependencyConnection connection = outputConnections.get(name);
        if (connection == null) {
            logger.error("Getting output connection named {} returned null. No such output connection in {}.", name, this);
            // throw new NullPointerException(errorMessage);
        }
        return connection;
    }

    public List<String> getInputConnectionsList() {
        List<String> inputConnectionNameList = new ArrayList<>();
        this.inputConnections.forEach((name, connection) -> inputConnectionNameList.add(name));
        return inputConnectionNameList;
    }

    public List<String> getOutputConnectionsList() {
        List<String> outputConnectionNameList = new ArrayList<>();
        this.inputConnections.forEach((name, connection) -> outputConnectionNameList.add(name));
        return outputConnectionNameList;
    }

    public void disconnectInputFbo(int inputId) {
        logger.info("Disconnecting {} input Fbo number {}", this.getUri(), inputId); //NOPMD
            DependencyConnection connectionToDisconnect = this.inputConnections.get(FboConnection.getConnectionName(inputId, this.nodeUri));
        if (connectionToDisconnect != null) {
            // TODO make it reconnectInputFboToOutput
            if (!connectionToDisconnect.getConnectedConnections().isEmpty()) {
                connectionToDisconnect.disconnect();
            } else {
                logger.warn("Connection was not connected, it probably originated in this node. (Support FBOs can be created inside nodes)");
            }
        } else {
            logger.error("No such connection");
        }
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
        FBO fbo;
        if (!fboUsages.containsKey(fboName)) {
            fboUsages.put(fboName, fboManager);
        } else {
            logger.warn("FBO {} is already requested.", fboName);
            fbo = fboManager.get(fboName);
            this.addInputFboConnection(inputConnections.size() + 1, fbo);
            return fbo;
        }
        fbo = fboManager.request(fboConfig);
        return fbo;
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
            logger.atError().log("Attempted to add default state change {} to the set of desired state changes. (Node: {})",
                    stateChange.getClass().getSimpleName(), this);
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

    /**
     * Deletes all desired state changes for the node and adds them all again.
     * Must call after changing dependency connections.
     */
    public void resetDesiredStateChanges() {
        desiredStateChanges.clear();
        setDependencies(context);
    }

    public void clearDesiredStateChanges() {
        desiredStateChanges.clear();
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

    @Override
    public Name getAka() {
        return nodeAka;
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
       return Assets.get(materialUrn, Material.class).orElseThrow(() ->
               new RuntimeException("Failed to resolve required asset: '" + materialUrn.toString() + "'"));
    }
}
