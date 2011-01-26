package org.apache.maven.scm.provider.perforce.command;

import org.apache.maven.scm.provider.perforce.util.PerforceScmTestUtils;
import org.apache.maven.scm.tck.command.tag.TagCommandTckTest;

public class PerforceTagCommandTckTest extends TagCommandTckTest {

	@Override
	public String getScmUrl() throws Exception {
		return PerforceScmTestUtils.getScmUrl(getRepositoryRoot());
	}

	@Override
	public void initRepo() throws Exception {
		// TODO Auto-generated method stub

	}

}
