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


Environment call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')

    // TODO: This and configureEnvironment share 90% of the same code

    timeout(90) {
        dir('automation/velum-bootstrap') {
            sh(script: './velum-interactions --setup')
        }
    }

    timeout(90) {
        try {
            dir('automation/velum-bootstrap') {
                withEnv([
                    "ENVIRONMENT=${WORKSPACE}/environment.json",
                ]) {
                    sh(script: "./velum-interactions --bootstrap")
                    sh(script: "cp kubeconfig ${WORKSPACE}/kubeconfig")
                }
            }
            echo "Fetch caasp-cli from master and set up Kubernetes login"
            sh(script: "cp ${WORKSPACE}/kubeconfig ${WORKSPACE}/logs/kubeconfig.velum_orig")
            sh(script: "mkdir -p ${WORKSPACE}/tmp")
            sleep 300
            scpFromMinion(minion: environment.minions[0], source: "/usr/bin/caasp-cli", destination: "${WORKSPACE}/tmp/")
            sh(script: "${WORKSPACE}/tmp/caasp-cli login --kubeconfig ${WORKSPACE}/kubeconfig --server https://${environment.kubernetesHost}:6443 -p password -u test@test.com")

        } finally {
            dir('automation/velum-bootstrap') {
                junit "velum-bootstrap.xml"
                try {
                    archiveArtifacts(artifacts: "screenshots/**")
                    archiveArtifacts(artifacts: "kubeconfig")
                } catch (Exception exc) {
                    echo "Failed to Archive Artifacts"
                }
            }
        }
    }
}
