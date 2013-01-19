package org.terasology.grammarSystem.logic.grammar.assets;

import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.grammarSystem.logic.grammar.ProductionSystem;

/** @author skaldarnar */
public class Grammar implements Asset {

    private AssetUri uri;

    private ProductionSystem productionSystem;

    //TODO: store additional header information somehow

    /**
     * A new org.terasology.logic.grammar object is created with a new (empty) AssetUri.
     *
     * @param productionSystem the production system - not null.
     */
    public Grammar(ProductionSystem productionSystem) {
        this(new AssetUri(), productionSystem);
    }

    /**
     * A Grammar object holds the underlying production system as well as the AssetUri to identify the org.terasology.logic.grammar. The
     * production system must not be null, which would result in an undefined org.terasology.logic.grammar.
     *
     * @param uri              the AssetUri of this org.terasology.logic.grammar
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
