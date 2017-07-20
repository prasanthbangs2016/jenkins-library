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
import com.suse.kubic.PullRequest

def call(Map parameters = [:]) {
    String num = parameters.get('num', env.CHANGE_ID)

    if (env.CHANGE_URL == null) {
        return NULL
    }

    // we must replace some parts in order to get the corresponding URL in the API
    url = env.CHANGE_URL
    url = url.replace("/github.com/","/api.github.com/repos/")
    url = url.replace("/pull/","/pulls/")
    url = url.replace("http://","https://")

    echo "Getting PR-${num} details from GitHub from ${url}"
    PullRequest pr = new PullRequest()

    slurper = new groovy.json.JsonSlurper()

    // TODO: add authentication, so our API requests are not throttled
    response = httpRequest(url: url, validResponseCodes: "200")
    parsed = slurper.parseText(response.content)
    description = parsed.body

    def tag_regexp = /\/(\w+)\r\n/
    def tag_matches = (description =~ /$tag_regexp/)
    if (tag_matches.count > 0) {
        for (tag_match in tag_matches) {
            tag = tag_match[1]
            echo "Adding PR tag ${tag}"
            pr.tags.add(tag)
        }
    }

    return pr
}
