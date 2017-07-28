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

Environment call(Map parameters = [:]) {
    String type = parameters.get('type', 'devenv')
    int masterCount = parameters.get('masterCount')
    int workerCount = parameters.get('workerCount')

    switch (type) {
        case 'devenv':
            return createEnvironmentDevenv(masterCount: masterCount, workerCount: workerCount)
        default:
            error("Unknown environment type: ${type}")
    }
}
