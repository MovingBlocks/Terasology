package console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class BugMetric extends GitHubMetric {
	
	public static void getBugs(Git git, Repository repo, Hashtable<String, Boolean> table) throws GitAPIException,
			NoHeadException, RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
		String com;
		//FetchCommand classflag = new  FetchCommand();
		int i=0;
        Iterable<RevCommit> log = git.log()
        		.call();
        for(RevCommit commit : log){
        	com = commit.getFullMessage();
        	System.out.println(com);
        	//System.out.println(commit.getId());
        	System.out.println(hasBug(com));
        	ArrayList<String> classes = getClassesInCommit(i,repo);
        	for(String c: classes) {
        		System.out.println("Bugs table: " + table);
        		if (table.get(c)==null) {
        			table.put(c, hasBug(com));
        			
        		}
        		else {table.remove(c);
        		table.put(c,hasBug(com));
        		}
        	}
        	
        	i++;
        }
        System.out.println("Final bugs table: " + table);
        
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
