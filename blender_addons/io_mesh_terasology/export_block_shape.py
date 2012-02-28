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
import datetime
import math

def convertVec3d(v):
		return -v[0], v[2], v[1]
		
def convertVec3dAbs(v):
	return v[0], v[2], v[1]

def writeAutoCollider( 
		fw,
		scene	
		):
	parts = ["Center", "Top", "Bottom", "Front", "Back", "Left", "Right"]

	min = [100000.0,100000.0,100000.0]
	max = [-100000.0,-100000.0,-100000.0]
	
	for part in parts:
		if part in bpy.data.objects:
			mesh = bpy.data.objects[part].data
			for faceNum, f in enumerate(mesh.faces):
				faceVerts = f.vertices
				for vertNum, index in enumerate(faceVerts):
					vert = mesh.vertices[index].co
					for i in range(3):
						if vert[i] > max[i]:
							max[i] = vert[i]
						elif vert[i] < min[i]:
							min[i] = vert[i]
	
	pos = [0.0,0.0,0.0]
	dim = [0.0,0.0,0.0]
	
	for i in range(3):
		pos[i] = 0.5 * (max[i] + min[i])
		dim[i] = 0.5 * (max[i] - min[i])
	fw("	Colliders = [\n")	
	fw("		[\n")
	fw("			position : [%.6f, %.6f, %.6f],\n" % convertVec3d(pos))
	fw("			extents : [%.6f, %.6f, %.6f]\n" % convertVec3dAbs(dim))
	fw("		]\n")	
	fw("	]\n")
		
def writeCollider( 
		obj, 
		fw,
		scene	
		):
	if not obj:
		return
		
	mesh = obj.data
		
	min = [100000.0,100000.0,100000.0]
	max = [-100000.0,-100000.0,-100000.0]
		
	for faceNum, f in enumerate(mesh.faces):
		faceVerts = f.vertices
		for vertNum, index in enumerate(faceVerts):
			vert = mesh.vertices[index].co
			for i in range(3):
				if vert[i] > max[i]:
					max[i] = vert[i]
				elif vert[i] < min[i]:
					min[i] = vert[i]
	
	pos = [0.0,0.0,0.0]
	dim = [0.0,0.0,0.0]
	
	for i in range(3):
		pos[i] = 0.5 * (max[i] + min[i])
		dim[i] = 0.5 * (max[i] - min[i])
		
	fw("		[\n")
	fw("			position : [%.6f, %.6f, %.6f],\n" % convertVec3d(pos))
	fw("			extents : [%.6f, %.6f, %.6f]\n" % convertVec3dAbs(dim))
	fw("		]")	

def writeMeshPart(name, 
		obj, 
		fw,
		scene,
		apply_modifiers		
		):
		
	def roundVec3d(v):
		return round(v[0], 6), round(v[1], 6), round(v[2], 6)
		

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
	if "teraFullSide" in obj:
		if obj.teraFullSide:
			fw("		fullSide = true\n");
		else:
			fw("		fullSide = false\n");
	else:
		fw("		fullSide = false\n");
	
	fw("	}\n")
	
	if apply_modifiers:
		bpy.data.meshes.remove(mesh)

def save(operator,
		 context,
		 filepath="",
		 apply_modifiers=True,
		 for_embed=False
		 ):

	scene = context.scene

	file = open(filepath, "w", encoding="utf8", newline="\n")
	fw = file.write
	if for_embed:
		fw("package org.terasology.data.shapes\n\n")
	fw("shape {\n")
	
	fw('	author = "%s"\n' % scene.teraAuthor)
	
	now = datetime.datetime.now()
	
	fw('	exportDate = "%s"\n' % '{:%Y-%m-%d %H:%M:%S}'.format(now))
	
	bpy.ops.object.mode_set(mode='OBJECT')
	
	parts = ["Center", "Top", "Bottom", "Front", "Back", "Left", "Right"]
	
	for part in parts:
		if part in bpy.data.objects:
			writeMeshPart(part, bpy.data.objects[part], fw, scene, apply_modifiers);
	if scene.teraAutoCollider:
		writeAutoCollider(fw, scene)
	else: 
		first = True
		for object in bpy.data.objects:
			if object.teraAABB:
				if first:
					fw("	Colliders = [\n")
					first = False
				else:
					fw(",\n")
				writeCollider(object, fw, scene)
		if not first:
			fw("\n	]\n")
	
	fw("}\n")
	file.close()
	print("saving complete: %r " % filepath)

	return {'FINISHED'}
