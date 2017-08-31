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

Environment call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')
    String type = parameters.get('type', 'caasp-kvm')
    String openstackImage = parameters.get('openstackImage')
    int workerCount = parameters.get('workerCount')

    switch (type) {
        case 'caasp-kvm':
            echo "Secondary worker creation step unnecessary"
            return environment
        case 'openstack':
            echo "Secondary worker creation step unnecessary"
            return environment
        default:
            error("Unknown environment type: ${type}")
    }
}
