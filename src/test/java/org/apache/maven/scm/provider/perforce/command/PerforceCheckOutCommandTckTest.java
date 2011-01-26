package org.apache.maven.scm.provider.perforce.command;

import org.apache.maven.scm.provider.perforce.util.PerforceScmTestUtils;
import org.apache.maven.scm.tck.command.checkout.CheckOutCommandTckTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerforceCheckOutCommandTckTest extends CheckOutCommandTckTest {
	private static final Logger logger = LoggerFactory.getLogger(PerforceCheckOutCommandTckTest.class);

	@Override
	public String getScmUrl() throws Exception {
		logger.info(getRepositoryRoot().getAbsolutePath());
		return PerforceScmTestUtils.getScmUrl(getRepositoryRoot());
	}

	@Override
	public void initRepo() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
