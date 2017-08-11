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
    int masterCount = parameters.get('masterCount')
    int workerCount = parameters.get('workerCount')

    int minionCount = masterCount + workerCount;

    if (masterCount != 1) {
        error('Multiple masters are not supported on a devenv environment')
    }

    timeout(60) {
        parallel 'caasp-devenv': {
            sh(script: 'killall -9 kubelet || :')

            dir('caasp-devenv') {
                sh(script: 'set -o pipefail; ./cleanup --non-interactive 2>&1 | tee ${WORKSPACE}/logs/caasp-devenv-cleanup.log')
            }

            sh(script: 'docker kill $(docker ps -a -q) 2>&1 | tee ${WORKSPACE}/logs/docker-cleanup.log')
            sh(script: 'docker rm $(docker ps -a -q) 2>&1 | tee -a ${WORKSPACE}/logs/docker-cleanup.log')
            sh(script: 'docker rmi --no-prune sles12/velum:0.0 2>&1 | tee -a ${WORKSPACE}/logs/docker-cleanup.log')
            sh(script: 'docker rmi --no-prune sles12/velum:development 2>&1 | tee -a ${WORKSPACE}/logs/docker-cleanup.log')
            sh(script: 'docker rmi --no-prune $(docker images -q) 2>&1 | tee -a ${WORKSPACE}/logs/docker-cleanup.log')
        },
        'terraform': {
            dir('terraform') {
                withEnv([
                    "MINIONS_SIZE=${minionCount}",
                    "SKIP_DASHBOARD=true",
                    "PREFIX=jenkins",
                    "FORCE=true",
                    "WGET_FLAGS=--no-verbose",
                    "NO_COLOR=true",
                    "STAGING=devel",
                ]) {
                    ansiColor('xterm') {
                        sh(script: 'set -o pipefail; ./contrib/libvirt/k8s-libvirt.sh destroy 2>&1 | tee ${WORKSPACE}/logs/terraform-destroy.log')
                    }
                }
            }
        }
    }
}
