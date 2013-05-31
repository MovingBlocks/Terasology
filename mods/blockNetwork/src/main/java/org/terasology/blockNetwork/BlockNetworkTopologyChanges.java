package org.terasology.blockNetwork;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class BlockNetworkTopologyChanges {
    public final Map<Network, Collection<Network>> networkSplits;
    public final Map<Network, Collection<Network>> networkMerges;
    public final Set<Network> networkDeletes;
    public final Set<Network> networkAdds;
    public final Set<Network> networkUpdates;

    public BlockNetworkTopologyChanges(Set<Network> networkAdds, Set<Network> networkUpdates, Set<Network> networkDeletes, Map<Network, Collection<Network>> networkMerges, Map<Network, Collection<Network>> networkSplits) {
        this.networkAdds = networkAdds;
        this.networkUpdates = networkUpdates;
        this.networkDeletes = networkDeletes;
        this.networkMerges = networkMerges;
        this.networkSplits = networkSplits;
    }
}
