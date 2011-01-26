package org.apache.maven.scm.provider.perforce.util;

import java.io.File;

import org.junit.Ignore;

@Ignore
public class PerforceScmTestUtils {

	public static String getScmUrl(File repositoryRoot) {
		return "scm:perforce:user@server:port://repo/path";
	}
}
