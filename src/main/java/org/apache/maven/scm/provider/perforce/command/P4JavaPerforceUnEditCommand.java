package org.apache.maven.scm.provider.perforce.command;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.unedit.AbstractUnEditCommand;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.P4JavaPerforceRepository;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;

public class P4JavaPerforceUnEditCommand extends AbstractUnEditCommand {

	@SuppressWarnings("unchecked")
	@Override
	protected ScmResult executeUnEditCommand(ScmProviderRepository repository, ScmFileSet fileSet) throws ScmException {
		P4JavaPerforceRepository p4Repo = (P4JavaPerforceRepository) repository;
		String errorMessage = "Could not revert client files.";
		List<IFileSpec> fileSpecs = null;
		try {
			fileSpecs = p4Repo.transformFiles(fileSet.getFileList());
			IServer server = p4Repo.getPerforceServer();
			IClient client = p4Repo.getWorkspace(server, fileSet.getBasedir());
			server.setCurrentClient(client);
			List<IFileSpec> revertedFiles = server.getCurrentClient().revertFiles(fileSpecs, Boolean.FALSE, -1, Boolean.FALSE, Boolean.FALSE);
			return new UnEditScmResult(P4JavaPerforceRepository.NO_COMMAND_LINE, p4Repo.transformFileSpec(revertedFiles));
		} catch (IOException e) {
			throw new ScmException("Error obtaining local path for file set.", e);
		} catch (P4JavaException e) {
			if (getLogger().isDebugEnabled() && fileSpecs != null) {
				getLogger().debug("Error occured while reverting files: ");
				for (IFileSpec fileSpec : fileSpecs) {
					getLogger().debug("File: " + fileSpec.getDepotPathString() + " --> " + fileSpec.getClientPathString());
				}
			}
			throw new ScmException(errorMessage, e);
		} catch (URISyntaxException e) {
			throw new ScmException(errorMessage, e);
		}
	}

}
