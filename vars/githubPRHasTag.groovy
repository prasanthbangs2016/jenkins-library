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

// check if the PR has a tag as "/something" in the description
// always false if this is not being run in a PR
def call(Map parameters = [:]) {
    String org = parameters.get('org')
    String repo = parameters.get('repo')
    String tag = parameters.get('tag')

    // we do not have CHANGE_ID on !PRs, so just return false
    def num = parameters.get('num', env.CHANGE_ID)
    if (num == null) {
        return false
    }

    def prInfo = githubGetPRInfo(org: org, repo: repo, num: num)
    return prInfo['body'].toLowerCase().contains("/${tag}".toLowerCase())
}
