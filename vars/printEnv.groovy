// Copyright 2017 SUSE LINUX GmbH, Nuernberg, Germany.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

String call(Map parameters = [:]) {
	echo "BRANCH_NAME = " + env.BRANCH_NAME
	echo "BUILD_CAUSE = " + env.BUILD_CAUSE
	echo "BUILD_DISPLAY_NAME = " + env.BUILD_DISPLAY_NAME
	echo "BUILD_ID = " + env.BUILD_ID
	echo "BUILD_NUMBER = " + env.BUILD_NUMBER
	echo "BUILD_TAG = " + env.BUILD_TAG
	echo "BUILD_URL = " + env.BUILD_URL
	echo "CHANGE_AUTHOR = " + env.CHANGE_AUTHOR
	echo "CHANGE_AUTHOR_DISPLAY_NAME = " + env.CHANGE_AUTHOR_DISPLAY_NAME
	echo "CHANGE_AUTHOR_EMAIL = " + env.CHANGE_AUTHOR_EMAIL
	echo "CHANGE_ID = " + env.CHANGE_ID
	echo "CHANGE_TARGET = " + env.CHANGE_TARGET
	echo "CHANGE_TITLE = " + env.CHANGE_TITLE
	echo "CHANGE_URL = " + env.CHANGE_URL
	echo "CVS_BRANCH = " + env.CVS_BRANCH
	echo "EXECUTOR_NUMBER = " + env.EXECUTOR_NUMBER
	echo "GIT_BRANCH = " + env.GIT_BRANCH
	echo "GIT_COMMIT = " + env.GIT_COMMIT
	echo "GIT_URL = " + env.GIT_URL
	echo "HOME = " + env.HOME
	echo "HUDSON_HOME = " + env.HUDSON_HOME
	echo "JAVA_HOME = " + env.JAVA_HOME
	echo "JENKINS_HOME = " + env.JENKINS_HOME
	echo "JENKINS_URL = " + env.JENKINS_URL
	echo "JOB_BASE_NAME = " + env.JOB_BASE_NAME
	echo "JOB_NAME = " + env.JOB_NAME
	echo "JOB_URL = " + env.JOB_URL
	echo "LANG = " + env.LANG
	echo "LOGNAME = " + env.LOGNAME
	echo "NODE_LABELS = " + env.NODE_LABELS
	echo "NODE_NAME = " + env.NODE_NAME
	echo "OLDPWD = " + env.OLDPWD
	echo "PWD = " + env.PWD
	echo "SHELL = " + env.SHELL
	echo "SVN_REVISION = " + env.SVN_REVISION
	echo "TERM = " + env.TERM
	echo "USER = " + env.USER
	echo "WORKSPACE = " + env.WORKSPACE
	echo "payload = " + env.payload
	//http://stackoverflow.com/questions/38254968/how-to-get-scm-url-in-build-script-for-jenkins-multibranch-workflow-project
	echo "scm = " + scm
	//def scmUrl = scm.getUserRemoteConfigs()[0].getUrl()
}
