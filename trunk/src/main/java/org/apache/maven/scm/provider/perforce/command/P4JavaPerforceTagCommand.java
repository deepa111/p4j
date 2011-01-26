package org.apache.maven.scm.provider.perforce.command;

import java.io.IOException;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.P4JavaPerforceRepository;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.ILabel;
import com.perforce.p4java.core.ILabelMapping;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.Label;
import com.perforce.p4java.impl.generic.core.Label.LabelMapping;
import com.perforce.p4java.server.IServer;

public class P4JavaPerforceTagCommand extends AbstractTagCommand {

	@SuppressWarnings("unchecked")
	@Override
	protected ScmResult executeTagCommand(ScmProviderRepository repository, ScmFileSet fileSet, String tagName, ScmTagParameters scmTagParameters) throws ScmException {
		P4JavaPerforceRepository p4Repo = (P4JavaPerforceRepository) repository;

		try {
			IServer server = p4Repo.getPerforceServer();
			IClient client = p4Repo.getWorkspace(server, fileSet.getBasedir());
			server.setCurrentClient(client);
			ILabel label = new Label();
			label.setServer(server);
			label.setLocked(Boolean.FALSE);
			label.setOwnerName(repository.getUser());
			label.setName(tagName);
			label.setDescription(scmTagParameters.getMessage());

			ViewMap<ILabelMapping> viewMapping = new ViewMap<ILabelMapping>();
			ILabelMapping labelMapping = new LabelMapping(LabelMapping.ORDER_UNKNOWN, client.getClientView().getEntry(0).getDepotSpec());
			viewMapping.addEntry(labelMapping);
			label.setViewMapping(viewMapping);

			server.createLabel(label);
			List<IFileSpec> labelSyncedFiles = server.getCurrentClient().labelSync(p4Repo.transformFiles(fileSet.getFileList()), tagName, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);

			if (shouldLock()) {
				label = server.getLabel(tagName);
				label.setLocked(Boolean.TRUE);
				label.update();
			}

			return new TagScmResult(P4JavaPerforceRepository.NO_COMMAND_LINE, labelSyncedFiles);
		} catch (IOException e) {
			throw new ScmException("Error obtaining local path for file set.", e);
		} catch (Exception e) {
			throw new ScmException("Could not get a list of opened files from Perforce.", e);
		}
	}

	private Boolean shouldLock() {
		return Boolean.valueOf(System.getProperty("maven.scm.locktag", "true"));
	}
}
