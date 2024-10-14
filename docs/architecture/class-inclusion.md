# Class Inclusion in Terasology

This document explains the criteria and methods for including classes in Terasology’s engine and modules. It covers:
- Adding classes to the permission list
- Including classes in the `classesOnClasspathsToAddToEngine`
- Annotating classes with `@gestalt.module.sandbox.API`
- Class inclusion in the permission set

## 1. Adding Classes to the Permission List

### Purpose:
- **Security**: Ensure only trusted classes are allowed to execute.
- **Access Control**: Grant permissions to specific classes or packages.

### Code Example:
```java
// ModuleManager.java
permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson", ReflectPermission.class);
permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson.internal", ReflectPermission.class);
