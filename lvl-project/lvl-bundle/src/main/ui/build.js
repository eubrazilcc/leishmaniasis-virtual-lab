({
	baseUrl : 'js',
	mainConfigFile : 'js/requirejs_main.js',
	name : 'app',
	out : 'dist/js/requirejs_main.js',	
	wrap : true,
	findNestedDependencies : true,
	removeCombined : true,		
	optimize : 'uglify2',	
	inlineText : true,
	preserveLicenseComments : false
})