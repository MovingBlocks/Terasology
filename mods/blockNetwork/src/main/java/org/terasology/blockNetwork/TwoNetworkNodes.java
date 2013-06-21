package org.terasology.blockNetwork;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class TwoNetworkNodes {
    public final NetworkNode node1;
    public final NetworkNode node2;

    public TwoNetworkNodes(NetworkNode node1, NetworkNode node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TwoNetworkNodes that = (TwoNetworkNodes) o;
        
        return ((that.node1.equals(node1) && that.node2.equals(node2))
                || (that.node1.equals(node2) && that.node2.equals(node1)));
    }

    @Override
    public int hashCode() {
        return node1.hashCode() + node2.hashCode();
    }
}
