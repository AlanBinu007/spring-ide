package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.ProjectDeploymentTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;

public class DockerRunTarget extends AbstractRunTarget<DockerTargetParams> 
implements RemoteRunTarget<DockerClient, DockerTargetParams>, ProjectDeploymentTarget {

	private static final String[] NO_STRINGS = new String[0];
	private static final String DEPLOYMENTS = "deployments";
	
	LiveVariable<DockerClient> client = new LiveVariable<>();
	private DockerTargetParams params;
	
	LiveSetVariable<String> deployedProjects = new LiveSetVariable<>();
	private List<Disposable> disposables = new ArrayList<>();
	private final DockerDeployer deployer;
	
	public DockerRunTarget(DockerRunTargetType type, DockerTargetParams params, DockerClient client) {
		super(type, params.getUri());
		this.params = params;
		this.client.setValue(client);
		try {
			String[] restoredDeployments = getPersistentProperties().get(DEPLOYMENTS, NO_STRINGS);
			if (restoredDeployments!=null) {
				deployedProjects.replaceAll(ImmutableList.copyOf(restoredDeployments));
			}
			disposables.add(deployedProjects.onChange((_e, v) -> {
				try {
					getPersistentProperties().put(DEPLOYMENTS, deployedProjects.getValues().toArray(NO_STRINGS));
				} catch (Exception e) {
					Log.log(e);
				}
			}));

		} catch (Exception e) {
			Log.log(e);
		}
		this.deployer = new DockerDeployer(this, deployedProjects, this.client);
	}

	public SimpleDIContext injections() {
		return getType().injections();
	}
	
	@Override
	public DockerRunTargetType getType() {
		return (DockerRunTargetType) super.getType();
	}

	@Override
	public RemoteBootDashModel createSectionModel(BootDashViewModel parent) {
		return new GenericRemoteBootDashModel<>(this, parent);
	}
	
	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return false;
	}

	@Override
	public DockerTargetParams getParams() {
		return params;
	}

	@Override
	public void dispose() {
		for (Disposable d : disposables) {
			d.dispose();
		}
		disposables.clear();
	}

	@Override
	public LiveExpression<DockerClient> getClientExp() {
		return client;
	}

	@Override
	public Collection<App> fetchApps() throws Exception {
		return deployer.getApps();
	}

	@Override
	public synchronized void disconnect() {
		DockerClient c = client.getValue();
		if (c!=null) {
			client.setValue(null);
			c.close();
		}
	}

	@Override
	public synchronized void connect(ConnectMode mode) throws Exception {
		if (!isConnected()) {
			this.client.setValue(DefaultDockerClient.builder().uri(params.getUri()).build());
		}
	}

	@Override
	public void performDeployment(Set<IProject> projects, RunState runOrDebug) throws Exception {
		for (IProject p : projects) {
			deployedProjects.add(p.getName());
		}
	}
}