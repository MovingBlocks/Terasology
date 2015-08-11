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
		ArrayList<String> unusedClasses = browseTreeRecursive(repo);
		Iterable<RevCommit> log = git.log()
				.call();
		for(RevCommit commit : log) {
			System.out.println(commit.getFullMessage());
			ArrayList<String> classesInCommit = getClassesInCommit(i,repo);
			if (classesInCommit.isEmpty()) {
				for (String c: unusedClasses) {
					classesInCommit.add(c);
				}
			}
			for(String className: classesInCommit) {
				if (table.get(className) == null) {
					table.put(className, 1);
				}
				else {
					int versions = table.get(className);
					table.put(className,versions+1);
				}
				if (unusedClasses.contains(className)) {
					unusedClasses.remove(className);
				}
			}
			System.out.println(table+"\n*************\n");
			i++;
		}
	}
}
