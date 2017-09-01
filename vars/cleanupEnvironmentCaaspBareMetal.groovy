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
        dir('automation/caasp-bare-metal/deployer') {
            withCredentials([file(credentialsId: 'caasp-bare-metal-serverlist', variable: 'SERVERLIST_PATH'),
                    file(credentialsId: 'caasp-bare-metal-conf', variable: 'CONFFILE')]) {
                // Servers are released but not powered down
                sh(script: 'set -o pipefail; ./deployer --release ${JOB_NAME}-${BUILD_NUMBER} 2>&1 | tee ${WORKSPACE}/logs/caasp-bare-metal-destroy.log')
            }
        }
    }
}
