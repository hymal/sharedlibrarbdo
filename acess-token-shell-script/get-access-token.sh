#!/bin/bash

key_json_file="$1"
scope="$2"
jwt_token=$(./create-jwt-token.sh "$key_json_file" "$scope")

curl -v -s -X POST https://www.googleapis.com/oauth2/v4/token \
    --data-urlencode 'grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer' \
    --data-urlencode "assertion=$jwt_token" 