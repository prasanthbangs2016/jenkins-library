package com.suse.kubic;

// A Kubic Environment
class Environment implements Serializable {
	String dashboardHost;
	String kubernetesHost;
	String kubeconfigFile;

	List<Minion> minions = new ArrayList<>();
}
