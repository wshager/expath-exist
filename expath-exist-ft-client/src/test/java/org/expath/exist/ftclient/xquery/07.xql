xquery version "3.0";

import module "http://expath.org/ns/ft-client";

let $connection := ft-client:connect(xs:anyURI('ftp://ftp-user:ftp-pass@127.0.0.1'))
let $expected-result := <expected-result/>
let $file-to-store := util:binary-to-string(util:binary-doc(concat('xmldb:', resolve-uri('../resources/test.txt', concat(substring-after(system:get-module-load-path(), 'xmldb:'), '/')))))
let $actual-result := 
	<actual-result>
		{
		ft-client:store-resource($connection, concat("/dir-with-rights/tmp/test", util:uuid(), ".txt"), $file-to-store)
		}
	</actual-result>
let $close-connection := ft-client:disconnect($connection)		
	

return
	<result>
		{
		(
		$actual-result,
		if ($actual-result/text() = 'true')
			then <result-token>passed</result-token>
			else <result-token>failed</result-token>
		)
		}
	</result>