package org.terasology.logic.common;

import com.google.api.client.util.Sets;
import org.terasology.entitySystem.Component;

import java.util.Collections;
import java.util.Set;

public class RetainComponentsComponent implements Component {
    public Set<Class<? extends Component>> components = Sets.newHashSet();
}
