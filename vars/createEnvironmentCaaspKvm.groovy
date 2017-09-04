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
    int masterCount = parameters.get('masterCount')
    int workerCount = parameters.get('workerCount')

    Environment environment

    timeout(60) {
        dir('automation/caasp-kvm') {
            sh(script: "set -o pipefail; ./caasp-kvm --build -m ${masterCount} -w ${workerCount} 2>&1 | tee ${WORKSPACE}/logs/caasp-kvm-build.log")

            // Read the generated environment file
            environment = new Environment(readJSON(file: 'environment.json'))

            sh(script: "cp environment.json ${WORKSPACE}/environment.json")
            sh(script: "cat ${WORKSPACE}/environment.json")
        }

        archiveArtifacts(artifacts: 'environment.json', fingerprint: true)
    }

    return environment
}
