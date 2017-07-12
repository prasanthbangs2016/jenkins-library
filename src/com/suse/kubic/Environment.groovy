package com.suse.kubic;

// A Kubic Environment
class Environment implements Serializable {
	String dashboardHost;
	String kubernetesHost;

	String sshUser;
	String sshKey;

	List<Minion> minions = new ArrayList<>();
}
