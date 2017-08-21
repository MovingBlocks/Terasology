
package org.terasology.config.facade;

import org.terasology.config.BindsConfig;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.input.Input;

import java.util.Collection;
import java.util.List;

/**
 * Facade for {@link Config#getBinds()}.
 * The binds configuration holds the mapping from bind uris.
 * The {@link SimpleUri} for a binding contains the module from the binding and the id from the binding annotation, 
 * e.g. {@link RegisterBindButton.
 */
public interface BindsConfiguration {

    /**
     * Returns true if the input is bound to a bind uri.
     */
    boolean isBound(Input newInput);

    /**
     * Sets all bindings to the mappings from the given config.
     */
    void setBinds(BindsConfig other);

    /**
     * Returns a list of all input, bound to the given bind uri.
     */
    List<Input> getBinds(SimpleUri uri);

    /**
     * Returns the internal binds config.
     */
    BindsConfig getBindsConfig();

    /**
     * Returns true if the configuration contains any bindings for the given uri.
     */
    boolean hasBinds(SimpleUri uri);

    /**
     * Binds one or more inputs to one bind uri.
     * If the inputs are used for other uris, they will be removed and used for the new bind uri.
     */
    void setBinds(SimpleUri bindUri, Input ... inputs);

    /**
     * Binds one or more inputs to one bind uri.
     * If the inputs are used for other uris, they will be removed and used for the new bind uri.
     */
    void setBinds(SimpleUri bindUri, Iterable<Input> inputs);

    /**
     * Returns a collection with all bound input values. 
     * Changes to this collection may be visible to the underlying data, therefore it should be used for read-only.
     */
    Collection<Input> getBoundInputs();

}
