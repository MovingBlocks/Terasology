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

public class VersionMetric extends GitHubMetric{
	
	public void getVersions(Git git, Repository repo, Hashtable<String, Integer> table) throws GitAPIException,
			NoHeadException, RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
		int i=0;
		Iterable<RevCommit> log = git.log()
				.call();
		for(RevCommit commit : log) {
			System.out.println(commit.getFullMessage());
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
			System.out.println(table+"\n*************\n");
			i++;
		}
	}
}
