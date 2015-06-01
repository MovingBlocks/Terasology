package coloringCommands;

import java.util.List;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;

@RegisterSystem
public class PlaceBlockCommand extends BaseComponentSystem {

    private String[] colors = {"Red", "Blue", "Green"};

    @Command(shortDescription = "Places a block in front of the player of the color specified({Red,Blue,Green} implemented)")
    public String placeColorBlock(@CommandParam("colorBlock") String colorBlock) {
    	if(!isImplementedColor(colorBlock))
    		return "Put an implemented color in {Red, Blue, Green}";
    	WorldRenderer renderer = CoreRegistry.get(WorldRenderer.class);
    	Camera camera= renderer.getActiveCamera();
    	
    	Vector3f spawnPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        spawnPos.add(offset);

        BlockFamily blockFamily;
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        List<BlockUri> matchingUris = blockManager.resolveAllBlockFamilyUri(colorBlock);
        blockFamily = blockManager.getBlockFamily(matchingUris.get(0));
        WorldProvider world = CoreRegistry.get(WorldProvider.class);
        if (world != null) {
            world.setBlock(new Vector3i((int) spawnPos.x, (int) spawnPos.y, (int) spawnPos.z), blockFamily.getArchetypeBlock());

            StringBuilder builder = new StringBuilder();
            builder.append(blockFamily.getArchetypeBlock());
            builder.append(" block placed at position (");
            builder.append((int) spawnPos.x).append((int) spawnPos.y).append((int) spawnPos.z).append(")");
            return builder.toString();
        }
        throw new IllegalArgumentException("Sorry, something went wrong!");
    }

    /**
     * Return whether or not the color is implemented
     * @param colorBlock
     * @return
     */
	private boolean isImplementedColor(String colorBlock) 
	{
		for(String color : colors)
		{
			if (colorBlock.equals(color)) return true;
		}
		return false;
	}
	
	@Command(shortDescription = "Puts the floor of the constant city of the color specified({Red,Blue,Green} implemented)")
    public String placeColorFloor(@CommandParam("colorBlock") String colorBlock) {
    	if(!isImplementedColor(colorBlock))
    		return "Put an implemented color in {Red, Blue, Green}";
    	WorldRenderer renderer = CoreRegistry.get(WorldRenderer.class);
    	Camera camera= renderer.getActiveCamera();
    	
    	Vector3f spawnPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        spawnPos.add(offset);

        BlockFamily blockFamily;
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        List<BlockUri> matchingUris = blockManager.resolveAllBlockFamilyUri(colorBlock);
        blockFamily = blockManager.getBlockFamily(matchingUris.get(0));
        WorldProvider world = CoreRegistry.get(WorldProvider.class);
        if (world != null) {
        	for(int x = 0; x<=19; ++x)
        		for(int z = 0; z<=19; ++z)
        			world.setBlock(new Vector3i(x, 10, z), blockFamily.getArchetypeBlock());
            return "Success";
        }
        throw new IllegalArgumentException("Sorry, something went wrong!");
    }

}
