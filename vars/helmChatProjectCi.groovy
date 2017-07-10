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
    def chart = parameters.get('chart')

    echo "Starting Helm Chat Project CI for ${chart}"

    withKubicEnvironment(
            nodeLabel: 'leap42.2&&m1.xlarge',
            gitBase: 'https://github.com/kubic-project',
            gitBranch: env.getEnvironment().get('CHANGE_TARGET', env.BRANCH_NAME),
            gitCredentialsId: 'github-token',
            minionCount: 3) {

        stage('Deploy Helm') {
            echo "Your kube master is @ ${environment.kubernetesHost}"
            echo "Your kubectl config is @ ${environment.kubeconfigFile} (TODO... But this will also just be avilable) "
        }

        stage('Deploy SomeChart') {
            echo "..."
        }
    }
}
