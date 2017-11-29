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
// Clones a single Kubic Repo.
import com.suse.kubic.BuildParamaters

def call(Map parameters = [:]) {
    def branch = parameters.get('branch')
    def repo = parameters.get('repo')
    boolean ignorePullRequest = parameters.get('ignorePullRequest', false)

    echo "Cloning Kubic Repo: ${repo}"

    timeout(5) {
        dir(repo) {
            if (!ignorePullRequest && env.JOB_NAME.contains(repo)) {
                checkout scm
            } else {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${branch}"]],
                    userRemoteConfigs: [
                        [url: "${BuildParamaters.gitBase}/${repo}.git", credentialsId: BuildParamaters.gitCredentialsId]
                    ],
                    extensions: [[$class: 'CleanCheckout']],
                ])
            }
        }
    }
}
