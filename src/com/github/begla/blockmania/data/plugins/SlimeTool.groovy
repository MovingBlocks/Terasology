package com.github.begla.blockmania.data.plugins

import com.github.begla.blockmania.world.characters.Slime
import com.github.begla.blockmania.tools.Tool
import com.github.begla.blockmania.main.Blockmania

class SlimeTool implements Tool
{
    public void executeLeftClickAction() {
        println "Executing left click! Going to spawn a Slime"
        Slime s = new Slime(Blockmania.getInstance().getActiveWorld())
        s.setSpawningPoint(Blockmania.getInstance().getActiveWorld().getPlayer().getSpawningPoint())
        s.respawn()
        Blockmania.getInstance().getMobManager().addMob(s)
    }

    public void executeRightClickAction() {
        println "Executing right click! Which means not doing anything";
    }
}