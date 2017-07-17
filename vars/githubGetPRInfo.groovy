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

import groovy.json.JsonSlurper

def call(Map parameters = [:]) {
    String org = parameters.get('org')
    String repo = parameters.get('repo')
    String num = parameters.get('num', env.CHANGE_ID)

    echo "Getting PR-${num} details from GitHub"

    def response = httpRequest(url: "https://api.github.com/repos/${org}/${repo}/pulls/${num}")
    // TODO: process the response.status
    def slurper = new groovy.json.JsonSlurper()
    return slurper.parseText(response.content)
}
