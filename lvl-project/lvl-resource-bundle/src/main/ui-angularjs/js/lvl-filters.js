'use strict';

/* Filters */

angular.module('lvl.filters', [])
.filter('countryflag', [ '$sce', function($sce) { // country filter
	return function(code) {
		var country = '';
		if (code && typeof code === 'string') {
			var twoLetterCode = code.split("_")[1];
			var code2 = twoLetterCode ? twoLetterCode.toUpperCase() : '';
			var countryName = cnMap()[code2];
			if (countryName) {								
				country = $sce.trustAsHtml('<img src="img/blank.gif" class="flag flag-' + code2.toLowerCase() 
						+ '" alt="' + countryName + '" /> ' + countryName);
			}
		}
		return country;
	};
}])
.filter('truncate', function () {
	return function (text, length, end) {
		if (isNaN(length)) {
			length = 10;
		}
		if (end === undefined) {
			end = "...";
		}
		if (text.length <= length || text.length - end.length <= length) {
			return text;
		} else {
			return String(text).substring(0, length - end.length) + end;
		}

	};
});