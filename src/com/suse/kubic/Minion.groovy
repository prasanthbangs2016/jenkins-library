package com.suse.kubic;

class Minion implements Serializable {
	String index;
	String fqdn;
	String role;
	String minionId;
	String proxyCommand;
	Addresses addresses = new Addresses();
}
