*Note: This page is still under construction!*

In Terasology, [block shapes](Block-Shapes.md) are defined in **.shape** files. These are JSON files (technically, [GSON](https://github.com/google/gson) which provide rendering mesh data, physics collision data, and mouse collision data for Terasology's voxel blocks.

This page lists the Shape File format's specification standards, and their history.

# 1.1 - Unreleased, 2020

The Shape File format is currently undergoing development to increase both versatility and efficiency. It retains partial backwards compatibility; 1.0 files remain fully valid in the 1.1 standard, but a parser that expects a 1.0 file will likely be unable to parse a 1.1 file.

Significant improvements over 1.0 include the ability to define multiple directions in which a section should be rendered, as opposed to either a single direction or always being rendered; the ability to define more than one section to be rendered in any given direction; and no longer requiring data to be duplicated when different points within a section share vertex, normal, or UV data with each other.

## Changes

### First-level parameters

- Sections are no longer restrained to being named "front", "back", "top", "bottom", "left", "right", and "center" as in 1.0. They may be given any unique identifier which does not conflict with other parameters of the shape file, regardless of capitalization ("author", "collision", "displayName", and "exportDate" cannot be used as section names).

### Section Object parameters

- "faces" of a section may represent a point with an array of 3 integer indices, corresponding to "vertices", "normals", and "texcoords" of the section, as opposed to a single integer index that is used for all three. Index arrays and single indices may be used interchangeably within the same face.

- "vertices", "normals", and "texcoords" within a section are no longer required to be the same length, as faces can refer to each list individually.

- Sections have an optional "sides" parameter that defines valid directions for the section to be rendered. It may be presented as a decimal integer, a single Unicode character, a binary integer, or a valid directional name. If not included, it will use the name of the section as a fallback, as in 1.0 files.

## Full Standard

Coming soon.

# 1.0 - Feb. 17, 2014

After the addition of the "displayName" parameter, the Shape File format as parsed by Terasology would remain unchanged for 6 years. Thus it has been retroactively declared as Shape File 1.0, the standard specification in use prior to 2020.

## Full Standard

### First-level parameters

All first-level parameters are to be treated as optional. The absence of any first-level parameter should not result in an error.

| Parameter        | Type             | Description                                                          |
| ---------------- |:----------------:| :--------------------------------------------------------------------|
| **"author"**     | String           | This string represents the name of the person who created this block shape. |
| **"collision"**  | Collision Object | The collision parameter represents information about the rotational and collision data of the shape. |
| **"displayName"**| String           | This string is added to the end of a block's display name when the block is using this shape.        |
| **"exportDate"** | String           | This is a [RFC 3339](https://tools.ietf.org/html/rfc3339) timestamp in the form of "YYYY-MM-DD hh:mm:ss", intended to display when the shape was last modified within an editing program (such as Blender).          |
| **"front"**      | Section Object   | Section data representing the front (Z-) side of the block.          |
| **"back"**       | Section Object   | Section data representing the back (Z+) side of the block.           |
| **"left"**       | Section Object   | Section data representing the left (X-) side of the block.           |
| **"right"**      | Section Object   | Section data representing the right (X+) side of the block.          |
| **"top"**        | Section Object   | Section data representing the top (Y+) side of the block.            |
| **"bottom"**     | Section Object   | Section data representing the bottom (Y-) side of the block.         |
| **"center"**     | Section Object   | Section data representing part of the block that should be rendered regardless of visible directions.|

### Collision Object parameters

As with first-level parameters, all collision object parameters should be treated as optional.

| Parameter        | Type             | Description                                                          |
| ---------------- |:----------------:| :--------------------------------------------------------------------|
| **"colliders"**  |Array (Collider Object)| A list of collider objects that represent the collision data for the shape. An empty list is valid, and will result in a simple full cube being used. 
| **"convexHull"** | Boolean          | If present (regardless of true OR false) the "colliders" parameter will be ignored, and a convex hull will be calculated for the shape. If neither "colliders" nor "convexHull" is present, a simple full cube will be used.               |
| **"symmetric"**  | Boolean          | If true, sets symmetry along all three axes and overrides pitch, roll, and yaw, if present.           |
| **"pitchSymmetric"** | Boolean      | If true, sets symmetry around the X axis.                            |
| **"rollSymmetric"**  | Boolean      | If true, sets symmetry around the Z axis.                            |
| **"yawSymmetric"**   | Boolean      | If true, sets symmetry around the Y axis.                            |

### Collider Object parameters

If the "type" parameter is omitted, the collider should be ignored entirely. If "type" is "AABB", omitting the "position" or "extents" parameters should result in an error. If the "type" is "Sphere", omitting the "radius" parameter should result in an error. 

| Parameter        | Type             | Description                                                          |
| ---------------- |:----------------:| :--------------------------------------------------------------------|
| **"type"**       | String           | Either "AABB" (axis-aligned bounding box) or "Sphere".               |
| **"position"**   | Vector3F         | The center (*not* origin) of the collider.                           |
| **"extents"**    | Vector3F         | The full width (*not* distance from center) of the collider. Only valid for type "AABB".            |
| **"radius"**     | Float            | Only valid for type "Sphere".                                        |

### Section Object parameters

"vertices", "normals", and "texcoords" arrays must be identical in length, and if any of the three arrays is a different size, it will result in an error.

| Parameter        | Type             | Description                                                          |
| ---------------- |:----------------:| :--------------------------------------------------------------------|
| **"vertices"**   | Array (Vector3F) | The list of vertex coordinates.                                      |
| **"normals"**    | Array (Vector3F) | Normal data (-1 to 1) for each vertex.                               |
| **"texcoords"**  | Array (Vector2F) | The UV map coordinates (0 to 1) for each vertex,                     |
| **"faces"**      | Array (Array(Int)) | The list of face information.                                      |

### Face parameters

The "faces" array of a section contains any number of face definitions. Faces consist of arrays of integers, where each integer is the index of an included vertex in the "vertices" list. All vertices in the face should be listed in clockwise order. If an integer is outside the bounds of the vertices list (either a negative number, or greater than the length of the vertices array), it should result in an error.