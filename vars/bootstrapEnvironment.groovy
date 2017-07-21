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

    timeout(90) {
        dir('automation/velum-bootstrap') {
            sh(script: 'bundle config build.nokogiri --use-system-libraries; bundle install')
        }
    }

    timeout(90) {
        try {
            dir('automation/velum-bootstrap') {
                withEnv([
                    "VERBOSE=true",
                    "ENVIRONMENT=${WORKSPACE}/terraform/environment.json",
                    // TODO: drop this after switching to a VM based admin setup
                    "DEVENV=true",
                ]) {
                    sh(script: "bundle exec rspec --format RspecJunitFormatter --out ${WORKSPACE}/velum-bootstrap.xml spec/**/*")
                }
            }
        } finally {
            junit "velum-bootstrap.xml"
            try {
                archiveArtifacts(artifacts: '*.png', fingerprint: true)
            } catch (Exception exc) {
                echo "Failed to Archive Screenshots"
            }
        }
    }
}
