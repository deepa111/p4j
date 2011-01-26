package org.apache.maven.scm.provider.perforce.command;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.P4JavaPerforceRepository;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.server.IServer;

public class P4JavaPerforceCheckOutCommand extends AbstractCheckOutCommand {

	@Override
	protected CheckOutScmResult executeCheckOutCommand(ScmProviderRepository repository, ScmFileSet fileSet, ScmVersion scmVersion, boolean recursive) throws ScmException {
		P4JavaPerforceRepository p4Repo = (P4JavaPerforceRepository) repository;

		Boolean forceSync = Boolean.FALSE;
		File[] files = fileSet.getBasedir().listFiles();
		if (files == null || files.length == 0) {
			forceSync = Boolean.TRUE;
		}

		try {
			IServer server = p4Repo.getPerforceServer();
			IClient client = p4Repo.getWorkspace(server, fileSet.getBasedir());

			client.setServer(server);
			server.setCurrentClient(client);

			String path = p4Repo.getPath() + "...";
			if (scmVersion != null) {
				path += "@" + scmVersion.getName();
			}
			List<IFileSpec> fileSpecs = FileSpecBuilder.makeFileSpecList(path);
			fileSpecs = server.getDepotFiles(fileSpecs, Boolean.FALSE);
			fileSpecs = FileSpecBuilder.getValidFileSpecs(fileSpecs);
			List<IFileSpec> syncedFiles = server.getCurrentClient().sync(fileSpecs, forceSync, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);

			if (getLogger().isDebugEnabled()) {
				getLogger().debug("Checked out " + syncedFiles.size() + " for workspace: " + client.getName());
			}
			
			return new CheckOutScmResult(P4JavaPerforceRepository.NO_COMMAND_LINE, p4Repo.transformFileSpec(syncedFiles));
		} catch (IOException e) {
			throw new ScmException("Error getting client workspace.", e);
		} catch (Exception e) {
			throw new ScmException("Could not remove files from Perforce.", e);
		}
	}

}
