import com.github.begla.blockmania.main.Blockmania
import com.github.begla.blockmania.tools.Tool
import com.github.begla.blockmania.world.characters.Slime

def Blockmania blockmania = Blockmania.getInstance();

class SlimeTool implements Tool {
    public void executeLeftClickAction() {
        println "Executing left click! Going to spawn a Slime"

        Slime s = new Slime(blockmania.getActiveWorld())
        s.setSpawningPoint blockmania.getActiveWorld().getPlayer().getPosition()
        s.respawn()

        blockmania.getMobManager().addMob s
    }

    public void executeRightClickAction() {
        println "Executing right click! Which means not doing anything";
    }
}

def slimeGun = new SlimeTool()

println "SlimeToolAddon.groovy is trying to add the slimeGun tool"
blockmania.getActiveWorld().getPlayer().addTool slimeGun