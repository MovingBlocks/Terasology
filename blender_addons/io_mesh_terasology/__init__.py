#!BPY
"""
Name: 'TerasologyBlockShapeExport'
Blender: 262
Group: 'Export'
Tooltip: 'Export a Terasology Block Shape'
"""

bl_info = {
	"name": "Terasology Block Shape Export",
	"description": "Exporter for producing Terasology Block Shape files (in JSON format)",
	"author": "Immortius",
	"version": (1, 2),
	"blender": (2, 6, 0),
	"location": "File > Import-Export",
	"category": "Import-Export"}

import bpy
import os
import bpy_extras.io_utils
from bpy.props import StringProperty, BoolProperty

class ExportBlockShape(bpy.types.Operator, bpy_extras.io_utils.ExportHelper):
	bl_idname = "export_mesh.terasology_block_shape"
	bl_label = "Export Terasology Block Shape"

	filename_ext = ".json"
	filter_glob = StringProperty(default="*.json", options={'HIDDEN'})

	apply_modifiers = BoolProperty(
		name="Apply Modifiers",
		description="Apply Modifiers to the exported mesh",
		default=True)
		
	@classmethod
	def	poll(cls, context):
		return context.active_object != None

	def execute(self, context):
		filepath = self.filepath
		filepath = bpy.path.ensure_ext(filepath, self.filename_ext)
		from . import export_block_shape
		keywords = self.as_keywords(ignore=("filter_glob","check_existing"))
		return export_block_shape.save(self, context, **keywords)

	def draw(self, context):
		layout = self.layout

		row = layout.row()
		row.prop(self, "apply_modifiers")
		
#UI Panel
		
bpy.types.Object.teraFullSide = BoolProperty(
	name="Full Side",
	description="Is this side of the block complete",
	default = False)
	
bpy.types.Object.teraAABB = BoolProperty(
	name="Is AABB Collider",
	description="Is this object used to describe an AABB collider",
	default = False)
	
bpy.types.Scene.teraAuthor = StringProperty(
	name="Author",
	description="Is this side of the block complete",
	default = "")
	
bpy.types.Scene.teraAutoCollider = BoolProperty(
	name="Auto-generate Collider",
	description="Automatically generate an AABB collider that encapulates the block",
	default = False)
		
class UIPanel(bpy.types.Panel):
	bl_label = "Terasology Properties"
	bl_space_type = "VIEW_3D"
	bl_region_type = "UI"

	def draw(self, context):
		layout = self.layout
		scene = context.scene
		if not scene:
			return
		layout.prop(scene, 'teraAuthor')
		layout.prop(scene, 'teraAutoCollider')
		
		ob = context.object
		if not ob:
			return
		if not ob.type == 'MESH':
			return
		
		layout.prop(ob, 'teraFullSide')
		layout.prop(ob, 'teraAABB')
		
def menu_export(self, context):
	self.layout.operator(ExportBlockShape.bl_idname, text="Terasology Block Shape (.json)")

def register():
	bpy.utils.register_module(__name__)
	bpy.types.INFO_MT_file_export.append(menu_export)

def unregister():
	bpy.utils.unregister_module(__name__)
	bpy.types.INFO_MT_file_export.remove(menu_export)

if __name__ == "__main__":
	register()