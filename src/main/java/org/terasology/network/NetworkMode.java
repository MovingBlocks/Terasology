package org.terasology.network;

/**
 * @author Immortius
 */
public enum NetworkMode {
    NONE(true),
    SERVER(true),
    CLIENT(false);

    private boolean authority;

    private NetworkMode(boolean authority) {
        this.authority = authority;
    }

    public boolean isAuthority() {
        return authority;
    }
}
