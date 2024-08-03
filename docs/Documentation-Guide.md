Documentation is a crucial part of making and keeping software understandable and maintainable.
Especially in open source development where the contributor basis is subject to frequent fluctuation, it's important to provide maintainers with sufficient information about intentions, assumptions, issues and future plans when writing code.

The following guidelines will explain the various levels of detail documentation can be created with and the associated purposes and characteristics of said documentation.

## Level of Detail 0 - Code

Code can serve as documentation of the logic it represents. Proper documentation requires understandable and unambiguous variable and function names, clear order of instructions, etc.

This form of documentation should be very low-level and specific to the implemented logic. It's easy to maintain as it's bound to change anyway when the logic itself changes. Both change authors and reviewers are responsible to verify the naming and structure used in new or improved code are comprehensible.

- used for debugging or in-depth understanding of the code-base
- can seem superfluous or obvious to contributors familiar with the topic or part of the codebase
- can be repetitive which might be annoying when learning about the code-base but is crucial when hunting down bugs

### Example

```java
    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void changeMaxHealth(ChangeMaxHealthEvent event, EntityRef player, HealthComponent health) {
        int oldMaxHealth = health.maxHealth;
        health.maxHealth = (int) event.getResultValue();
        Prefab maxHealthReductionDamagePrefab = prefabManager.getPrefab("Health:maxHealthReductionDamage");
        player.send(new DoDamageEvent(Math.max(health.currentHealth - health.maxHealth, 0),
                maxHealthReductionDamagePrefab));
        player.send(new MaxHealthChangedEvent(oldMaxHealth, health.maxHealth));
        player.saveComponent(health);
    }
```

## Level of Detail 1 - JavaDoc & Code Comments
- updating should be part of PR, both author's and reviewers responsibility
- less specific, but still rather detailed on what's happening
- similar to a short summary of what a class, function, or section of code (pack of lines) does
- used for debugging and learning how and for what to utilize classes and functions
- can also be used to find action items for contributions, for instance future plans for features or refactorings to improve the code-base

### Example

```java
    /**
     * Sends out an immutable notification event when maxHealth of a character is changed.
     */
```

## Level of Detail 2 - Module Documentation

- /docs folder
- describe scope of systems, triggers for events, what components represent
- updating should be part of PR
- used for learning how and for what purpose to use a module
- should link to module API (JavaDoc) for a more in-depth and technical perspective 

### Example

> The Health Authority System handles changes of player health, but doesn't manage occurrences of damage. Changes in player health can originate from various sources, including damage, impairment effects, gameplay events, etc. None of these is relevant for the Health Authority System, it only cares about the actual change of the player health value and the associated events used to change it.

## Level of Detail 3 - Engine Wiki / Tutorial Modules
- hard to maintain as not part of the version control
- conceptual, generalized
- used to learn how a project is setup and works on a coarse-grained point of view
- provides means for conceptual or learning-by-doing learning types
- topics: patterns, tools, architecture
- should ideally make use of diagrams and examples to also enable visual learning types

### Example

For instance, this page.