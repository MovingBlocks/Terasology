
package org.terasology.documentation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.terasology.engine.module.ModuleManager;
import org.terasology.input.DefaultBinding;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.RegisterBindButton;
import org.terasology.testUtil.ModuleManagerFactory;

import com.google.common.collect.TreeBasedTable;

/**
 * Enumerates all default key bindings and writes them sorted by ID to the console
 */
public class BindingScraper
{
    /**
     * @param args (ignored)
     * @throws Exception if the module environment cannot be loaded
     */
    public static void main(String[] args) throws Exception {
        ModuleManager moduleManager = ModuleManagerFactory.create();

        TreeBasedTable<String, Input, String> keyTable = TreeBasedTable.create(
                String.CASE_INSENSITIVE_ORDER,
                (i1, i2) -> Integer.compare(i1.getId(), i2.getId()));

        for (Class<?> buttonEvent : moduleManager.getEnvironment().getTypesAnnotatedWith(RegisterBindButton.class)) {
            DefaultBinding defBinding = buttonEvent.getAnnotation(DefaultBinding.class);
            if (defBinding != null) {
                if (defBinding.type() == InputType.KEY) {
                    Input input = InputType.KEY.getInput(defBinding.id());

                    RegisterBindButton info = buttonEvent.getAnnotation(RegisterBindButton.class);
                    keyTable.put(info.category(), input, info.description());
                }
            }
        }

        for (String row : keyTable.rowKeySet()) {
            System.out.println("# " + row);
            for (Entry<Input, String> entry : keyTable.row(row).entrySet()) {
                System.out.println("`" + entry.getKey().getDisplayName() + "` : " + entry.getValue());
            }
        }
    }
}
