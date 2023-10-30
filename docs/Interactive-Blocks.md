This is a guide on how to make a block which the user can interact with.

## Block Definition

First of all, we need to define the actual block.
A block is a JSON file ending with `.block`.
It needs to be placed in the `assets/blocks` directory.

Example: To add a HiddenChest block to the module Furnishings you would put the file under: `modules/Furnishings/assets/blocks/HiddenChest.block`

The block file is json. For the start we will put the following content into the text file:

```json
{
  "attachmentAllowed": false,
  "rotation": "horizontal",
  "tiles": {
    "sides": "CoreAssets:GreenLeaf",
    "front": "CoreAssets:ChestFront",
    "topBottom": "Core:ChestTopBottom"
  },
  "entity": {
    "prefab": "Furnishings:HiddenChest"
  },
  "inventory": {
    "stackable": false
  }
}
```

With the tiles object it is possible to specify cube like blocks with up to 6 different textures on the side of the block.
The properties of the `tiles` object should reference textures by their asset URI: `<module name>:<tile image name without extension>` (learn more in the [Asset System Tutorial](https://github.com/Terasology/TutorialAssetSystem/wiki)).
Any image under `assets/blockTiles` in module can be referenced that way.

For more details about the block properties have a look at [Block Definitions](Block-Definitions.md).

## Block Entity

With the `entity` property of the block it is possible to specify which entity is representing the block.
The sub property `prefab` takes a prefab identifier.
Like with texture identifiers, the value before the colon specifies the module in which the prefab is in.
The value after the colon specifies the extension-less filename of the prefab.
Prefabs files should be placed in the `assets/prefabs` directory and must have the extension `.prefab`, e.g., `assets/prefabs/HiddenChest.prefab`.

In a prefab you specify which components your entity has by default.
Every class that extends from Component can be used as a property in the prefab file.
All component classes end with "Component".
The property has the same name as the component class except that it lacks the suffix.
For example, an instance of the InteractionTargetComponent class can be added to the prefab by adding a property called InteractionTarget.

The following prefabs makes the block behave like a chest with 4 item slots:

```json
{
  "Inventory": {
    "privateToOwner": false,
    "itemSlots": [0, 0, 0, 0]
  },
  "PlaySoundAction": {
    "sounds": "engine:click"
  },
  "InteractionTarget": {},
  "InteractionScreen": {
    "screen": "engine:containerScreen"
  }
}
```

The Inventory component makes the entity have an inventory.
The 4 zeros mean that there should be 4 empty slots.

The PlaySoundAction component makes the entity play a sound when it gets activated with <kbd>E</kbd> (default key binding).

The InteractionTarget component makes it possible to start an interaction with the component by pressing <kbd>E</kbd> while the cursor is on the block.
An interaction for itself is invisible to the user, until you add a visualization for it.

The InteractionScreen component makes it possible to specify an UI screen, which will automatically be opened when the user starts an interaction with the block.
Closing that UI will automatically end the interaction with the block.
The InteractionScreen component has a property called `screen` that specifies which UI should be opened.

## Block Interaction UI

The look of a UI can be specified with text files that end with `.ui`.
Those text files need to be placed in the `assets/ui` folder of the module.

For a quick start we will make a copy of the `containerScreen.ui` found in the engine and place it under `assets/ui/HiddenContainer.ui` in our module.
If you placed the UI file in the Furnishings module then you need to change the screen property of the InteractionScreen component to `Furnishings:HiddenContainer`.

Then we can make adjustments to the UI.
For example, we could make the container grid be 2x2 by changing the `maxHorizontalCells` property of the container grid to 2:

```json
{
  "type": "ContainerScreen",
  "contents": {
    "type": "relativeLayout",
    "contents": [
      {
        "type": "InventoryGrid",
        "id": "inventory",
        "maxHorizontalCells": 6,
        "layoutInfo": {
          "use-content-width": true,
          "use-content-height": true,
          "position-right": {
            "target": "CENTER",
            "offset": 16
          },
          "position-vertical-center": {}
        }
      },
      {
        "type": "InventoryGrid",
        "id": "container",
        "maxHorizontalCells": 2,
        "layoutInfo": {
          "use-content-width": true,
          "use-content-height": true,
          "position-left": {
            "target": "CENTER",
            "offset": 16
          },
          "position-vertical-center": {}
        }
      }
    ]
  }
}
```

## Adding logic

The structure of the UI file is simple.
The topmost `type` property specifies the name of the Java class that should be created.
The other properties specify the default configuration of that Java class.
The type fields in the substructures describe again which Java class is providing logic to that sub structure.

The top base class should extend either CoreScreenLayer or BaseInteractionScreen.
The latter is just an enhanced CoreScreenLayer with a simpler way of accessing the interaction target / the block entity.

In the initialize method you can then access the sub components by using a find method to search the sub components by the id you specified in the `.ui` file.
