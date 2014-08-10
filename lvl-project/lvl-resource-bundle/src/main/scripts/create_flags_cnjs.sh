#!/usr/bin/env bash
##############################################################################
# Copyright 2014 EUBrazilCC (EU‐Brazil Cloud Connect)
# 
# Licensed under the EUPL, Version 1.1 or - as soon they will be approved by 
# the European Commission - subsequent versions of the EUPL (the "Licence");
# You may not use this work except in compliance with the Licence.
# You may obtain a copy of the Licence at:
# 
#   http://ec.europa.eu/idabc/eupl
# 
# Unless required by applicable law or agreed to in writing, software 
# distributed under the Licence is distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the Licence for the specific language governing permissions and 
# limitations under the Licence.
# 
# This product combines work with different licenses. See the "NOTICE" text
# file for details on the various modules and licenses.
# The "NOTICE" text file is part of the distribution. Any derivative works
# that you distribute must include a readable copy of the "NOTICE" text file.
##############################################################################

##############################################################################
# Usage examples:
#
# 1) Parse list of country names obtained from OpenGeoCode.org, create a CSS 
#    image sprite with the country flags obtained from GeoNames.org and a 
#    RequireJS module with the conversion from the two-letter country codes
#    (ISO 3166-1 alpha-2) to country names:
#
#    create_flags_cnjs.sh OGC
#
#    Output files: output/flags.css, output/flags.png, output/country_names.js
##############################################################################

# stop on errors
set -e

SCRIPT_NAME=$(basename $0)

# resolve to absolute path where this script run
if [ ! -h $0 ]; then
  SCRIPT_DIR=$(cd $(dirname $0) && pwd)
else
  SCRIPT_DIR=$(cd $(dirname $(readlink $0)) && pwd)
fi

# resolve parameters
CN_FORMAT=$1
if [[ -z "${CN_FORMAT}" ]] ; then
  echo "Country name map format was not specified. Exiting..."
  exit 1
fi

CN_MAPF="countrynames.txt"

now()
{
  date +"%Y-%m-%d %H:%M %Z"
}

createSpriteOGC()
{
  echo " >> Downloading country names from OpenGeoCode.org..."
  
  wget -q -P output http://opengeocode.org/download/${CN_MAPF}
  if [[ ! -e "output/${CN_MAPF}" || ! -f "output/${CN_MAPF}" ]] ; then
    echo "Country name map file cannot be found or is not a file. Exiting..."
    exit 1
  fi
  
  echo "`cat output/countrynames.txt | grep -v '^#.*' | wc -l` country names fetched"

  echo " >> Downloading country flags from GeoNames.org..."
  
  sed '/^#/d' output/${CN_MAPF} | awk 'BEGIN { FS="; " } { system("wget -q -P output/icons http://www.geonames.org/flags/x/" tolower($1) ".gif") }'
  FLAGS=""; CODES=""
  for f in output/icons/*.gif ; do
    FLAGS="${FLAGS} ${f}"
    CODES="$CODES ${f:13:2}"
  done
  
  echo "`ls -l output/icons/*.gif | wc -l` icon flags fetched"

  echo " >> Converting and assembling the flags into a single image..."

  SPRITE_H=16
  montage ${FLAGS} -tile 1x -resize "x${SPRITE_H}+1+2" -background transparent -geometry "x${SPRITE_H}>+0+0" -gravity NorthWest output/sprite.png  
  
  echo "`du -h output/sprite.png | awk '{ print $1 }'` image sprite created"
  
  echo " >> Optimizing the flags image..."
  
  pngcrush -q output/sprite.png output/flags.png
  
  echo "`du -h output/flags.png | awk '{ print $1 }'` image sprite optimized"
  
  SPRITE_W=`identify -format "%[fx:w]" output/flags.png`

  echo " >> Writing the CSS image sprite..."

  echo "/* Auto-generated with ${SCRIPT_NAME} on `now` */" > output/flags.css
  echo ".flag {
  width: ${SPRITE_W}px;
  height: ${SPRITE_H}px;
  background:url(../img/flags.png) no-repeat
}" >> output/flags.css

  OFFSET=0
  for c in $CODES ; do    
    echo ".flag.flag-${c} { background-position: 0 -${OFFSET}px }" >> output/flags.css
    OFFSET=$(($OFFSET + $SPRITE_H))
  done
}

# Includes the OGC fields: 1) ISO 3166-1 alpha-2; and 5) ISO 3166-1 English short name (proper reading order)
createCountryNameOGC()
{
  echo " >> Writing the country name JavaScript module..."

  echo "/* Auto-generated with ${SCRIPT_NAME} on `now` */" > output/country_names.js
  echo "define(function () {
    return { " >> output/country_names.js
  sed '/^#/d' output/${CN_MAPF} | awk 'BEGIN { FS="; " } { print "        \"" $1 "\":\"" $5 "\","}' | sed '$ s/,//g' >> output/country_names.js
  echo "    }
});" >> output/country_names.js
}

# do the job
case "$CN_FORMAT" in
  OGC) # OpenGeoCode
    createSpriteOGC
    createCountryNameOGC
    ;;
  *)
    echo "Usage: ${SCRIPT_DIR}/${SCRIPT_NAME} {OGC|XXX}"
    exit 1
    ;;    
esac

exit 0