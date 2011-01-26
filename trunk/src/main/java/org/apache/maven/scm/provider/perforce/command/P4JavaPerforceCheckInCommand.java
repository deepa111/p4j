package org.apache.maven.scm.provider.perforce.command;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.P4JavaPerforceRepository;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.mapbased.server.Server;
import com.perforce.p4java.server.IServer;

public class P4JavaPerforceCheckInCommand extends AbstractCheckInCommand {

	@SuppressWarnings("unchecked")
	@Override
	protected CheckInScmResult executeCheckInCommand(ScmProviderRepository repository, ScmFileSet fileSet, String message, ScmVersion scmVersion) throws ScmException {
		IChangelist changeList = null;
		P4JavaPerforceRepository p4Repo = (P4JavaPerforceRepository) repository;
		try {
			List<IFileSpec> fileSpecs = p4Repo.transformFiles(fileSet.getFileList());
			IServer server = p4Repo.getPerforceServer();
			IClient client = p4Repo.getWorkspace(server, fileSet.getBasedir());
			server.setCurrentClient(client);
			
			changeList = server.getCurrentClient().createChangelist(
					new Changelist(IChangelist.UNKNOWN, server.getCurrentClient().getName(), server.getUserName(), ChangelistStatus.NEW, new Date(), message, Boolean.FALSE, (Server) server));

			changeList.refresh();
			server.getCurrentClient().reopenFiles(fileSpecs, changeList.getId(), null);
			fileSpecs = server.getCurrentClient().openedFiles(fileSpecs, -1, changeList.getId());
			changeList.refresh();
			changeList.update();
			client.refresh();

			List<IFileSpec> submittedFiles = changeList.submit(Boolean.FALSE);

			return new CheckInScmResult(P4JavaPerforceRepository.NO_COMMAND_LINE, p4Repo.transformFileSpec(submittedFiles));
		} catch (IOException e) {
			throw new ScmException("Error obtaining local path for file set.", e);
		} catch (Exception e) {
			throw new ScmException("Could not remove files from Perforce.", e);
		}
	}

}
