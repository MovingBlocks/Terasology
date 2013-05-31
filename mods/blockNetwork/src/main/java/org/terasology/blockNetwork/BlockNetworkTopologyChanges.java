package org.terasology.blockNetwork;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class BlockNetworkTopologyChanges {
    public final Map<SimpleNetwork, Collection<SimpleNetwork>> networkSplits;
    public final Map<SimpleNetwork, Collection<SimpleNetwork>> networkMerges;
    public final Set<SimpleNetwork> networkDeletes;
    public final Set<SimpleNetwork> networkAdds;
    public final Set<SimpleNetwork> networkUpdates;

    public BlockNetworkTopologyChanges(Set<SimpleNetwork> networkAdds, Set<SimpleNetwork> networkUpdates, Set<SimpleNetwork> networkDeletes, Map<SimpleNetwork, Collection<SimpleNetwork>> networkMerges, Map<SimpleNetwork, Collection<SimpleNetwork>> networkSplits) {
        this.networkAdds = networkAdds;
        this.networkUpdates = networkUpdates;
        this.networkDeletes = networkDeletes;
        this.networkMerges = networkMerges;
        this.networkSplits = networkSplits;
    }
}
