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
    int minionCount = parameters.get('minionCount')

    echo "Starting Kubic core project periodic"

    // TODO: Make this an OpenStack based deploy with 50+ nodes.
    withKubicEnvironment(
            nodeLabel: 'leap42.2&&m1.xlarge',
            gitBase: 'https://github.com/kubic-project',
            gitBranch: env.getEnvironment().get('CHANGE_TARGET', env.BRANCH_NAME),
            gitCredentialsId: 'github-token',
            minionCount: minionCount) {

        stage('Run Basic Tests') {
            // TODO: Add some cluster tests, e.g. booting pods, checking they work, etc
            parallel 'testinfra': {
                runRestInfra(environment: environment)
            }
        }

        stage('Run Kube e2e-tests') {
            // TODO... 
        }
    }
}
