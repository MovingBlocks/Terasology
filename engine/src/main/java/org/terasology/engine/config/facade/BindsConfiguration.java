
package org.terasology.engine.config.facade;

import org.terasology.engine.config.BindsConfig;
import org.terasology.engine.config.Config;
import org.terasology.engine.core.SimpleUri;
import org.terasology.input.Input;
import org.terasology.engine.input.RegisterBindButton;

import java.util.Collection;
import java.util.List;

/**
 * Facade for {@link Config#getBinds()}.
 * The binds configuration holds the mapping from binding uris to Inputs.
 * The {@link SimpleUri} for a binding contains the module from the binding and the id from the binding annotation, 
 * e.g. from {@link RegisterBindButton}.
 * @see BindsConfig
 */
public interface BindsConfiguration {

    /**
     * Returns true if the input is bound to a bind uri.
     */
    boolean isBound(Input newInput);

    /**
     * Sets all bindings to the mappings contained in the given config.
     */
    void setBinds(BindsConfig other);

    /**
     * Returns a list of all inputs, bound to the given uri.
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
    * Binds one or more inputs to one uri.
    * If the inputs are bound to other uris they will be dissociated from them first
     */
    void setBinds(SimpleUri bindUri, Input ... inputs);

    /**
    * Binds one or more inputs to one uri.
    * If the inputs are bound to other uris they will be dissociated from them first
     */
    void setBinds(SimpleUri bindUri, Iterable<Input> inputs);

    /**
     * Returns a read-only view of all bound inputs.
     */
    Collection<Input> getBoundInputs();

}
