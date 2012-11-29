xquery version "1.0";

import module namespace xut = "http://kuberam.ro/ns/xquery-unit-tests" at "../../xquery-unit-tests.xqm";

let $input := util:binary-doc(concat($xut:resources-collection, 'keystore'))
let $expected-result :=
	<expected-result>UI/aOJodA6gtJPitQ6xcJA==</expected-result>
let $actual-result :=
	<actual-result>
		{crypto:hash($input, "MD5", ())}
	</actual-result>
let $condition := normalize-space($expected-result/text()) = normalize-space($actual-result/text())
	

return
	<result>
		{
		(
		if ($condition)
			then <result-token>passed</result-token>
			else <result-token>failed</result-token>
		, $actual-result
		)
		}
	</result>