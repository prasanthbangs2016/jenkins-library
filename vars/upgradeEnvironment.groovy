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


Environment call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')
    String updateRepo = parameters.get('updateRepo')

    // Flow for this step:
    // On each node:
    // * Hack vendors.conf to ensure updates are seen as updates
    // * Add the updates repo
    // * Trigger transactional-update
    // On the admin node:
    // * Refresh grains
    // Via Velum:
    // * Run the upgrade steps

    Minion adminMinion;

    environment.minions.each { minion ->
        if (minion.role == "admin") {
            adminMinion = minion
        }
    }

    // TODO: These commands shouldn't exist here, an automation tool should be created to handle this and allow
    // local execution.

    // Hack vendors, add the repo
    shOnMinion(minion: adminMinion, script: "docker exec -i \$\\(docker ps | grep salt-master | awk '{print \$1}'\\) salt --batch 20 -P 'roles:(admin|kube-(master|minion))' cmd.run 'echo \"[main]\" > /etc/zypp/vendors.d/vendors.conf'")
    shOnMinion(minion: adminMinion, script: "docker exec -i \$\\(docker ps | grep salt-master | awk '{print \$1}'\\) salt --batch 20 -P 'roles:(admin|kube-(master|minion))' cmd.run 'echo \"vendors = suse,opensuse,obs://build.suse.de,obs://build.opensuse.org\" >> /etc/zypp/vendors.d/vendors.conf'")
    shOnMinion(minion: adminMinion, script: "docker exec -i \$\\(docker ps | grep salt-master | awk '{print \$1}'\\) salt --batch 20 -P 'roles:(admin|kube-(master|minion))' cmd.run 'zypper ar --refresh --no-gpgcheck ${updateRepo}'")

    // Trigger transactional-update
    shOnMinion(minion: adminMinion, script: "docker exec -i \$\\(docker ps | grep salt-master | awk '{print \$1}'\\) salt --batch 20 -P 'roles:(admin|kube-(master|minion))' cmd.run 'systemctl disable --now transactional-update.timer'")
    shOnMinion(minion: adminMinion, script: "docker exec -i \$\\(docker ps | grep salt-master | awk '{print \$1}'\\) salt --batch 15 -P 'roles:(admin|kube-(master|minion))' cmd.run '/usr/sbin/transactional-update cleanup dup salt'")

    // Refresh Grains
    shOnMinion(minion: adminMinion, script: "docker exec -i \$\\(docker ps | grep salt-master | awk '{print \$1}'\\) salt --batch 20 '*' saltutil.refresh_grains")

    // Run the Upgrade Steps
    timeout(90) {
        try {
            dir('automation/velum-bootstrap') {
                withEnv([
                    "ENVIRONMENT=${WORKSPACE}/environment.json",
                ]) {
                    sh(script: "./velum-interactions --update-admin --update-minions")
                    sh(script: "cp kubeconfig ${WORKSPACE}/kubeconfig")
                }
            }
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
