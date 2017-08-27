
package org.terasology.config.facade;

import org.terasology.config.BindsConfig;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.input.Input;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableCollection;

public class BindsConfigurationImpl implements BindsConfiguration {

    private Config config;

    public BindsConfigurationImpl(Config config) {
        this.config = config;
    }

    @Override
    public boolean isBound(Input newInput) {
        return config.getBinds().isBound(newInput);
    }

    @Override
    public void setBinds(BindsConfig other) {
        config.getBinds().setBinds(other);
    }

    @Override
    public List<Input> getBinds(SimpleUri uri) {
        return config.getBinds().getBinds(uri);
    }

    @Override
    public boolean hasBinds(SimpleUri uri) {
        return config.getBinds().hasBinds(uri);
    }

    @Override
    public void setBinds(SimpleUri bindUri, Input ... inputs) {
        config.getBinds().setBinds(bindUri, inputs);
    }

    @Override
    public void setBinds(SimpleUri bindUri, Iterable<Input> inputs) {
        config.getBinds().setBinds(bindUri, inputs);
    }

    @Override
    public BindsConfig getBindsConfig() {
        return config.getBinds();
    }

    @Override
    public Collection<Input> getBoundInputs() {
        return unmodifiableCollection(config.getBinds().getBoundInputs());
    }

}
