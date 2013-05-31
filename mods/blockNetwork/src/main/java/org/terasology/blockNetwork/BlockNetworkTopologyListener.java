package org.terasology.blockNetwork;

import java.util.Collection;

public interface BlockNetworkTopologyListener {
    public void networkAdded(Network newNetwork);
    public void networkUpdated(Network network);
    public void networkRemoved(Network network);
    public void networkSplit(Network sourceNetwork, Collection<? extends Network> resultNetwork);
    public void networksMerged(Network mainNetwork, Network mergedNetwork);
}
