/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package console;


import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.permission.PermissionManager;

/**
 * @author mperalta92, kidonkey
 */
@RegisterSystem
public class GitCommand extends BaseComponentSystem{

    @Command(shortDescription = "activate the GitHub metrics",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String github() throws IOException, NoHeadException, GitAPIException {
    	
    	FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File("C:/Users/RicardoMatias/Desktop/Ejemplo"))
        		.readEnvironment().findGitDir().build();
    	RevWalk walk = new RevWalk(repository);
    	for(RevCommit commit : walk){
    		String men = commit.getFullMessage();
    		System.out.println(men);
    	}
    	walk.dispose();
    	return "this comand will execute the githubs metrics";
    }
}
