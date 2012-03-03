package org.terasology.model.inventory;

import static org.lwjgl.opengl.GL11.glTranslatef;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;
import org.terasology.logic.manager.TextureManager;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockGroup;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

/**
 * Icon for rendering items in inventory.
 */
@SuppressWarnings("rawtypes")
public class Icon {
	private static Map<Class, Icon> icons;
	
	private UIGraphicsElement _element;
	private BlockGroup _blockGroup;
	private int _x;
	private int _y;
	
	/**
	 * Creates Icon for BlockGroup class.
	 * 
	 * @param blockGroup
	 */
	public Icon(BlockGroup blockGroup) {
		_element = null;
		_blockGroup = blockGroup;
		setAtlasPosition(0, 0);
	}
	
	/**
	 * Creates an Icon for a non-BlockGroup class
	 */
	public Icon() {
		_element = new UIGraphicsElement("items");
		_blockGroup = null;
		
        _element.setSize(new Vector2f(32, 32));
        _element.getTextureSize().set(new Vector2f(0.0624f, 0.0624f));
        _element.setVisible(true);
        _element.setPosition(new Vector2f(-10f, -16f));

        setAtlasPosition(0, 0);
	}
	
	/**
	 * Returns the icon for <code>item</code>.
	 * 
	 * @param item the Item to check
	 * @return the Icon for item
	 */
	public static Icon get(Item item) {
		if (icons == null) {
			loadIcons();
		}
		
		if (item instanceof ItemBlock) {
			return new Icon(((ItemBlock) item).getBlockGroup());
		}
				
		return icons.get(item.getClass());
	}
	
	private static void loadIcons() {
		icons = new HashMap<Class, Icon>();

        //TODO: Hmm, does this mean we have hard coded our tool displays? Should try to move this to ToolManager in that case?
		Icon axeIcon = new Icon();
		Icon pickAxeIcon = new Icon();
		Icon dynamiteIcon = new Icon();
		Icon blueprintIcon = new Icon();
        Icon debugIcon = new Icon();
		
		axeIcon.setAtlasPosition(1, 7);
		pickAxeIcon.setAtlasPosition(1, 6);
		dynamiteIcon.setAtlasPosition(5, 0);
		blueprintIcon.setAtlasPosition(10, 3);
        debugIcon.setAtlasPosition(10, 3);
		
		icons.put(ItemAxe.class, axeIcon);
		icons.put(ItemPickAxe.class, pickAxeIcon);
		icons.put(ItemDynamite.class, dynamiteIcon);
		icons.put(ItemBlueprint.class, blueprintIcon);
        icons.put(ItemDebug.class, debugIcon);
        icons.put(ItemRailgun.class, dynamiteIcon);
	}
	
	/**
	 * Draw the icon.
	 */
	public void render() {
		if (_blockGroup == null) {
			_element.renderTransformed();			
		} else {
	        GL11.glEnable(GL11.GL_TEXTURE_2D);

	        GL11.glPushMatrix();
	        glTranslatef(4f, 0f, 0f);
	        GL11.glScalef(20f, 20f, 20f);
	        GL11.glRotatef(170f, 1f, 0f, 0f);
	        GL11.glRotatef(-16f, 0f, 1f, 0f);
	        TextureManager.getInstance().bindTexture("terrain");

	        Block block = _blockGroup.getArchetypeBlock();
	        block.render();

	        GL11.glPopMatrix();

	        GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
	}
	
	/** 
	 * @return x-offset in icon sheet
	 */
	public int getX() {
		return _x;
	}
	
	/**
	 * 
	 * @return y-offset in icon sheet
	 */
	public int getY() {
		return _y;
	}
	
	private void setAtlasPosition(int x, int y) {
		_x = x;
		_y = y;
		
		if (_element == null) {
			return;
		}
		
		_element.getTextureOrigin().set(new Vector2f(x * 0.0625f, y * 0.0625f));
	}
}
