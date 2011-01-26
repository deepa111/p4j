package org.apache.maven.scm.provider.perforce.command;

import org.apache.maven.scm.provider.perforce.util.PerforceScmTestUtils;
import org.apache.maven.scm.tck.command.checkin.CheckInCommandTckTest;

public class PerforceCheckInCommandTckTest extends CheckInCommandTckTest {

	@Override
	public String getScmUrl() throws Exception {
		return PerforceScmTestUtils.getScmUrl(getRepositoryRoot());
	}

	@Override
	public void initRepo() throws Exception {
		// TODO Auto-generated method stub

	}

}
