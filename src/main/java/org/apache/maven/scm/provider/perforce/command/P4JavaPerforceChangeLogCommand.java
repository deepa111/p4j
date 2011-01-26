package org.apache.maven.scm.provider.perforce.command;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.P4JavaPerforceRepository;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelist.Type;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;

public class P4JavaPerforceChangeLogCommand extends AbstractChangeLogCommand {

	@Override
	protected ChangeLogScmResult executeChangeLogCommand(ScmProviderRepository repository, ScmFileSet fileSet, Date startDate, Date endDate, ScmBranch branch, String datePattern) throws ScmException {
		P4JavaPerforceRepository p4Repo = (P4JavaPerforceRepository) repository;
		String message = "Could not get a list of opened files from Perforce.";

		try {
			IServer server = p4Repo.getPerforceServer();
			IClient client = p4Repo.getWorkspace(server, fileSet.getBasedir());
			server.setCurrentClient(client);
			
			List<IFileSpec> files = FileSpecBuilder.makeFileSpecList(client.getClientView().getEntryList().iterator().next().getDepotSpec());

			List<IChangelistSummary> recentlySubmitted = server.getChangelists(100, files, null, null, Boolean.TRUE, Type.SUBMITTED, Boolean.FALSE);
			List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
			for (IChangelistSummary changelistSummary : recentlySubmitted) {
				ChangeSet changeSet = new ChangeSet();
				changeSet.setAuthor(changelistSummary.getUsername());
				changeSet.setComment(changelistSummary.getDescription());
				changeSet.setRevision(Integer.toString(changelistSummary.getId()));
				changeSet.setDate(changelistSummary.getDate());
				List<ChangeFile> changeFiles = new ArrayList<ChangeFile>();
				IChangelist changelist = server.getChangelist(changelistSummary.getId());
				for (IFileSpec fileSpec : changelist.getFiles(Boolean.TRUE)) {
					ChangeFile changeFile = new ChangeFile(fileSpec.getDepotPathString(), Integer.toString(fileSpec.getWorkRev()));
					changeFiles.add(changeFile);
				}
				changeSet.setFiles(changeFiles);
				changeSets.add(changeSet);
			}

			ChangeLogSet cls = new ChangeLogSet(changeSets, null, null);
			cls.setStartVersion(null);
			cls.setEndVersion(null);
			return new ChangeLogScmResult(P4JavaPerforceRepository.NO_COMMAND_LINE, cls);
		} catch (P4JavaException e) {
			throw new ScmException(message, e);
		} catch (URISyntaxException e) {
			throw new ScmException(message, e);
		} catch (IOException e) {
			throw new ScmException("Error getting client workspace.", e);
		}
	}

}
