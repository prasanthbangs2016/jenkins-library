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

    timeout(60) {
        parallel 'caasp-devenv': {
            dir('caasp-devenv') {
                withEnv([
                    "CONTAINER_MANIFESTS_DIR=${WORKSPACE}/caasp-container-manifests/",
                    "SALT_DIR=${WORKSPACE}/salt/",
                    "VELUM_DIR=${WORKSPACE}/velum/",
                ]) {
                    sh(script: 'set -o pipefail; ./start --non-interactive 2>&1 | tee ${WORKSPACE}/logs/caasp-devenv-start.log &')
                }
            }

            timeout(15) {
                sh(script: 'echo "Waiting for Velum"; until $(curl -s -k https://127.0.0.1/ | grep -q "Log in"); do echo -n "."; sleep 3; done')
            }
        },
        'terraform': {
            dir('terraform') {
                withEnv([
                    "MINIONS_SIZE=3",
                    "SKIP_DASHBOARD=true",
                    "PREFIX=jenkins",
                    "WGET_FLAGS=--no-verbose",
                ]) {
                    sh(script: 'set -o pipefail; ./contrib/libvirt/k8s-libvirt.sh apply 2>&1 | tee ${WORKSPACE}/logs/terraform-apply.log')
                }
            }
        }
    }
}
