package org.apache.maven.scm.provider.perforce.command;

import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.perforce.P4JavaPerforceRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.server.IServer;

public class P4JavaPerforceAddCommandTest {
	/**
	 * The client that represents the workspace of the user.
	 */
	private IClient client;
	/**
	 * The Perforce server that commands are to be executed against
	 */
	private IServer server;
	/**
	 * The SCM Provider's repository.
	 */
	private P4JavaPerforceRepository repository;
	/**
	 * The base directory of the ScmFileSet.
	 */
	private File baseDir;
	/**
	 * The list of file specs to add to the Perforce depot.
	 */
	private List<IFileSpec> fileSpecsToAdd;
	/**
	 * The list of file specs that the depot added.
	 */
	private List<IFileSpec> fileSpecsAdded;
	/**
	 * The list of scm files that were added as a result of the command.
	 */
	private List<ScmFile> scmFilesAdded;
	/**
	 * The parameters for the add command.
	 */
	private CommandParameters parameters;
	/**
	 * The list of files associated with the ScmFileSet.
	 */
	private List<File> files;
	private AbstractCommand addCommand;
	private ScmFileSet scmFileSet;
	
	@Before
	public void setup() throws Exception {
		client = mock(IClient.class);
		server = mock(IServer.class);
		repository = mock(P4JavaPerforceRepository.class);
		baseDir = new File("src/test/resources");
		fileSpecsToAdd = new ArrayList<IFileSpec>();
		fileSpecsAdded = new ArrayList<IFileSpec>();
		scmFilesAdded = new ArrayList<ScmFile>();
		parameters = new CommandParameters();
		files = new ArrayList<File>();
		addCommand = new P4JavaPerforceAddCommand();
		scmFileSet = mock(ScmFileSet.class);

		
		// P4Java Repository
		when(repository.getPerforceServer()).thenReturn(server);
		when(repository.getWorkspace(server, baseDir)).thenReturn(client);
		when(server.getCurrentClient()).thenReturn(client);
		when(repository.transformFiles(files)).thenReturn(fileSpecsToAdd);
		when(repository.transformFileSpec(fileSpecsAdded)).thenReturn(scmFilesAdded);
		
		// SCM File Set
		when(scmFileSet.getBasedir()).thenReturn(baseDir);
		when(scmFileSet.getFileList()).thenReturn(files);
				
		parameters.setString(CommandParameter.BINARY, "false");
		parameters.setString(CommandParameter.MESSAGE, "Hi.");
	}

	/**
	 * Attempt to add one file, expect that one file is returned as added.
	 * @throws Exception
	 */
	@Test
	public void testAddOne() throws Exception {
		File fileToAdd = new File("src/test/resources/testAdd/fileToAdd.txt");
		files.add(fileToAdd);

		IFileSpec fileSpec = mock(IFileSpec.class);
		when(fileSpec.getDepotPathString()).thenReturn("//depot/path/string/fileToAdd.txt");
		when(fileSpec.getAction()).thenReturn(FileAction.ADD);
		fileSpecsToAdd.add(fileSpec);
		fileSpecsAdded.add(fileSpec);

		ScmFile scmFile = new ScmFile(fileToAdd.getAbsolutePath(), ScmFileStatus.ADDED);
		scmFilesAdded.add(scmFile);

		when(client.addFiles(fileSpecsToAdd, Boolean.FALSE, -1, null, Boolean.FALSE)).thenReturn(fileSpecsAdded);
		
		AddScmResult addResult = (AddScmResult) addCommand.execute(repository, scmFileSet, parameters);
		Assert.assertTrue("The result was not a success.", addResult.isSuccess());
	}

	/**
	 * Attempt to add nothing.
	 * @throws Exception
	 */
	@Test
	public void testAddNothing() throws Exception {
		when(client.addFiles(fileSpecsToAdd, Boolean.FALSE, -1, null, Boolean.FALSE)).thenReturn(fileSpecsAdded);

		AddScmResult addResult = (AddScmResult) addCommand.execute(repository, scmFileSet, parameters);
		Assert.assertTrue("The result was not a success.", addResult.isSuccess());
	}
	
	/**
	 * Attempts to add one file, but the operation fails and no exception is thrown by P4Java.  The
	 * result of the command should be a failure.
	 * @throws Exception
	 */
	@Test
	public void testAddFail() throws Exception {
		File fileToAdd = new File("src/test/resources/testAdd/fileToAdd.txt");
		files.add(fileToAdd);

		when(client.addFiles(fileSpecsToAdd, Boolean.FALSE, -1, null, Boolean.FALSE)).thenReturn(fileSpecsAdded);

		AddScmResult addResult = (AddScmResult) addCommand.execute(repository, scmFileSet, parameters);
		Assert.assertTrue("Should not be a success, attempt to add should have failed.", !addResult.isSuccess());
	}
	
	/**
	 * Attempts to add a file but the P4Java API throws an exception.  Verify the exception is propagated.
	 * @throws Exception
	 */
	@Test(expected=ScmException.class)
	public void testAddException() throws Exception {
		File fileToAdd = new File("src/test/resources/testAdd/fileToAdd.txt");
		files.add(fileToAdd);

		ScmFile scmFile = new ScmFile(fileToAdd.getAbsolutePath(), ScmFileStatus.ADDED);
		scmFilesAdded.add(scmFile);

		when(client.addFiles(fileSpecsToAdd, Boolean.FALSE, -1, null, Boolean.FALSE)).thenThrow(new AccessException("User does not have permission to add file."));

		addCommand.execute(repository, scmFileSet, parameters);
	}
}
