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
    int minionCount = parameters.get('minionCount')

    Environment environment = new Environment()

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

            timeout(30) {
                sh(script: 'echo "Waiting for Velum"; until $(curl -s -k https://127.0.0.1/ | grep -q "Log in"); do echo -n "."; sleep 3; done')
            }
        },
        'terraform': {
            dir('terraform') {
                withEnv([
                    "MINIONS_SIZE=${minionCount}",
                    "SKIP_DASHBOARD=true",
                    "PREFIX=jenkins",
                    "WGET_FLAGS=--no-verbose",
                    "NO_COLOR=true",
                ]) {
                    ansiColor('xterm') {
                        sh(script: 'set -o pipefail; ./contrib/libvirt/k8s-libvirt.sh apply 2>&1 | tee ${WORKSPACE}/logs/terraform-apply.log')
                    }
                }

                // This should all live in the TF repo, and always be rendered
                def minionJson = sh(script: """set -o pipefail; cat terraform.tfstate | jq '.modules[].resources[] | select(.type==\"libvirt_domain\")
                    | .primary | .attributes | {fqdn: .metadata | split(\",\") | .[0], ipv4: .[\"network_interface.0.addresses.0\"], id: .metadata
                    | split(\",\") | .[1] | tonumber, role: (if (.metadata | split(\",\") | .[1] | tonumber) == 0 then \"master\" else \"worker\" end)}' \
                    | jq -s . | jq '{minions: .}' | tee ${WORKSPACE}/logs/minions.json""", returnStdout: true)

                // Fill out the dashboardHost
                environment.dashboardHost = sh(script: """ip addr show \$(awk '\$2 == 00000000 { print \$1 }' /proc/net/route) \
                    | awk '\$1 == \"inet\" {print \$2}' | cut -f1 -d/""", returnStdout: true).trim()

                // Fill out the minions
                def data = readJSON(text: minionJson)

                data.minions.each { dataMinion ->
                    Minion minion = new Minion()

                    minion.id = dataMinion.id
                    minion.fqdn = dataMinion.fqdn
                    minion.role = dataMinion.role
                    minion.ipv4 = dataMinion.ipv4
                    minion.minion_id = shOnMinion(minion: minion, script: 'cat /etc/machine-id', returnStdout: true).trim()

                    if (minion.role == "master") {
                        // If we found the master, fill out the
                        // kubernetes host
                        environment.kubernetesHost = dataMinion.ipv4
                    }

                    environment.minions.push(minion)
                }

                writeFile(file: "${WORKSPACE}/logs/environment.json", text: new JsonBuilder(environment).toPrettyString())
                sh(script: "cat ${WORKSPACE}/logs/environment.json")
            }
        }
    }

    return environment
}