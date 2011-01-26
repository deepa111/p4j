package org.apache.maven.scm.provider.perforce;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.command.P4JavaPerforceAddCommand;
import org.apache.maven.scm.provider.perforce.command.P4JavaPerforceChangeLogCommand;
import org.apache.maven.scm.provider.perforce.command.P4JavaPerforceCheckInCommand;
import org.apache.maven.scm.provider.perforce.command.P4JavaPerforceCheckOutCommand;
import org.apache.maven.scm.provider.perforce.command.P4JavaPerforceEditCommand;
import org.apache.maven.scm.provider.perforce.command.P4JavaPerforceLoginCommand;
import org.apache.maven.scm.provider.perforce.command.P4JavaPerforceRemoveCommand;
import org.apache.maven.scm.provider.perforce.command.P4JavaPerforceStatusCommand;
import org.apache.maven.scm.provider.perforce.command.P4JavaPerforceTagCommand;
import org.apache.maven.scm.provider.perforce.command.P4JavaPerforceUnEditCommand;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider"
 *                   role-hint="perforce"
 */
public class P4JavaPerforceScmProvider extends AbstractScmProvider {
	private static final String scmType = "perforce";

	@Override
	public String getScmType() {
		return scmType;
	}

	@Override
	public ScmProviderRepository makeProviderScmRepository(String scmSpecificUrl, char delimiter) throws ScmRepositoryException {
		String path;
		int port = 0;
		String host = null;

		int i1 = scmSpecificUrl.indexOf(delimiter);
		int i2 = scmSpecificUrl.indexOf(delimiter, i1 + 1);

		if (i1 > 0) {
			int lastDelimiter = scmSpecificUrl.lastIndexOf(delimiter);
			path = scmSpecificUrl.substring(lastDelimiter + 1);
			host = scmSpecificUrl.substring(0, i1);

			// If there is tree parts in the scm url, the second is the port
			if (i2 >= 0) {
				try {
					String tmp = scmSpecificUrl.substring(i1 + 1, lastDelimiter);
					port = Integer.parseInt(tmp);
				} catch (NumberFormatException ex) {
					throw new ScmRepositoryException("The port has to be a number.");
				}
			}
		} else {
			path = scmSpecificUrl;
		}

		String user = null;
		String password = null;
		if (host != null && host.indexOf("@") > 1) {
			user = host.substring(0, host.indexOf("@"));
			host = host.substring(host.indexOf("@") + 1);
		}

		if (path.indexOf("@") > 1) {
			if (host != null) {
				if (getLogger().isWarnEnabled()) {
					getLogger().warn("Username as part of path is deprecated, the new format is " + "scm:perforce:[username@]host:port:path_to_repository");
				}
			}

			user = path.substring(0, path.indexOf("@"));
			path = path.substring(path.indexOf("@") + 1);
		}
		return new P4JavaPerforceRepository(host, port, path, user, password);
	}

	@Override
	protected ChangeLogScmResult changelog(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		Command changeLogCommand = new P4JavaPerforceChangeLogCommand();
		changeLogCommand.setLogger(getLogger());
		return (ChangeLogScmResult) changeLogCommand.execute(repository, fileSet, parameters);
	}

	@Override
	protected AddScmResult add(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		Command addCommand = new P4JavaPerforceAddCommand();
		addCommand.setLogger(getLogger());
		return (AddScmResult) addCommand.execute(repository, fileSet, parameters);
	}

	@Override
	protected RemoveScmResult remove(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		Command removeCommand = new P4JavaPerforceRemoveCommand();
		removeCommand.setLogger(getLogger());
		return (RemoveScmResult) removeCommand.execute(repository, fileSet, parameters);
	}

	@Override
	protected CheckInScmResult checkin(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		Command checkInCommand = new P4JavaPerforceCheckInCommand();
		checkInCommand.setLogger(getLogger());
		return (CheckInScmResult) checkInCommand.execute(repository, fileSet, parameters);
	}

	@Override
	protected CheckOutScmResult checkout(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		Command checkOutCommand = new P4JavaPerforceCheckOutCommand();
		checkOutCommand.setLogger(getLogger());
		return (CheckOutScmResult) checkOutCommand.execute(repository, fileSet, parameters);
	}

	@Override
	protected EditScmResult edit(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		Command editCommand = new P4JavaPerforceEditCommand();
		editCommand.setLogger(getLogger());
		return (EditScmResult) editCommand.execute(repository, fileSet, parameters);
	}

	@Override
	protected LoginScmResult login(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		Command loginCommand = new P4JavaPerforceLoginCommand();
		loginCommand.setLogger(getLogger());
		return (LoginScmResult) loginCommand.execute(repository, fileSet, parameters);
	}

	@Override
	protected StatusScmResult status(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		Command statusCommand = new P4JavaPerforceStatusCommand();
		statusCommand.setLogger(getLogger());
		return (StatusScmResult) statusCommand.execute(repository, fileSet, parameters);
	}

	@Override
	protected TagScmResult tag(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		Command tagCommand = new P4JavaPerforceTagCommand();
		tagCommand.setLogger(getLogger());
		return (TagScmResult) tagCommand.execute(repository, fileSet, parameters);
	}

	@Override
	protected UnEditScmResult unedit(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		Command uneditCommand = new P4JavaPerforceUnEditCommand();
		uneditCommand.setLogger(getLogger());
		return (UnEditScmResult) uneditCommand.execute(repository, fileSet, parameters);
	}

	@Override
	protected UpdateScmResult update(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		CheckOutScmResult checkOutResult = checkout(repository, fileSet, parameters);
		return new UpdateScmResult(checkOutResult.getCommandLine(), checkOutResult.getCheckedOutFiles());

	}

	@Override
	public boolean requiresEditMode() {
		return Boolean.TRUE;
	}
}