import com.github.begla.blockmania.game.Blockmania
import com.github.begla.blockmania.tools.Tool
import com.github.begla.blockmania.world.characters.mobs.GelatinousCube

class SlimeTool implements Tool {

    public void executeLeftClickAction() {
        println "Executing left click! Going to spawn a Gelatinous Cube"

        GelatinousCube s = new GelatinousCube(Blockmania.getInstance().getActiveWorldRenderer())
        s.setSpawningPoint(Blockmania.getInstance().getActiveWorldRenderer().getPlayer().getPosition())
        s.respawn()

        Blockmania.getInstance().getActiveWorldRenderer().getMobManager().addMob(s)
    }

    public void executeRightClickAction() {
        println "Executing right click! Which means not doing anything";
    }
}

def slimeGun = new SlimeTool()

println "SlimeToolAddon.groovy is trying to add the slimeGun tool"
blockmania.getActiveWorldRenderer().getPlayer().addTool(slimeGun)