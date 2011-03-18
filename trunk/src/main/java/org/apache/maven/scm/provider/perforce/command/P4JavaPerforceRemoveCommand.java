package org.apache.maven.scm.provider.perforce.command;

import java.io.IOException;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.remove.AbstractRemoveCommand;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.P4JavaPerforceRepository;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.server.IServer;

public class P4JavaPerforceRemoveCommand extends AbstractRemoveCommand {

	@Override
	protected ScmResult executeRemoveCommand(ScmProviderRepository repository, ScmFileSet fileSet, String message) throws ScmException {
		P4JavaPerforceRepository p4Repo = (P4JavaPerforceRepository) repository;
		try {
			List<IFileSpec> fileSpecs = p4Repo.transformFiles(fileSet.getFileList());
			IServer server = p4Repo.getPerforceServer();
			IClient client = p4Repo.getWorkspace(server, fileSet.getBasedir());
			server.setCurrentClient(client);
			List<IFileSpec> removedFiles = server.getCurrentClient().deleteFiles(fileSpecs, -1, Boolean.FALSE);
			return new RemoveScmResult(P4JavaPerforceRepository.NO_COMMAND_LINE, p4Repo.transformFileSpec(removedFiles));
		} catch (IOException e) {
			throw new ScmException("Error obtaining local path for file set.", e);
		} catch (Exception e) {
			throw new ScmException("Could not remove files from Perforce.", e);
		}
	}

}
