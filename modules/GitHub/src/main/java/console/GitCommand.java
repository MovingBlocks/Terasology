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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.permission.PermissionManager;

/**
 * @author mperalta92, kidonkey
 */
@RegisterSystem
public class GitCommand extends BaseComponentSystem{
	public static StringBuilder output;
    @Command(shortDescription = "activate the GitHub metrics",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String github() throws IOException, NoHeadException, GitAPIException {
    	// Parameters
    		output=new StringBuilder();
    			String localPath, remotePath;
    			Repository localRepo;
    		    Git git;
    		    String projectName = "My_project";
    		    Hashtable<String, Boolean> table = new Hashtable<String, Boolean>();
    		    /*------Setting Parameters-------------------------------*/
    		    
    			localPath="C:/Users/I/Desktop/temp/"+projectName;
    			remotePath= "https://github.com/mperalta92/Ejemplo.git";
    			localRepo = new FileRepository(localPath + "/.git");
    	        git = new Git(localRepo);
    	        /*---------clone remote repository---------------------*/
    	        //preguntar si existe el repositorio en el folder del localpath, si existe hacer pull , si no clone
    	      
    	       // if( RepositoryCache.FileKey.isGitRepository(new File(localPath), FS.DETECTED)){
    	        if( hasAtLeastOneReference(localRepo)){
    	        	gitPull(git, remotePath);
    	        //	}
    	        }else{
    	        	gitGlone(localPath, remotePath);
    	        }
    	        /*----------get commits description--------------------*/
    	        extractBugs(git, localRepo, table);  
    	        getBranchLog(localRepo);
    	        showChangedFilesBetweenCommits(localRepo);
    	        git.close();
    	return output.toString();
    }
    private static boolean hasAtLeastOneReference(Repository repo) {

	    for (Ref ref : repo.getAllRefs().values()) {
	        if (ref.getObjectId() == null) {
	        	continue;
	        }
	        return true;
	    }

	    return false;
	}

	private static void gitPull(Git git, String remotePath) throws WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, RefNotAdvertisedException, NoHeadException, TransportException, GitAPIException {
		// This method execute a pull from remote repository
		git.pull().call();
	}


	private static void extractBugs(Git git, Repository repo, Hashtable<String, Boolean> table) throws GitAPIException,
			NoHeadException, RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
		String com;
		//FetchCommand classflag = new  FetchCommand();
		int i=0;
        Iterable<RevCommit> log = git.log()
        		.call();
        for(RevCommit commit : log){
        	com = commit.getFullMessage();
        	output.append(com+'\n');
        	output.append(commit.getId().toString()+'\n');
        	output.append(hasBug(com)+""+'\n');
        	ArrayList<String> classes = getClassesInCommit(i,repo);
        	for(String c: classes) {
        		if (table.get(c) == null) {
        			table.put(c, hasBug(com));
        		}
        	}
        	i++;
        }
	}
	public void traverseTree(Repository repo,AnyObjectId commit) throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		TreeWalk treeWalk = new TreeWalk( repo );
		treeWalk.addTree( commit );
		treeWalk.setRecursive( true );
		treeWalk.setPostOrderTraversal( true );
		while( treeWalk.next() ) {
			int fileMode = Integer.parseInt( treeWalk.getFileMode( 0 ).toString() );
			String objectId = treeWalk.getObjectId( 0 ).name();
			String path = treeWalk.getPathString();
			output.append( String.format( "%06d %s %s", fileMode, objectId, path )+'\n' );
		}
	}




	private static void gitGlone(String localPath, String remotePath)
			throws GitAPIException, InvalidRemoteException, TransportException {
		CloneCommand clone =Git.cloneRepository().setURI(remotePath);        
        clone.setDirectory(new File(localPath)).call();
	}
	

	public static boolean hasBug(String message){
		String aux;
		boolean bugged = false;
		for(int i=0;i<(message.length()-5);++i){
			aux=message.substring(i, i+5);
			aux.toLowerCase();
			if(aux.substring(0,3).equals("bug")) bugged = true;
			if(aux.equals("fixed")) {
				bugged = false;
				break;
			}
		}
		return bugged;
	}
	public static ArrayList<String> getClassesInCommit(int commitsFromHead, Repository repo) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException, GitAPIException {
		Repository repository =repo;
		String commitPointer = "HEAD^";
		String tree= "{tree}";
		ArrayList<String> classes = new ArrayList<String>();

        for (int i = 0; i < commitsFromHead+1; i++) {
        	commitPointer=commitPointer+"^";
        }
    	ObjectId head = repository.resolve(commitPointer+tree);
    	commitPointer=commitPointer+"^";
		ObjectId oldHead = repository.resolve(commitPointer+tree);
		if (oldHead != null) {
			output.append("Printing diff between tree: " + oldHead
					+ " and " + head+"\n");
			// prepare the two iterators to compute the diff between
			ObjectReader reader = repository.newObjectReader();
			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, oldHead);
			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, head);
			// finally get the list of changed files
			List<DiffEntry> diffs = new Git(repository).diff()
					.setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
			for (DiffEntry entry : diffs) {
				output.append("Class: " + extractClassFromEntry(entry)+ "\n");
				classes.add(extractClassFromEntry(entry));
			}
		}
		
		repository.close();
		return classes;
	}
	 public static String extractClassFromEntry(DiffEntry entry) {
	     	String stringEntry= entry.toString();
	     	int start;
	     	int end = stringEntry.length()-6;
	     	for (int j = 0; j < end;j++) {
	     		if (stringEntry.charAt(j)=='/') {
	     			start = j+1;
	     			return stringEntry.substring(start,end);
	     		}
	     	}
	     	return null;
	     }
	 public static void getBranchLog(Repository repo) throws IOException, GitAPIException  {
		 Repository repository = repo;

        Iterable<RevCommit> logs = new Git(repository).log()
                .call();
        int count = 0;
        for (RevCommit rev : logs) {
        	output.append("Commit: " + rev + ", name: " + rev.getName() + ", id: " + rev.getId().getName()+"\n");
            count++;
        }
        output.append("Had " + count + " commits overall on current branch\n");
        repository.close();
	}
	 public static void showChangedFilesBetweenCommits(Repository repo) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException, GitAPIException {
			Repository repository =repo;
			int commits = 4;
			String commitPointer = "HEAD^";
			String tree= "{tree}";

	        for (int i = 0; i < commits-1; i++) {
	        	ObjectId head = repository.resolve(commitPointer+tree);
	        	commitPointer=commitPointer+"^";
				ObjectId oldHead = repository.resolve(commitPointer+tree);
				if (oldHead == null) return;
				output.append("Printing diff between tree: " + oldHead
						+ " and " + head+"\n");
				// prepare the two iterators to compute the diff between
				ObjectReader reader = repository.newObjectReader();
				CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
				oldTreeIter.reset(reader, oldHead);
				CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
				newTreeIter.reset(reader, head);
				// finally get the list of changed files
				List<DiffEntry> diffs = new Git(repository).diff()
						.setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
				for (DiffEntry entry : diffs) {
					output.append("Class: " + extractClassFromEntry(entry)+"\n");
				}
				repository.close();
			}
	        
	       
		}
	 private static void extractVersions(Git git, Repository repo, Hashtable<String, Integer> table) throws GitAPIException,
		NoHeadException, RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
	String com;
	int i=0;
	Iterable<RevCommit> log = git.log()
			.call();
	for(RevCommit commit : log){
		com = commit.getFullMessage();
		System.out.println(com);
		System.out.println(commit.getId());
		ArrayList<String> classes = getClassesInCommit(i,repo);
		for(String c: classes) {
			if (table.get(c) == null) {
				table.put(c, 1);
			}
			else {
				int versions = table.get(c);
				table.put(c,versions+1);
			}
		}
		i++;
	}
}
}
