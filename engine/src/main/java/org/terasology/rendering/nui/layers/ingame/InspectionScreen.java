/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.ingame;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.InspectionToolComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIText;

/**
 */
public class InspectionScreen extends BaseInteractionScreen {
    private UIText fullDescriptionLabel;
    private UIText entityIdField;
    private UIButton setEntityIdButton;


    @Override
    public void initialise() {
        fullDescriptionLabel = find("fullDescriptionLabel", UIText.class);
        entityIdField = find("entityIdField", UIText.class);
        setEntityIdButton = find("setEntityIdButton", UIButton.class);
        setEntityIdButton.subscribe(widget -> {
            String text = entityIdField.getText();
            EntityRef interactionTarget = getInteractionTarget();
            InspectionToolComponent inspectorComponent = interactionTarget.getComponent(InspectionToolComponent.class);
            if (text.equals("this")) {
                inspectorComponent.inspectedEntity = interactionTarget;
            } else {
                try {
                    int id1 = Integer.parseInt(text);
                    inspectorComponent.inspectedEntity = CoreRegistry.get(EntityManager.class).getEntity(id1);
                } catch (NumberFormatException e) {
                    fullDescriptionLabel.setText("Please specify a valid number");
                }
            }
            updateFields(interactionTarget);
        });

    }

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        updateFields(interactionTarget);
    }

    private void updateFields(EntityRef interactionTarget) {
        InspectionToolComponent inspectorComponent = interactionTarget.getComponent(InspectionToolComponent.class);
        EntityRef inspectedEntity = inspectorComponent.inspectedEntity;
        entityIdField.setText(Long.toString(inspectedEntity.getId()));
        if (inspectedEntity.exists()) {
            if (inspectedEntity.isActive()) {
                fullDescriptionLabel.setText(inspectedEntity.toFullDescription());
            } else {
                fullDescriptionLabel.setText("not active: " + inspectedEntity.toFullDescription());
            }
        } else {
            fullDescriptionLabel.setText("Non existing entity with id " + inspectedEntity.getId());
        }
    }

}
