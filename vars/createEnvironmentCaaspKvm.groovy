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
import com.suse.kubic.Minion
import groovy.json.JsonBuilder

Environment call(Map parameters = [:]) {
    int masterCount = parameters.get('masterCount')
    int workerCount = parameters.get('workerCount')

    Environment environment = new Environment()

    timeout(60) {
        dir('automation/caasp-kvm') {
            sh(script: "set -o pipefail; ./caasp-kvm --build -m ${masterCount} -w ${workerCount} 2>&1 | tee ${WORKSPACE}/logs/caasp-kvm-build.log")

            // Read the generated environment file
            def environmentJson = readJSON(file: 'environment.json')

            // Fill out the dashboardHost and kubernetesHost
            environment.dashboardHost = environmentJson.dashboardHost
            environment.kubernetesHost = environmentJson.kubernetesHost
            environment.sshUser = environmentJson.sshUser
            environment.sshKey = environmentJson.sshKey

            environmentJson.minions.each { tfMinion ->
                Minion minion = new Minion()

                minion.index = tfMinion.index
                minion.fqdn = tfMinion.fqdn
                minion.role = tfMinion.role
                minion.minionId = tfMinion.minionId
                minion.proxyCommand = tfMinion.proxyCommand
                minion.addresses.publicIpv4 = tfMinion.addresses.publicIpv4
                minion.addresses.privateIpv4 = tfMinion.addresses.privateIpv4

                environment.minions.push(minion)
            }

            sh(script: "cp environment.json ${WORKSPACE}/environment.json")
            sh(script: "cat ${WORKSPACE}/environment.json")
        }

        archiveArtifacts(artifacts: 'environment.json', fingerprint: true)
    }

    return environment
}
