"""
This script exports Terasology block shapes from Blender. These are exported as as .groovy files.
Each block should be centered on the origin, and contain sub meshes with the following names:
 - Center
 - Top
 - Bottom
 - Front
 - Back
 - Left
 - Right
Each side can be given a custom property of "full" to denote that it fills that direction.
"""

import bpy
import os

def writeMeshPart(name, 
		obj, 
		fw,
		scene,
		apply_modifiers
		):
		
	def roundVec3d(v):
		return round(v[0], 6), round(v[1], 6), round(v[2], 6)
		
	def convertVec3d(v):
		return -v[0], v[2], v[1]

	def roundVec2d(v):
		return round(v[0], 6), round(v[1], 6)
	
	if not obj:
		return
	
	if apply_modifiers:
		mesh = obj.to_mesh(scene, True, 'PREVIEW')
	else:
		mesh = obj.data
		
	processedVerts = []
	processedFaces = [[] for f in range(len(mesh.faces))]
		
	for i, f in enumerate(mesh.faces):
		faceVerts = f.vertices
		for j, index in enumerate(faceVerts):
			vert = mesh.vertices[index]
			if f.use_smooth:
				normal = tuple(f.normal)
			else:
				normal = tuple(vert.normal)
			uvtemp = mesh.uv_textures.active.data[i].uv[j]
			uvs = uvtemp[0], 1.0 - uvtemp[1]
			
			processedFaces[i].append(len(processedVerts))
			processedVerts.append((vert, normal, uvs))
		
			
	fw("	%s {\n" % name)
	
	fw("		vertices = [")
	first = True
	for i, v in enumerate(processedVerts):
		if not first:
			fw(", ")
		fw("[%.6f, %.6f, %.6f]" % convertVec3d(v[0].co))
		first = False
	fw("]\n")
	
	fw("		normals = [")
	first = True
	for i, v in enumerate(processedVerts):
		if not first:
			fw(", ")
		fw("[%.6f, %.6f, %.6f]" % convertVec3d(v[1]))
		first = False
	fw("]\n")
	
	fw("		texcoords = [")
	first = True
	for i, v in enumerate(processedVerts):
		if not first:
			fw(", ")
		fw("[%.6f, %.6f]" % v[2])
		first = False
	fw("]\n")
	
	fw("		faces = [\n")
	firstFace = True
	for face in processedFaces:
		if not firstFace:
			fw(",\n")
		fw("			[")
		
		first = True
		for ind in face:
			if not first:
				fw(", ")
			fw("%d" % ind)
			first = False
		fw("]")
		firstFace = False
	fw("\n		]\n")
	if "full" in obj or "full" in obj.data:
		fw("		fullSide = true\n");
	else:
		fw("		fullSide = false\n");
	
	fw("	}\n")
	
	if apply_modifiers:
		bpy.data.meshes.remove(mesh)

def save(operator,
		 context,
		 filepath="",
		 apply_modifiers=True
		 ):

	scene = context.scene

	file = open(filepath, "w", encoding="utf8", newline="\n")
	fw = file.write
	fw("shape {\n")
	
	bpy.ops.object.mode_set(mode='OBJECT')
	
	parts = ["Center", "Top", "Bottom", "Front", "Back", "Left", "Right"]
	
	for part in parts:
		if part in bpy.data.objects:
			writeMeshPart(part, bpy.data.objects[part], fw, scene, apply_modifiers);
	
	fw("}\n")
	file.close()
	print("saving complete: %r " % filepath)

	return {'FINISHED'}
