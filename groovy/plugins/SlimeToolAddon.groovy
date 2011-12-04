import com.github.begla.blockmania.tools.Tool
import com.github.begla.blockmania.world.characters.mobs.GelatinousCube
import com.github.begla.blockmania.game.Blockmania

class SlimeTool implements Tool {

    public void executeLeftClickAction() {
        println "Executing left click! Going to spawn a Gelatinous Cube"

        GelatinousCube s = new GelatinousCube(Blockmania.getInstance().getActiveWorld())
        s.setSpawningPoint(Blockmania.getInstance().getActiveWorld().getPlayer().getPosition())
        s.respawn()

        Blockmania.getInstance().getActiveWorld().getMobManager().addMob(s)
    }

    public void executeRightClickAction() {
        println "Executing right click! Which means not doing anything";
    }
}

def slimeGun = new SlimeTool()

println "SlimeToolAddon.groovy is trying to add the slimeGun tool"
blockmania.getActiveWorld().getPlayer().addTool(slimeGun)