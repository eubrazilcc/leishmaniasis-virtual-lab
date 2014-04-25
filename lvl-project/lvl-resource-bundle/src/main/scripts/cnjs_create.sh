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
# 1) Parse list of country names obtained from OpenGeoCode.org, and create a 
#    JavaScript module:
#
#    cnjs_create.sh countrynames.txt
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
CN_MAPF=$1
if [[ ! -e "${CN_MAPF}" || ! -f "${CN_MAPF}" ]] ; then
  echo "Country name map file was not specified or is not a file. Exiting..."
  exit 1
fi

CN_FORMAT="OGC"

now()
{
  date +"%Y-%m-%d %H:%M %Z"
}

parse_openGeoCode()
{
  echo "/* Auto-generated with ${SCRIPT_NAME} on `now` */"
  echo "var cnMap = function() {"
  echo "    return { "
  sed '/^#/d' ${CN_MAPF} | awk 'BEGIN { FS="; " } { print "        \"" $1 "\":\"" $4 "\","}' | sed '$ s/,//g'
  echo "    };"
  echo -n "}"
}

# do the job
case "$CN_FORMAT" in
  *)
    parse_openGeoCode
    ;;
esac
