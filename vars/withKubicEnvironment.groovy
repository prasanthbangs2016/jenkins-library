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

def call(Map parameters = [:], Closure body) {
    def nodeLabel = parameters.get('nodeLabel', 'devel')
    def gitBase = parameters.get('gitBase')
    def gitBranch = parameters.get('gitBranch')
    def gitCredentialsId = parameters.get('gitCredentialsId')
    int minionCount = parameters.get('minionCount', 3)

    echo "Creating Kubic Environment"

    // Allocate a node
    node (nodeLabel) {
        // Show some info about the node were running on
        stage('Node Info') {
            echo "Node: ${env.NODE_NAME}"
            sh(script: 'ip a')
            def response = httpRequest(url: 'http://169.254.169.254/latest/meta-data/public-ipv4')
            echo "Public IPv4: ${response.content}"
        }

        // Basic prep steps
        stage('Preparation') {
            step([$class: 'WsCleanup'])
            sh(script: 'mkdir logs')
        }

        // Fetch the necessary code
        stage('Retrieve Code') {
            cloneAllKubicRepos(gitBase: gitBase, branch: gitBranch, credentialsId: gitCredentialsId)
        }

        Environment environment;

        try {
            // Create the Kubic environment
            stage('Create Environment') {
                environment = createEnvironment(minionCount: minionCount)
            }

            // Bootstrap the Kubic environment
            stage('Bootstrap Environment') {
                bootstrapEnvironment(environment: environment)
            }

            // Prepare the body closure delegate
            def delegate = [:]
            // Set some context variables available inside the body() method
            delegate['environment'] = environment
            body.resolveStrategy = Closure.DELEGATE_FIRST
            body.delegate = delegate

            // Execute the body of the test
            body()
        } finally {
            // Gather logs from the environment
            stage('Gather Logs') {
                try {
                    sh(script: "touch logs/dummy.log")
                } catch (Exception exc) {
                    echo "Failed to Gather Logs"
                    // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed.
                }
            }

            // Destroy the Kubic Environment
            stage('Destroy Environment') {
                try {
                    cleanupEnvironment(minionCount: minionCount)
                } catch (Exception exc) {
                    echo "Failed to Destroy Environment"
                    // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed.
                }
            }

            // Archive the logs
            stage('Archive Logs') {
                try {
                    archiveArtifacts(artifacts: 'logs/*', fingerprint: true)
                } catch (Exception exc) {
                    echo "Failed to Archive Logs"
                    // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed.
                }
            }
        }
    }
}
