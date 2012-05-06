package org.terasology.model.inventory;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.AssetManager;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockFamily;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * Icon for rendering items in inventory.
 */
@SuppressWarnings("rawtypes")
public class Icon {
	private static Map<String, Icon> icons;

	private UIGraphicsElement _element;
	private BlockFamily _blockFamily;
	private int _x;
	private int _y;
    private Texture terrainTex;

	/**
	 * Creates Icon for BlockFamily class.
	 *
	 * @param blockFamily
	 */
	public Icon(BlockFamily blockFamily) {
		_element = null;
		_blockFamily = blockFamily;
		setAtlasPosition(0, 0);
        terrainTex = AssetManager.loadTexture("engine:terrain");
	}

	/**
	 * Creates an Icon for a non-BlockFamily class
	 */
	public Icon() {
		_element = new UIGraphicsElement(AssetManager.loadTexture("engine:items"));
		_blockFamily = null;

        _element.setSize(new Vector2f(32, 32));
        _element.getTextureSize().set(new Vector2f(0.0624f, 0.0624f));
        _element.setVisible(true);
        _element.setPosition(new Vector2f(-10f, -16f));

        setAtlasPosition(0, 0);
	}

	/**
	 * Returns the icon for <code>name</code>.
	 *
	 * @param name the name of the icon
	 * @return the Icon for item
	 */
	public static Icon get(String name) {
		if (icons == null) {
			loadIcons();
		}

		return icons.get(name.toLowerCase(Locale.ENGLISH));
	}

	private static void loadIcons() {
		icons = new HashMap<String, Icon>();

        //TODO: Hmm, does this mean we have hard coded our tool displays? Should try to move this to ToolManager in that case?
		Icon axeIcon = new Icon();
		Icon pickAxeIcon = new Icon();
		Icon redPowderIcon = new Icon();
		Icon noteIcon = new Icon();
        Icon testTubeIcon = new Icon();
        Icon greenOrbIcon = new Icon();

		axeIcon.setAtlasPosition(1, 7);
		pickAxeIcon.setAtlasPosition(1, 6);
		redPowderIcon.setAtlasPosition(8, 3);
        testTubeIcon.setAtlasPosition(15, 8);
		noteIcon.setAtlasPosition(10, 3);
        greenOrbIcon.setAtlasPosition(14, 1);

		icons.put("axe", axeIcon);
		icons.put("pickaxe", pickAxeIcon);
		icons.put("dynamite", redPowderIcon);
		icons.put("blueprint", noteIcon);
        icons.put("debug", greenOrbIcon);
        icons.put("railgun", testTubeIcon);
	}

	/**
	 * Draw the icon.
	 */
	public void render() {
		if (_blockFamily == null) {
			_element.renderTransformed();
		} else {
	        GL11.glEnable(GL11.GL_TEXTURE_2D);

	        GL11.glPushMatrix();
	        glTranslatef(4f, 0f, 0f);
	        GL11.glScalef(20f, 20f, 20f);
	        GL11.glRotatef(170f, 1f, 0f, 0f);
	        GL11.glRotatef(-16f, 0f, 1f, 0f);
            glBindTexture(GL11.GL_TEXTURE_2D, terrainTex.getId());

	        Block block = _blockFamily.getArchetypeBlock();
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
