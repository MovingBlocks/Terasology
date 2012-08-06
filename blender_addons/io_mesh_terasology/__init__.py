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

bpy.types.Scene.teraAuthor = StringProperty(
	name="Author",
	description="Is this side of the block complete",
	default = "")
	
bpy.types.Scene.teraCollisionType = bpy.props.EnumProperty( 
	name = "Collision Type",
	description="Type of collision to use for this block",
	items = [("FullCube", "Full Cube", "The entire block is solid"),
		("AutoAABB", "Auto AABB", "An AABB is calculated that encompasses the block mesh"),
		("ConvexHull", "Auto Convex Hull", "A convex hull is calculated that encompasses the block mesh"),
		("Manual", "Manual", "One or more colliders are specified to describe the collision")])
		
bpy.types.Scene.teraCollisionSymmetric = BoolProperty(
	name="Is Collision Symmetric",
	description="Whether the collision is symmetric for all rotations of the block",
	default = False)
	
bpy.types.Scene.teraBillboardNormals = BoolProperty(
	name="Use Billboard Normals",
	description="Are normals set up for billboards (pointing up)",
	default = False)
		
bpy.types.Object.teraFullSide = BoolProperty(
	name="Full Side",
	description="Is this side of the block complete",
	default = False)
	
bpy.types.Object.teraColliderType = bpy.props.EnumProperty( 
	name = "Collider Type",
	description="Type of collider this mesh provides",
	items = [("None", "None", "This mesh is not a collider"),
		("AABB", "AABB", "This mesh provides a aabb collider"),
		("Sphere", "Sphere", "This mesh provides a sphere collider")],
	default = "None")
			
class TeraScenePropUIPanel(bpy.types.Panel):
	bl_label = "Terasology Scene Properties"
	bl_space_type = "VIEW_3D"
	bl_region_type = "UI"

	def draw(self, context):
		layout = self.layout
		scene = context.scene
		if not scene:
			return
		layout.prop(scene, 'teraAuthor')
		layout.prop(scene, 'teraCollisionType')
		layout.prop(scene, 'teraCollisionSymmetric')
		layout.prop(scene, 'teraBillboardNormals')
				
class TeraObjectPropUIPanel(bpy.types.Panel):
	bl_label = "Terasology Mesh Properties"
	bl_space_type = "VIEW_3D"
	bl_region_type = "UI"

	def draw(self, context):
		layout = self.layout
		
		ob = context.object
		if not ob:
			return
		if not ob.type == 'MESH':
			return
		
		layout.prop(ob, 'teraFullSide')
		layout.prop(ob, 'teraColliderType')
		
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