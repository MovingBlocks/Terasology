import com.github.begla.blockmania.world.characters.Slime

// Just a silly little script that says hello and shows it can reference the world (look ma, no need to even declare world! It's magic via binding)
println "Hello!"
println "Player: " + world.getPlayer()

//world = binding.variables.world
println "World: " + world

slime = new Slime(world)
println "Slime: " + slime
