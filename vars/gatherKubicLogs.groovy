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

def call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')

    echo "Gathering Kubic Logs"

    // Use returnStatus: true, and don't bother checking the results, so log gathering always "works".
    // Ideally we would wait until the end, and fail this step if any of the returned statues was non-zero.

    // "Short" logs you care about most should be piped through tee, while long or rarely used logs should
    // go straight to a file.

    timeout(10) {
        parallel 'admin': {
            // TODO: Move these somewhere else outside of the job config?
            sh(script: 'set -o pipefail; docker ps -a 2>&1 | tee ${WORKSPACE}/logs/docker-ps.log', returnStatus: true)
            
            dir("${WORKSPACE}/logs/admin-docker-inspect") {
                sh(script: 'set -o pipefail; for i in $(docker ps -a -q); do NAME=$(docker ps -a --format "table {{.ID}} {{.Names}}" | grep $i | awk "{print \\$2}"); docker inspect $i &> $NAME.log; done', returnStatus: true)
            }

            dir("${WORKSPACE}/logs/admin-docker-logs") {
                sh(script: 'set -o pipefail; for i in $(docker ps -a -q); do NAME=$(docker ps -a --format "table {{.ID}} {{.Names}}" | grep $i | awk "{print \\$2}"); docker logs $i &> $NAME.log; done', returnStatus: true)
            }

            dir("${WORKSPACE}/logs/manifests") {
                sh(script: 'cp ${WORKSPACE}/caasp-devenv/manifests/* .', returnStatus: true)
            }

            sh(script: 'set -o pipefail; docker exec -i $(docker ps | grep velum-dashboard | awk \'{print $1}\') entrypoint.sh bundle exec rails runner \'puts(Pillar.all.to_yaml)\' &> ${WORKSPACE}/logs/velum-pillar.yaml', returnStatus: true)
            sh(script: 'set -o pipefail; docker exec -i $(docker ps | grep velum-dashboard | awk \'{print $1}\') entrypoint.sh bundle exec rails runner \'puts(Minion.all.to_yaml)\' &> ${WORKSPACE}/logs/velum-minions.yaml', returnStatus: true)
            sh(script: 'set -o pipefail; docker exec -i $(docker ps | grep velum-dashboard | awk \'{print $1}\') entrypoint.sh bundle exec rails runner \'puts(SaltEvent.all.map(&:parsed_data).to_yaml)\' &> ${WORKSPACE}/logs/velum-salt-events.yaml', returnStatus: true)
        },
        'master': {
            // TODO.. We should do all minions in parallel, we can generate that, but no
            // need to do it right this second. Produces /var/log/nts_*.tbz.
            environment.minions.each { minion ->
                if (minion.role == "master") {
                    shOnMinion(minion: minion, script: "supportconfig -b")
                    scpFromMinion(minion: minion, source: "/var/log/nts_*.tbz", destination: "${WORKSPACE}/logs/")
                }
            }
        },
        'worker': {
            // TODO.. We should do all minions in parallel, we can generate that, but no
            // need to do it right this second. Produces /var/log/nts_*.tbz.
            environment.minions.each { minion ->
                if (minion.role == "worker") {
                    shOnMinion(minion: minion, script: "supportconfig -b")
                    scpFromMinion(minion: minion, source: "/var/log/nts_*.tbz", destination: "${WORKSPACE}/logs/")
                }
            }
        },
        'cluster': {
            // TODO.. Connect to cluster and gather any logs
        }
    }
}
