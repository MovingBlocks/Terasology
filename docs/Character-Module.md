- replace monkey head with animated cube
  - create animated model in Blender and export it
  - Create particle emitter so that there are particles on movement
  - Remove monkey head
- create character module that if active shows a different character
  - e.g. monkey head
  - create hook for character module(s) to place character
- Add model that plays walk animation and idle animations at proper times.
  - Add model with walk and idle animation (e.g. skeleton from gooey's quest or another model)
  - create hooks for walk and idle animations - to be handled by the character module
- Split up body
  - Create separate animated models for head, chest, legs and arms
  - Integrate body parts with existing animations for walk and idle
- Head customization 
  - Add further body parts (eye, beard, hair) attached to the head.
  - Introduce component that has fields like hairColor, hairModel, beardColor, beardModel, mouthModel, mouthColor, innerEyeModel, innerEyeColor, outerEyeColor, outerEyeModel
- Add generic way to express animation wishes to generic character module (as module, in engine, in character module)



```
{
    "location": {},
    "particleDataSprite": {
        "texture": "white"
    },
    "energyRangeGenerator": {
        "minEnergy": 0.5,
        "maxEnergy": 1.5
    },
    "velocityRangeGenerator": {
        "minVelocity": [0.0, 0.0, 0.0],
        "maxVelocity": [0.0, 0.0, 0.0]
    },
    "velocityAffector": {},
    "particleEmitter": {
        "lifeTime": 72000,
        "particleSpawnsLeft": 10000,
        "maxParticles": 10000,
        "particleCollision": false,
        "destroyEntityWhenDead": true,
        "spawnRateMin":20,
        "spawnRateMax":20

    },
    "PositionRangeGenerator": {
        "minPosition": [-0.3, -0.3, -0.3],
        "maxPosition": [0.3, 0.3, 0.3]
    },
    "ScaleRangeGenerator": {
        "minScale": [0.03, 0.03, 0.03],
        "maxScale": [0.03, 0.03, 0.03]
    },
    "ColorRangeGenerator": {
        "minColorComponents": [1.0, 0.0, 0.0, 0.5],
        "maxColorComponents": [1.0, 1.0, 0.0, 0.5]

    },
    "Network": {}
}
```

```
/**
 * Attaches a particle emitting component to the entity as owned child that is not persistent
 */
public class AttachParticleEmitterComponent implements Component {
    /**
     * Changes of this field at runtime are not supported yet
     */
    public Prefab particleSystem;
}

```



```

/**
 * Logic for {@link AttachParticleEmitterComponent}
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class AttachParticleEmitterSystem extends BaseComponentSystem {

    @In
    EntityManager entityManager;

    @ReceiveEvent
    public void onAttachmentNeeded(OnActivatedComponent event, EntityRef owningEntity, LocationComponent ownerLocationComponent,
                           AttachParticleEmitterComponent attachComponent) {
        EntityBuilder entityBuilder = entityManager.newBuilder(attachComponent.particleSystem);
        entityBuilder.setPersistent(false);
        LocationComponent locationComponent = new LocationComponent();
        entityBuilder.addOrSaveComponent(locationComponent);
        entityBuilder.setOwner(owningEntity);
        Vector3f offset = new Vector3f();
        EntityRef particleSystemEntity = entityBuilder.build();
        Location.attachChild(owningEntity, particleSystemEntity, offset, new Quat4f(1, 0, 0, 0));
    }
}
```