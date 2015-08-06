package console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class GitHubMetric {
	
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
}
