package com.suse.kubic;

// A static class holding common/global build paramaters. These paramaters
// can be overriden by a job by simply setting the value early in the job,
// for example:
//
//     BuildParamaters.workerCount = 10

class BuildParamaters implements Serializable {
	// Jenkins Settings
	static String nodeLabel = 'leap42.3&&m1.xxlarge';

	// Git and GitHub Settings
	static String githubOrg = 'kubic-project';
	static String gitBase = 'https://github.com/kubic-project';
	static String gitCredentialsId = 'github-token';

	// CaaSP Settings
	static int masterCount = 3;
	static int workerCount = 2;
}
