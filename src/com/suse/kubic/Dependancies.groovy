package com.suse.kubic;
// @GrabResolver(name='http-builder', m2Compatible='true', root='http://repo1.maven.org/maven2/')
// @Grapes([
//     @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' ),
//     @GrabConfig(systemClassLoader=true)
// ])
// import groovyx.net.http.HTTPBuilder

class Dependancies implements Serializable {
		def dependancies = [:]

		def getDependancies(String org, String repo, int num, String user, String password) {
			
			def authString = "${user}:${password}".getBytes().encodeBase64().toString()
			def conn = "https://api.github.com/repos/${org}/${repo}/pulls/${num}".toURL().openConnection()
			conn.setRequestProperty( "Authorization", "Basic ${authString}" )
			def response = ''
			if( conn.responseCode == 200 ) {
				response = conn.content.text

			} else {
				println "Something bad happened."
			 	println "${conn.responseCode}: ${conn.responseMessage}" 
			}
			def slurper = new groovy.json.JsonSlurper()
		    def content = slurper.parseText(response)
		    def prMessage = content['body']
			def contentRegex = /Depends-On: (?:https:\/\/github.com\/|)(.*)\/(.*)(?:\/pull\/|#)(\d*)/
			def contentMatcher = ( prMessage =~ contentRegex )
			for (match in contentMatcher) {
				// if (this.dependancies.get(org, [:]).get(repo, false).asBoolean()) {
				// 	this.dependancies[org].remove([repo])
				// 	continue
				// }
				this.addDependancy(match[1], match[2], match[3].toInteger())
				getDependancies(match[1], match[2], match[3].toInteger(), user, password)
			}
		}

		def addDependancy(String org, String repo, int num){

			if (this.dependancies[org] == null) {
				this.dependancies[org] = [:]
			}

			if (this.dependancies[org][repo] == null || this.dependancies[org][repo] == num) {
				this.dependancies[org][repo] = num
			} else {
				throw new Exception("Repos cannot have 2 dependant PRs")
			}
		}
}
