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

	public static void getBugs(Git git, Repository repo,
			Hashtable<String, Boolean> table) throws GitAPIException,
			NoHeadException, RevisionSyntaxException, AmbiguousObjectException,
			IncorrectObjectTypeException, IOException {
		ArrayList<String> unusedClasses = browseTreeRecursive(repo);
		String message;
		int i = 0;
		Iterable<RevCommit> log = git.log().call();
		for (RevCommit commit : log) {
			message = commit.getFullMessage();
			System.out.println(message);
			// System.out.println(commit.getId());
			System.out.println(hasBug(message));
			ArrayList<String> classesInCommit = getClassesInCommit(i, repo);
			if (classesInCommit.isEmpty()) {
				for (String c: unusedClasses) {
					classesInCommit.add(c);
				}
			}
			for (String className : classesInCommit) {
				if (table.get(className) == null) {
					table.put(className, hasBug(message));
				} else {
					table.remove(className);
					table.put(className, hasBug(message));
				}
				if (unusedClasses.contains(className)) {
					unusedClasses.remove(className);
				}
			}
			
			i++;
		}
		System.out.println("bugs: " + table);
	}

	public static boolean hasBug(String message) {
		String aux;
		boolean bugged = false;
		for (int i = 0; i < (message.length() - 5); ++i) {
			aux = message.substring(i, i + 5);
			aux.toLowerCase();
			if (aux.substring(0, 3).equals("bug"))
				bugged = true;
			if (aux.equals("fixed")) {
				bugged = false;
				break;
			}
		}
		return bugged;
	}
}
