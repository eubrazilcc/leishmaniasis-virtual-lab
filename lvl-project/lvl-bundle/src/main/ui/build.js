/* RequireJS optimizer configuration. Usage: r.js -o build.js */
({	
	mainConfigFile : 'js/requirejs_main.js',
	appDir : './',
	baseUrl : 'js',
	dir : 'dist',
	removeCombined : true,
	findNestedDependencies : true,	
	inlineText : true,
	preserveLicenseComments : false,
	wrap : true,
	wrapShim : true,
	optimize : 'uglify2', // 'none',
	optimizeCss : 'standard',
	fileExclusionRegExp : /^\.|^(r|build)\.js$|\.map$|html-minifier\.conf/,	
	modules: [
		{
			name : 'requirejs_main',			
			excludeShallow : [
				'text!data/config.json',
				'text!data/pipelines.json',
				'text!data/events.json'
			]
		}
	],
	paths : {
		jquery : 'empty:',
		bootstrap : 'empty:'
    }
})