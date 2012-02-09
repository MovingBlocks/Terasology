#!BPY
"""
Name: 'TerasologyBlockShapeExport'
Blender: 260
Group: 'Export'
Tooltip: 'Export a Terasology Block Shape'
"""

bl_info = {
	"name": "Terasology Block Shape Export",
	"description": "Exporter for producing Terasology Block Shape files",
	"author": "Immortius",
	"version": (1, 0),
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

	filename_ext = ".groovy"
	filter_glob = StringProperty(default="*.groovy", options={'HIDDEN'})

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
		
def menu_export(self, context):
	self.layout.operator(ExportBlockShape.bl_idname, text="Terasology Block Shape (.groovy)")

def register():
	bpy.utils.register_module(__name__)
	bpy.types.INFO_MT_file_export.append(menu_export)

def unregister():
	bpy.utils.unregister_module(__name__)
	bpy.types.INFO_MT_file_export.remove(menu_export)

if __name__ == "__main__":
	register()