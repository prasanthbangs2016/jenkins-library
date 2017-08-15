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
def call(Map parameters = [:]) {
    echo "Starting Kubic core project CI"

    int masterCount = parameters.get('masterCount', 1)
    int workerCount = parameters.get('workerCount', 2)

    if (env.CHANGE_AUTHOR != null) {
        // TODO: Don't hardcode salt repo name, find the right place
        // to lookup this information dynamically.
        githubCollaboratorCheck(
            org: 'kubic-project',
            repo: 'salt',
            user: env.CHANGE_AUTHOR,
            credentialsId: 'github-token')
    }

    // Configure the job properties
    properties([
        buildDiscarder(logRotator(numToKeepStr: '15')),
        disableConcurrentBuilds(),
    ])

    withKubicEnvironment(
            nodeLabel: 'leap42.2&&m1.xxlarge',
            environmentType: 'caasp-kvm',
            gitBase: 'https://github.com/kubic-project',
            gitBranch: env.getEnvironment().get('CHANGE_TARGET', env.BRANCH_NAME),
            gitCredentialsId: 'github-token',
            masterCount: masterCount,
            workerCount: workerCount) {

        stage('Run Tests') {
            // TODO: Add some cluster tests, e.g. booting pods, checking they work, etc
            runTestInfra(environment: environment)
        }
    }
}
