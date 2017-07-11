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
// Clones all Kubic Repos.
def call(Map parameters = [:]) {
    def gitBase = parameters.get('gitBase')
    def branch = parameters.get('branch')
    def credentialsId = parameters.get('credentialsId')

    echo 'Cloning all Kubic Repos'

    timeout(5) {
        parallel 'caasp-devenv': {
            cloneKubicRepo(gitBase: gitBase, branch: branch, credentialsId: credentialsId, repo: "caasp-devenv")
        },
        'salt': {
            cloneKubicRepo(gitBase: gitBase, branch: branch, credentialsId: credentialsId, repo: "salt")
        },
        'velum': {
            cloneKubicRepo(gitBase: gitBase, branch: branch, credentialsId: credentialsId, repo: "velum")
        },
        'caasp-container-manifests': {
            cloneKubicRepo(gitBase: gitBase, branch: branch, credentialsId: credentialsId, repo: "caasp-container-manifests")
        },
        'terraform': {
            cloneKubicRepo(gitBase: gitBase, branch: branch, credentialsId: credentialsId, repo: "terraform")
        },
        'testinfra': {
            cloneKubicRepo(gitBase: gitBase, branch: branch, credentialsId: credentialsId, repo: "testinfra")
        },
        'automation': {
            cloneKubicRepo(gitBase: gitBase, branch: branch, credentialsId: credentialsId, repo: "automation")
        }
    }
}
