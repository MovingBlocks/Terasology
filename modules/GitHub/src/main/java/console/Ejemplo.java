package console;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.FetchCommand;
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
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FS;

public class Ejemplo {

	public static void main(String[] args)throws IOException, NoHeadException, GitAPIException {
		// Parameters
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
        extractedBugs(git, table);  
        git.close();
     
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
		//git.pull().setRemote(remotePath).call();
	}


	private static void extractedBugs(Git git, Hashtable<String, Boolean> table) throws GitAPIException,
			NoHeadException {
		String com;
		//FetchCommand classflag = new  FetchCommand();
		
        Iterable<RevCommit> log = git.log()
        		.call();
        for(RevCommit commit : log){
        	com = commit.getFullMessage();
        	System.out.println(com);
        	//System.out.println(getName(com));
        	System.out.println(hasBug(com));
        }
	}


	private static char[] getName(String com) {
		// TODO Auto-generated method stub
		return null;
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

}
