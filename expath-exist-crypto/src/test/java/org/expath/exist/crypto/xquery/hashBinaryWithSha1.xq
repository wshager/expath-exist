xquery version "1.0";

let $script-collection := concat(replace(replace(request:get-effective-uri(), "/(\w)+.xql$", ""), "/rest//db", ""), '/')
let $input := util:binary-doc(concat(substring-before($script-collection, 'unit-tests/'), 'resources/keystore'))
let $expected-result :=
	<expected-result>GyscHvnJKxInsBLgSg/FRAmQXYU=</expected-result>
let $actual-result :=
	<actual-result>
		{crypto:hash($input, "SHA-1", "SUN")}
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