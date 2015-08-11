package console;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

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
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import org.eclipse.jgit.lib.Constants;

import java.util.List;

public class Ejemplo {

	public static void main(String[] args) throws IOException, NoHeadException,
			GitAPIException {
		// Parameters
		String localPath, remotePath;
		Repository localRepo;
		Git git;
		String projectName = "My_project";
		Hashtable<String, Boolean> table = new Hashtable<String, Boolean>();
		Hashtable<String, Integer> versions = new Hashtable<String, Integer>();
		/*------Setting Parameters-------------------------------*/

		localPath = "C:/Users/I/Desktop/temp/" + projectName;
		remotePath = "https://github.com/mperalta92/Ejemplo.git";
		localRepo = new FileRepository(localPath + "/.git");
		git = new Git(localRepo);
		/*---------clone remote repository---------------------*/
		// preguntar si existe el repositorio en el folder del localpath, si
		// existe hacer pull , si no clone

		// if( RepositoryCache.FileKey.isGitRepository(new File(localPath),
		// FS.DETECTED)){
		if (hasAtLeastOneReference(localRepo)) {
			gitPull(git, remotePath);
			// }
		} else {
			gitGlone(localPath, remotePath);
		}
		/*----------get commits description--------------------*/
		VersionMetric version = new VersionMetric();
		getBranchLog(localRepo);
		// showChangedFilesBetweenCommits(localRepo);
		BugMetric bugs = new BugMetric();
		bugs.getBugs(git, localRepo, table);
		version.getVersions(git,localRepo,versions);
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

	private static void gitPull(Git git, String remotePath)
			throws WrongRepositoryStateException,
			InvalidConfigurationException, DetachedHeadException,
			InvalidRemoteException, CanceledException, RefNotFoundException,
			RefNotAdvertisedException, NoHeadException, TransportException,
			GitAPIException {
		// This method execute a pull from remote repository
		git.pull().call();
	}

	private static void extractBugs(Git git, Repository repo,
			Hashtable<String, Boolean> table) throws GitAPIException,
			NoHeadException, RevisionSyntaxException, AmbiguousObjectException,
			IncorrectObjectTypeException, IOException {
		String com;
		// FetchCommand classflag = new FetchCommand();
		int i = 0;
		Iterable<RevCommit> log = git.log().call();
		for (RevCommit commit : log) {
			com = commit.getFullMessage();
			System.out.println(com);
			// System.out.println(commit.getId());
			System.out.println(hasBug(com));
			ArrayList<String> classes = getClassesInCommit(i, repo);
			for (String c : classes) {
				System.out.println("Bugs table: " + table);
				if (table.get(c) == null) {
					table.put(c, hasBug(com));

				} else {
					table.remove(c);
					table.put(c, hasBug(com));
				}
			}

			i++;
		}
		System.out.println("Final bugs table: " + table);

	}

	private static void extractVersions(Git git, Repository repo,
			Hashtable<String, Integer> table) throws GitAPIException,
			NoHeadException, RevisionSyntaxException, AmbiguousObjectException,
			IncorrectObjectTypeException, IOException {
		int i = 0;
		Iterable<RevCommit> log = git.log().call();
		for (RevCommit commit : log) {
			// System.out.println(commit.getFullMessage());
			ArrayList<String> classes = getClassesInCommit(i, repo);
			for (String c : classes) {
				if (table.get(c) == null) {
					table.put(c, 1);
				} else {
					int versions = table.get(c);
					table.put(c, versions + 1);
				}
			}

			i++;
		}
		System.out.println(table);

	}

	public void traverseTree(Repository repo, AnyObjectId commit)
			throws MissingObjectException, IncorrectObjectTypeException,
			CorruptObjectException, IOException {
		TreeWalk treeWalk = new TreeWalk(repo);
		treeWalk.addTree(commit);
		treeWalk.setRecursive(true);
		treeWalk.setPostOrderTraversal(true);
		while (treeWalk.next()) {
			int fileMode = Integer.parseInt(treeWalk.getFileMode(0).toString());
			String objectId = treeWalk.getObjectId(0).name();
			String path = treeWalk.getPathString();
			// System.out.println( String.format( "%06d %s %s", fileMode,
			// objectId, path ) );
		}
	}

	private static void gitGlone(String localPath, String remotePath)
			throws GitAPIException, InvalidRemoteException, TransportException {
		CloneCommand clone = Git.cloneRepository().setURI(remotePath);
		clone.setDirectory(new File(localPath)).call();
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

	public static void getBranchLog(Repository repo) throws IOException,
			GitAPIException {
		Repository repository = repo;

		Iterable<RevCommit> logs = new Git(repository).log().call();
		int count = 0;
		for (RevCommit rev : logs) {
			// System.out.println("Commit: " + rev + ", name: " + rev.getName()
			// + ", id: " + rev.getId().getName());
			count++;
		}
		// System.out.println("Had " + count +
		// " commits overall on current branch");
		repository.close();
	}

	public static void showChangedFilesBetweenCommits(Repository repo)
			throws RevisionSyntaxException, AmbiguousObjectException,
			IncorrectObjectTypeException, IOException, GitAPIException {
		Repository repository = repo;
		int commits = 4;
		String commitPointer = "HEAD^";
		String tree = "{tree}";

		for (int i = 0; i < commits - 1; i++) {
			ObjectId head = repository.resolve(commitPointer + tree);
			commitPointer = commitPointer + "^";
			ObjectId oldHead = repository.resolve(commitPointer + tree);
			if (oldHead == null)
				return;
			// System.out.println("Printing diff between tree: " + oldHead+
			// " and " + head);
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
				// System.out.println("Class: " + extractClassFromEntry(entry));
			}
			repository.close();
		}

	}

	public static ArrayList<String> getClassesInCommit(int commitsFromHead,
			Repository repo) throws RevisionSyntaxException,
			AmbiguousObjectException, IncorrectObjectTypeException,
			IOException, GitAPIException {
		Repository repository = repo;
		String commitPointer = "HEAD^";
		String tree = "{tree}";
		ArrayList<String> classes = new ArrayList<String>();

		for (int i = 0; i < commitsFromHead + 1; i++) {
			commitPointer = commitPointer + "^";
		}
		ObjectId head = repository.resolve(commitPointer + tree);
		commitPointer = commitPointer + "^";
		ObjectId oldHead = repository.resolve(commitPointer + tree);
		if (oldHead != null) {
			// System.out.println("Printing diff between tree: " + oldHead+
			// " and " + head);
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
				// System.out.println("extracted class\t " +
				// extractClassFromEntry(entry));
				classes.add(extractClassFromEntry(entry));
			}
		}
		repository.close();
		return classes;
	}

