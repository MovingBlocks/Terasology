# Engine Modules and Subsystems

## Overview

This document describes the structure and components related to engine modules in Terasology. It covers how subsystems and engine modules are defined and managed.

## Subsystems

Subsystems are components of the engine that are managed separately. In `TerasologyEngine.java`, the `allSubsystems` list is used to collect all subsystems.

```java
// TerasologyEngine.java, Lines 184-185
allSubsystems.stream().map(Object::getClass).forEach(this::addToClassesOnClasspathsToAddToEngine);
