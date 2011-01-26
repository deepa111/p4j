package org.apache.maven.scm.provider.perforce.command;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.P4JavaPerforceRepository;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;

public class P4JavaPerforceStatusCommand extends AbstractStatusCommand {

	@Override
	protected StatusScmResult executeStatusCommand(ScmProviderRepository repository, ScmFileSet fileSet) throws ScmException {
		String message = "Could not get a list of opened files from Perforce.";
		P4JavaPerforceRepository p4Repo = (P4JavaPerforceRepository) repository;
		
		try {
			IServer server = p4Repo.getPerforceServer();
			IClient client = p4Repo.getWorkspace(server, fileSet.getBasedir());
			server.setCurrentClient(client);
			List<String> paths = new ArrayList<String>();
			paths.add(fileSet.getBasedir().getCanonicalPath());
			List<IFileSpec> openedFiles = server.getCurrentClient().openedFiles(FileSpecBuilder.makeFileSpecList(paths.toArray(new String[paths.size()])), -1, -1);

			return new StatusScmResult(P4JavaPerforceRepository.NO_COMMAND_LINE, p4Repo.transformFileSpec(openedFiles));
		} catch (P4JavaException e) {
			throw new ScmException(message, e);
		} catch (URISyntaxException e) {
			throw new ScmException(message, e);
		} catch (IOException e) {
			throw new ScmException("Error obtaining local path for file set.", e);
		}
	}

}
