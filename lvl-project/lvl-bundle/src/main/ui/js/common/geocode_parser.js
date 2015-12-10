/**
 * Parses Google Geocode response.
 */

define(function () {	
	return function(resp) {
		var country, stateProvince, county, locality;
		if (resp && 'OK' === resp.status && $.isArray(resp.results) && resp.results.length > 0) {
			_.each(resp.results, function(r) {
				if ($.isArray(r.address_components) && r.address_components.length >= 4) {
					var countryAc = _.find(r.address_components, function(ac) {
						return _.contains(ac.types, 'country');
					}),
					stateProvinceAc = _.find(r.address_components, function(ac) {
						return _.contains(ac.types, 'administrative_area_level_1');
					}),
					countyAc = _.find(r.address_components, function(ac) {
						return _.contains(ac.types, 'administrative_area_level_2');
					}),
					localityAc = _.find(r.address_components, function(ac) {
						return _.contains(ac.types, 'sublocality_level_1');
					});
					country = countryAc ? countryAc.long_name : null;
					stateProvince = stateProvinceAc ? stateProvinceAc.short_name : null;
					county = countyAc ? countyAc.long_name : null;
					locality = localityAc ? localityAc.long_name : null;
				}
			});			
		}		
		return {
			country : country,
			stateProvince : stateProvince,
		    county : county,
		    locality : locality
		};
	}
});