	public static String extractClassFromEntry(DiffEntry entry) {
		String stringEntry = entry.toString();
		int start;
		int end = stringEntry.length() - 6;
		for (int j = 0; j < end; j++) {
			if (stringEntry.charAt(j) == '/') {
				start = j + 1;
				return stringEntry.substring(start, end);
			}
		}
		return null;
	}

	public static void browseTree(Repository repository)
			throws RevisionSyntaxException, AmbiguousObjectException,
			IncorrectObjectTypeException, IOException {
		ObjectId revId = repository.resolve(Constants.HEAD);
		TreeWalk treeWalk = new TreeWalk(repository);

		treeWalk.addTree(new RevWalk(repository).parseTree(revId));

		while (treeWalk.next()) {
			System.out.println("---------------------------");
			System.out.append("name: ").println(treeWalk.getNameString());
			System.out.append("path: ").println(treeWalk.getPathString());

			ObjectLoader loader = repository.open(treeWalk.getObjectId(0));

			System.out.append("directory: ").println(
					loader.getType() == Constants.OBJ_TREE);
			System.out.append("size: ").println(loader.getSize());
		}
	}

	// Iterates over items in a repo object.
	// @return A list of all classes in the repo
	public static ArrayList<String> browseTreeRecursive(Repository repository)
			throws IOException {
		ArrayList<String> classes = new ArrayList<String>();
		Ref head = repository.getRef("HEAD");
		RevWalk walk = new RevWalk(repository);
		RevCommit commit = walk.parseCommit(head.getObjectId());
		RevTree tree = commit.getTree();
		System.out.println("Having tree: " + tree);
		TreeWalk treeWalk = new TreeWalk(repository);
		treeWalk.addTree(tree);
		treeWalk.setRecursive(true);
		while (treeWalk.next()) {
			if (treeWalk.getPathString().substring(0, 3).equals("src")) {
				classes.add(treeWalk.getPathString().substring(4,
						treeWalk.getPathString().length() - 5));
			}
		}
		return classes;
	}
}
