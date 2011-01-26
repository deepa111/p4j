package org.apache.maven.scm.provider.perforce;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientViewMapping;
import com.perforce.p4java.core.IMapEntry.EntryType;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.client.ClientView;
import com.perforce.p4java.impl.generic.client.ClientView.ClientViewMapping;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.ServerFactory;

public class P4JavaPerforceRepository extends ScmProviderRepositoryWithHost {
	private static final String defaultWorkspaceProperty = "maven.scm.perforce.clientspec.name";
	public static final String NO_COMMAND_LINE = "No command line available.";
	private String path;

	public P4JavaPerforceRepository(String host, int port, String path, String user, String password) {
		setHost(host);
		setPort(port);
		this.path = path;
		setUser(user);
		setPassword(password);
	}

	public String getPath() {
		return path;
	}
	
	/**
	 * Attempts to connect to the Perforce server using the connection information used to define this
	 * repository.
	 * @return The Perforce server.
	 * @throws P4JavaException When the server at the given URI is not responsive or unavailable.
	 * @throws URISyntaxException If the URI of the server is invalid.
	 */
	public IServer getPerforceServer() throws P4JavaException, URISyntaxException {
		IServer server = ServerFactory.getServer("p4java://" + getHost() + ":" + getPort(), null);
		server.connect();
		server.setUserName(getUser());
		server.login(getPassword());

		return server;
	}
	
	public IClient getWorkspace(IServer server, File workingDirectory) throws P4JavaException, IOException {
		String workspaceName = getWorkspaceName(workingDirectory);
		IClient client = server.getClient(workspaceName);
		if (client == null) {
			client = new Client(server);
			client.setName(workspaceName);
			client.setRoot(workingDirectory.getAbsolutePath());
			client.setOwnerName(getUser());
			client.setHostName(InetAddress.getLocalHost().getHostName());

			List<IClientViewMapping> viewMappings = new ArrayList<IClientViewMapping>();
			IClientViewMapping viewMapping = new ClientViewMapping();
			viewMapping.setDepotSpec(getPath() + "...");
			viewMapping.setRight("//" + workspaceName + "/...");
			viewMapping.setType(EntryType.INCLUDE);
			viewMapping.setOrder(0);

			viewMappings.add(viewMapping);
			ClientView view = new ClientView(client, viewMappings);
			client.setClientView(view);
			server.createClient(client);

			client = server.getClient(workspaceName);
		}

		return client;
	}
	
	private String getWorkspaceName(File workDir) {
		String def = getDefaultWorkspaceName(workDir);
		String l = System.getProperty(defaultWorkspaceProperty, def);
		if (l == null || "".equals(l.trim())) {
			return def;
		}
		return l;
	}

	private String getDefaultWorkspaceName(File workDir) {
		String username = getUser();
		String hostname;
		String path;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
			// [SCM-370][SCM-351] client specs cannot contain forward slashes,
			// spaces and ~; "-" is okay
			path = workDir.getCanonicalPath().replaceAll("[/ ~]", "-");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return username + "-" + hostname + "-MavenSCM-" + path;
	}
	
	public List<ScmFile> transformFileSpec(List<IFileSpec> fileSpecs) {
		List<ScmFile> scmFiles = new ArrayList<ScmFile>();
		for (IFileSpec fileSpec : FileSpecBuilder.getValidFileSpecs(fileSpecs)) {
			if (fileSpec != null) {
				ScmFile scmFile = new ScmFile(fileSpec.getDepotPathString(), getFileStatus(fileSpec.getAction()));
				scmFiles.add(scmFile);
			}
		}
		return scmFiles;
	}

	public List<IFileSpec> transformFiles(List<File> files) throws IOException {
		List<IFileSpec> fileSpecs = new ArrayList<IFileSpec>();
		for (File scmFile : files) {
			fileSpecs.addAll(FileSpecBuilder.makeFileSpecList(scmFile.getCanonicalPath()));
		}
		return fileSpecs;
	}
	
	private static ScmFileStatus getFileStatus(FileAction action) {
		ScmFileStatus scmFileStatus;
		if (action == null) {
			return ScmFileStatus.UNKNOWN;
		}
		switch (action) {
		case ADD:
		case ADDED:
			scmFileStatus = ScmFileStatus.ADDED;
			break;
		case DELETE:
			scmFileStatus = ScmFileStatus.DELETED;
			break;
		case EDIT:
			scmFileStatus = ScmFileStatus.MODIFIED;
			break;
		case UPDATED:
			scmFileStatus = ScmFileStatus.UPDATED;
			break;
		default:
			scmFileStatus = ScmFileStatus.UNKNOWN;
		}
		return scmFileStatus;
	}
}
