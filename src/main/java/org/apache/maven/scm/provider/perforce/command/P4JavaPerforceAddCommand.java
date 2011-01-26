package org.apache.maven.scm.provider.perforce.command;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.P4JavaPerforceRepository;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;

public class P4JavaPerforceAddCommand extends AbstractAddCommand {

	@SuppressWarnings("unchecked")
	@Override
	protected ScmResult executeAddCommand(ScmProviderRepository repository, ScmFileSet fileSet, String message, boolean binary) throws ScmException {
		P4JavaPerforceRepository p4Repo = (P4JavaPerforceRepository) repository;
		try {
			List<IFileSpec> fileSpecs = p4Repo.transformFiles(fileSet.getFileList());
			IServer server = p4Repo.getPerforceServer();
			IClient client = p4Repo.getWorkspace(server, fileSet.getBasedir());
			server.setCurrentClient(client);
			List<IFileSpec> addedFiles = server.getCurrentClient().addFiles(fileSpecs, Boolean.FALSE, -1, null, Boolean.FALSE);
			if(addedFiles.size() == fileSet.getFileList().size()) {
				return new AddScmResult(P4JavaPerforceRepository.NO_COMMAND_LINE, p4Repo.transformFileSpec(addedFiles));
			} else {
				return new AddScmResult(P4JavaPerforceRepository.NO_COMMAND_LINE, "Attempted to add " + fileSet.getFileList().size() + " but only added " + addedFiles.size(), P4JavaPerforceRepository.NO_COMMAND_LINE, Boolean.FALSE);
			}
			
		} catch (IOException e) {
			throw new ScmException("Error obtaining local path for file set.", e);
		} catch (P4JavaException e) {
			StringBuilder builder = new StringBuilder();
			for(File file : (List<File>) fileSet.getFileList()) {
				builder.append(file.getAbsolutePath());
				builder.append(", ");
			}
			throw new ScmException("Could not open files for add: " + builder.toString(), e);
		} catch (URISyntaxException e) {
			throw new ScmException("Unable to connect to the Perforce server.", e);
		}
	}

}
