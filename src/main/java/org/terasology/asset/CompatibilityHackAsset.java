package org.terasology.asset;

/**
 * @author Immortius
 */
public abstract class CompatibilityHackAsset implements AssetData, Asset<AssetData> {

    private AssetUri uri;

    /**
     * @return This asset's identifying URI.
     */
    public final AssetUri getURI() {
        return uri;
    }

    public CompatibilityHackAsset(AssetUri uri) {
        this.uri = uri;
    }

    public CompatibilityHackAsset() {
    }

    public void setURI(AssetUri uri) {
        this.uri = uri;
    }

    @Override
    public void reload(AssetData data) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isDisposed() {
        return false;
    }
}
