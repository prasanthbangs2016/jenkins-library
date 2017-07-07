package com.suse.kubic;

// A Kubic Environment
class Environment implements Serializable {
	String dashboardHost;
	String kubernetesHost;

	List<Minion> minions = new ArrayList<>();
}
