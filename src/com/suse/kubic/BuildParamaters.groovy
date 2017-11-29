package com.suse.kubic;

// A static class holding common/global build paramaters

class BuildParamaters implements Serializable {
	static int masterCount = 3;
	static int workerCount = 2;

	static String githubOrg = 'kubic-project';

	static String gitBase = 'https://github.com/kubic-project';
	static String gitCredentialsId = 'github-token';
}
