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
import com.suse.kubic.Environment

def call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')

    dir("automation/testinfra") {
        // TODO: Use a parallel for each role, and run tox once per role with the group of minions.
        withEnv([
            "SSH_CONFIG=${WORKSPACE}/automation/misc-tools/environment.ssh_config",
            "ENVIRONMENT_JSON=${WORKSPACE}/environment.json"
        ]) {
            environment.minions.each { minion ->
                try {
                    sh("set -o pipefail; tox -e ${minion.role} -- --hosts ${minion.fqdn} --junit-xml ${minion.role}-${minion.index}.xml -v | tee -a ${WORKSPACE}/logs/testinfra.log")
                } finally {
                    junit "${minion.role}-${minion.index}.xml"
                }
            }
        }
    }
}
