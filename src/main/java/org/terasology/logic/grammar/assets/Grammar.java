package org.terasology.logic.grammar.assets;

import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.logic.grammar.ProductionSystem;

/**
 * @author skaldarnar
 */
public class Grammar implements Asset {

    private AssetUri uri;

    private ProductionSystem productionSystem;

    //TODO: store additional header information somehow

    /**
     * A new grammar object is created with a new (empty) AssetUri.
     *
     * @param productionSystem the production system - not null.
     */
    public Grammar(ProductionSystem productionSystem) {
        this(new AssetUri(), productionSystem);
    }

    /**
     * A Grammar object holds the underlying production system as well as the AssetUri to identify the grammar.
     * The production system must not be null, which would result in an undefined grammar.
     *
     * @param uri              the AssetUri of this grammar
     * @param productionSystem the production system - not null.
     */
    public Grammar(AssetUri uri, ProductionSystem productionSystem) {
        if (productionSystem == null) {
            throw new IllegalArgumentException("No null params allowed.");
        }
        this.uri = uri;
        this.productionSystem = productionSystem;
    }

    public ProductionSystem getProductionSystem() {
        return productionSystem;
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    @Override
    public void dispose() {
        // TODO: write some kind of destructor here
    }
}
