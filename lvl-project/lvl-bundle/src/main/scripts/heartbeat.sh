#!/usr/bin/env bash
##############################################################################
# Copyright 2014-2015 EUBrazilCC (EU‐Brazil Cloud Connect)
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
# 1) Inform a LeishVL cluster about the current status of this node:
# 
#    heartbeat.sh <longitude> <latitude> <cluster-endpoint>
#    heartbeat.sh 3.7036      40.4169    http://lvl.i3m.upv.es
##############################################################################

# stop on errors
set -e

# resolve parameters
HB_LON=$1
if [[ -z "${HB_LON}" ]] ; then
  echo "Longitude was not specified. Exiting..."
  exit 1
fi

HB_LAT=$2
if [[ -z "${HB_LAT}" ]] ; then
  echo "Latitude was not specified. Exiting..."
  exit 1
fi

HB_ENDPOINT=$3
if [[ -z "${HB_ENDPOINT}" ]] ; then
  echo "LeishVL service endpoint was not specified. Exiting..."
  exit 1
fi

# http://www.cyberciti.biz/faq/how-to-find-my-public-ip-address-from-command-line-on-a-linux/
my_public_ip()
{
  # dig +short myip.opendns.com @resolver1.opendns.com
  dig TXT +short o-o.myaddr.l.google.com @ns1.google.com | awk -F'"' '{ print $2}'
}

timestamp()
{
  date +"%s%3N"
}

# do the job
PUBLIC_IP=`my_public_ip`
curl -X PUT -H "Content-Type: application/json" -d "{\"instanceId\":\"${PUBLIC_IP}\",\"roles\":[\"auth\",\"shard\",\"broker\"],\"heartbeat\":`timestamp`,\"location\":{\"type\":\"Point\",\"coordinates\":[${HB_LON},${HB_LAT}]}}" "${HB_ENDPOINT}/lvl-service/rest/v1/instances/${PUBLIC_IP}"

# curl -X POST -H "Content-Type: application/json" -d "{\"instanceId\":\"${PUBLIC_IP}\",\"roles\":[\"auth\",\"shard\",\"broker\"],\"heartbeat\":`timestamp`,\"location\":{\"type\":\"Point\",\"coordinates\":[${HB_LON},${HB_LAT}]}}" "${HB_ENDPOINT}/lvl-service/rest/v1/instances"

exit 0
