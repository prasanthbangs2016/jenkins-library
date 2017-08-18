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
        def admins = []
        def masters = []
        def workers = []

        environment.minions.each { minion ->
            if (minion.role == 'admin') {
                admins.add(minion.fqdn)
            } else if (minion.role == 'master') {
                masters.add(minion.fqdn)
            } else if (minion.role == 'worker') {
                workers.add(minion.fqdn)
            } else {
                error(message: "Unknown role: " + minion.role)
            }
        }

        withEnv([
            "SSH_CONFIG=${WORKSPACE}/automation/misc-tools/environment.ssh_config",
            "ENVIRONMENT_JSON=${WORKSPACE}/environment.json"
        ]) {
            parallel 'admin': {
                lock('venv-setup') {
                    sh("set -o pipefail; tox -e admin --notest")
                }

                try {
                    sh("set -o pipefail; tox -e admin -- --hosts ${admins.join(",")} --junit-xml admin.xml -v | tee -a ${WORKSPACE}/logs/testinfra-admin.log")
                } finally {
                    junit "admin.xml"
                }
            },
            'master': {
                lock('venv-setup') {
                    sh("set -o pipefail; tox -e admin --notest")
                }

                try {
                    sh("set -o pipefail; tox -e master -- --hosts ${masters.join(",")} --junit-xml master.xml -v | tee -a ${WORKSPACE}/logs/testinfra-master.log")
                } finally {
                    junit "master.xml"
                }
            },
            'worker': {
                lock('venv-setup') {
                    sh("set -o pipefail; tox -e admin --notest")
                }

                try {
                    sh("set -o pipefail; tox -e worker -- --hosts ${workers.join(",")} --junit-xml worker.xml -v | tee -a ${WORKSPACE}/logs/testinfra-worker.log")
                } finally {
                    junit "worker.xml"
                }
            }
        }
    }
}
