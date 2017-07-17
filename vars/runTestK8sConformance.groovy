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

    // generate an admin certificate
    // TODO: we should skip all these certs and use a kubeconfig generated from Velum

    String ca_crt="${WORKSPACE}/ca.crt"
    String ca_key="${WORKSPACE}/ca.key"
    String admin_crt="${WORKSPACE}/admin.crt"
    String admin_csr="${WORKSPACE}/admin.csr"
    String admin_key="${WORKSPACE}/admin.key"

    dir("${WORKSPACE}/automation/k8s-e2e-tests") {
        // we need the ca.{crt,key} from the salt-minion-ca container
        cpFromDockerContainer("salt-minion-ca", "/etc/pki/ca.crt", ca_crt)
        cpFromDockerContainer("salt-minion-ca", "/etc/pki/private/ca.key", ca_key)

        sh """
           openssl genrsa -out ${admin_key} 2048
           openssl req -new -key ${admin_key} -out ${admin_csr} -subj '/CN=kube-admin'
           openssl x509 -req -in ${admin_csr} -CA ${ca_crt} -CAkey ${ca_key} -CAcreateserial -out ${admin_crt} -days 365
           """

        try {
            timeout(2 * 60 * 60) {
                withEnv(["DOCKER_RUN_ARGS=-i",]) {
                    ansiColor {
                        sh "./e2e-tests --url https://${environment.kubernetesHost}:6443/ --ca-crt ${ca_crt} --admin-key ${admin_key} --admin-crt ${admin_crt} --artifacts report --log ${WORKSPACE}/logs/e2e-tests.log"
                    }
                }
            }
        } finally {
            junit("report/*.xml")
        }
    }
}
