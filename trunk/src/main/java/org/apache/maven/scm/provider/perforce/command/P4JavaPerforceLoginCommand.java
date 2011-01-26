package org.apache.maven.scm.provider.perforce.command;

import java.net.URISyntaxException;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.login.AbstractLoginCommand;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.P4JavaPerforceRepository;

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.ServerStatus;

public class P4JavaPerforceLoginCommand extends AbstractLoginCommand {

	@Override
	public LoginScmResult executeLoginCommand(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
		P4JavaPerforceRepository p4Repo = (P4JavaPerforceRepository) repository;
		Boolean success = Boolean.FALSE;
		try {
			IServer server = p4Repo.getPerforceServer();
			if (server.getStatus().equals(ServerStatus.READY)) {
				success = Boolean.TRUE;
			}
		} catch (P4JavaException e) {
			throw new ScmException("Could not connect to Perforce as user: " + repository.getUser(), e);
		} catch (URISyntaxException e) {
			throw new ScmException("Unable to connect to Perforce, invalid SCM url.", e);
		}
		String message;
		if (success) {
			message = "Successfully logged in as: " + repository.getUser();
		} else {
			message = "Unable to login as: " + repository.getUser();
		}
		return new LoginScmResult(P4JavaPerforceRepository.NO_COMMAND_LINE, message, P4JavaPerforceRepository.NO_COMMAND_LINE, success);
	}

}
