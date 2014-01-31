/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.asset;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetType;
import org.terasology.asset.Assets;
import org.terasology.engine.module.Module;
import org.terasology.logic.behavior.tree.LookupNode;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.registry.CoreRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Loader for behavior assets. Can also save assets into json format.
 * <p/>
 * If there are both, Nodes and Renderables tree, both are loaded/saved. To ensure, the nodes get associated to
 * the correct renderable, additional ids are introduced (only in the json file).
 * <p/>
 * TODO this may be rewritten, especially considering the save functionality
 *
 * @author synopia
 */
public class BehaviorTreeLoader implements AssetLoader<BehaviorTreeData> {
    private static final Logger logger = LoggerFactory.getLogger(BehaviorTreeLoader.class);
    private BehaviorTreeGson treeGson = new BehaviorTreeGson();

    public void save(OutputStream stream, BehaviorTreeData data) throws IOException {
        try (JsonWriter write = new JsonWriter(new OutputStreamWriter(stream))) {
            write.setIndent("  ");
            write.beginObject().name("model");
            treeGson.saveTree(write, data.getRoot());
            write.endObject();
        }
    }

    @Override
    public BehaviorTreeData load(Module module, InputStream stream, List<URL> urls) throws IOException {
        BehaviorTreeData data = new BehaviorTreeData();
        try (JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
            reader.setLenient(true);
            reader.beginObject();
            nextName(reader, "model");
            data.setRoot(treeGson.loadTree(reader));
            reader.endObject();
        } catch (JsonSyntaxException e) {
            logger.error("Cannot load tree! " + e.getMessage());
            return null;
        }
        return data;
    }

    private String nextName(JsonReader in, String expectedName) throws IOException {
        String name = in.nextName();
        if (!expectedName.equals(name)) {
            throw new RuntimeException(expectedName + " expected!");
        }
        return name;
    }

    private final class BehaviorTreeGson {
        private int currentId;
        private Map<Node, Integer> nodeIds = Maps.newHashMap();
        private Map<Integer, Node> idNodes = Maps.newHashMap();

        private Gson gsonNode;

        private BehaviorTreeGson() {
            gsonNode = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapterFactory(new NodeTypeAdapterFactory())
                    .registerTypeAdapter(BehaviorTree.class, new BehaviorTreeTypeAdapterFactory())
                    .create();
        }

        public Node loadTree(JsonReader reader) {
            resetIds();
            return gsonNode.fromJson(reader, Node.class);
        }

        public void saveTree(JsonWriter writer, Node root) {
            resetIds();
            gsonNode.toJson(root, Node.class, writer);
        }

        public Node getNode(int id) {
            return idNodes.get(id);
        }

        public int getId(Node node) {
            return nodeIds.get(node);
        }

        private void resetIds() {
            idNodes.clear();
            nodeIds.clear();
            currentId = 0;
        }

        private class NodeTypeAdapterFactory implements TypeAdapterFactory {
            @Override
            public <T> TypeAdapter<T> create(final Gson gson, TypeToken<T> type) {
                final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        if (value instanceof Node) {
                            out.beginObject();
                            idNodes.put(currentId, (Node) value);
                            nodeIds.put((Node) value, currentId);

                            TypeAdapter<T> delegateAdapter = getDelegateAdapter(value.getClass());

                            out.name("nodeType").value(CoreRegistry.get(NodesClassLibrary.class).getMetadata(((Node) value).getClass()).getUri().toString())
                                    .name("nodeId").value(currentId);
                            currentId++;
                            out.name("node");
                            delegateAdapter.write(out, value);
                            out.endObject();
                        } else {
                            delegate.write(out, value);
                        }
                    }

                    @Override
                    public T read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.BEGIN_OBJECT) {
                            in.beginObject();
                            nextName(in, "nodeType");
                            String nodeType = in.nextString();
                            ClassMetadata<? extends Node, ?> classMetadata = CoreRegistry.get(NodesClassLibrary.class).resolve(nodeType);
                            nextName(in, "nodeId");
                            int id = in.nextInt();
                            nextName(in, "node");
                            if (classMetadata != null) {
                                TypeAdapter<T> delegateAdapter = getDelegateAdapter(classMetadata.getType());
                                Node result;
                                if (classMetadata.getType() == LookupNode.class) {
                                    result = classMetadata.newInstance();
                                    in.beginObject();
                                    nextName(in, "tree");
                                    String uri = in.nextString();
                                    ((LookupNode) result).tree = (BehaviorTree) Assets.resolve(AssetType.BEHAVIOR, uri);
                                    in.endObject();
                                } else {
                                    result = (Node) delegateAdapter.read(in);
                                }
                                idNodes.put(id, result);
                                nodeIds.put(result, id);
                                in.endObject();
                                return (T) result;
                            } else {
                                throw new NotSerializableException(nodeType);
                            }
                        } else {
                            return delegate.read(in);
                        }
                    }

                    private TypeAdapter<T> getDelegateAdapter(Class cls) {
                        return (TypeAdapter<T>) gson.getDelegateAdapter(NodeTypeAdapterFactory.this, TypeToken.get(cls));
                    }
                };
            }

        }

        private class BehaviorTreeTypeAdapterFactory extends TypeAdapter<BehaviorTree> {
            @Override
            public void write(JsonWriter out, BehaviorTree value) throws IOException {
                if (value == null) {
                    out.value("");
                } else {
                    out.value(value.getURI().toNormalisedSimpleString());
                }
            }

            @Override
            public BehaviorTree read(JsonReader in) throws IOException {
                String uri = in.nextString();
                if (uri != null && uri.length() > 0) {
                    return (BehaviorTree) Assets.resolve(AssetType.BEHAVIOR, in.nextString());
                } else {
                    return null;
                }
            }
        }
    }
}
