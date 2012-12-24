xquery version "3.0";

import module "http://expath.org/ns/ft-client";

let $script-collection := concat(replace(replace(request:get-effective-uri(), "tests/(\w)+.xql$", ""), "/rest//db", ""), 'data/')
, $connection := ft-client:connect(xs:anyURI('ftp://ftp-user:ftp-pass@127.0.0.1'))
, $expected-result :=
	<expected-result/>
, $resource := util:binary-doc(concat($script-collection, "bg.gif"))
, $actual-result := 
	<actual-result>
		{
		ft-client:store-resource($connection, concat("/dir-with-rights/tmp/bg", util:uuid(), ".gif"), $resource)
		}
	</actual-result>
, $close-connection := ft-client:disconnect($connection)		
	
	
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