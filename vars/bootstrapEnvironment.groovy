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

    // TODO - This should use Velum / other actual APIs - not this.

    timeout(10) {
        waitUntil {
            def minions = readJSON(text:inDockerContainer(name:'salt-master', script:'salt-key --list pre --out json', returnStdout: true))

            return (minions['minions_pre'].size() == environment.minions.size())
        }
    }
    inDockerContainer(name:'salt-master', script:'salt-key --accept-all --yes')
    timeout(10) {
        waitUntil {
            def minions = readJSON(text:inDockerContainer(name:'salt-master', script:'salt-key --list accepted --out json', returnStdout: true))

            return (minions['minions'].size() == (environment.minions.size() + 2))
        }
    }

    // Give the Minions time to notice their keys have been accepted
    sleep(time: 20)

    timeout(10) {
        waitUntil {
            def minions = inDockerContainer(name:'velum-dashboard', script: "entrypoint.sh rails runner 'ActiveRecord::Base.logger=nil; puts Minion.count'", returnStdout: true).trim()

            return (minions == Integer.toString(environment.minions.size()))
        }
    }

    inDockerContainer(name:'salt-master', script: "salt -l info '*' test.ping")
    inDockerContainer(name:'salt-master', script: "salt -l info '*' saltutil.refresh_grains")
    
    inDockerContainer(name:'velum-dashboard', script:"entrypoint.sh rails runner 'ActiveRecord::Base.logger=nil; Pillar.create pillar: \"api:server:external_fqdn\", value: \"${environment.kubernetesHost}\"'")
    inDockerContainer(name:'velum-dashboard', script:"entrypoint.sh rails runner 'ActiveRecord::Base.logger=nil; Pillar.create pillar: \"dashboard\", value: \"${environment.dashboardHost}\"'")

    environment.minions.each { minion ->
        if (minion.role == 'master') { 
            inDockerContainer(name:'salt-master', script:"salt '${minion.minion_id}' grains.setval roles \"['kube-master']\"")
        } else {
            inDockerContainer(name:'salt-master', script:"salt '${minion.minion_id}' grains.setval roles \"['kube-minion']\"")
        }
    }

    timeout(60) {
        inDockerContainer(name:'salt-master', script:'salt-run -l info state.orch orch.kubernetes')
    }
}