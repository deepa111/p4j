package org.apache.maven.scm.provider.perforce.command;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.edit.AbstractEditCommand;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.P4JavaPerforceRepository;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;

public class P4JavaPerforceEditCommand extends AbstractEditCommand {

	@SuppressWarnings("unchecked")
	@Override
	protected ScmResult executeEditCommand(ScmProviderRepository repository, ScmFileSet fileSet) throws ScmException {
		P4JavaPerforceRepository p4Repo = (P4JavaPerforceRepository) repository;
		String message = "Could not edit files from Perforce.";
		List<IFileSpec> fileSpecs = null;
		try {
			fileSpecs = p4Repo.transformFiles(fileSet.getFileList());
			IServer server = p4Repo.getPerforceServer();
			IClient client = p4Repo.getWorkspace(server, fileSet.getBasedir());
			client.setServer(server);
			server.setCurrentClient(client);

			fileSpecs = server.getDepotFiles(fileSpecs, false);

			List<IFileSpec> editedFiles = server.getCurrentClient().editFiles(fileSpecs, Boolean.FALSE, Boolean.FALSE, IChangelist.DEFAULT, null);

			return new EditScmResult(P4JavaPerforceRepository.NO_COMMAND_LINE, p4Repo.transformFileSpec(editedFiles));
		} catch (IOException e) {
			throw new ScmException("Error obtaining local path for file set.", e);
		} catch (P4JavaException e) {
			if (getLogger().isDebugEnabled() && fileSpecs != null) {
				getLogger().debug("Error occured while opening files for edit: ");
				for (IFileSpec fileSpec : fileSpecs) {
					getLogger().debug("File: " + fileSpec.getDepotPathString() + " --> " + fileSpec.getClientPathString());
				}
			}
			throw new ScmException(message, e);
		} catch (URISyntaxException e) {
			throw new ScmException(message, e);
		}
	}

}
