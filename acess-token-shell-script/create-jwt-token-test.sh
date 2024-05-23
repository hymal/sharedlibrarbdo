#!/bin/bash

# set -euo pipefail

base64var() {
    printf "$1" | base64stream
}

base64stream() {
    base64 | tr '/+' '_-' | tr -d '=\n'
}

key_json_file="$1"
scope="https://www.googleapis.com/auth/cloud-platform"
valid_for_sec="${3:-3600}"

rm -rf test.pem sample.pem
# pem=`grep -Po '"private_key": *\K"[^"]*"' $key_json_file`
grep -Po '"private_key": *\K"[^"]*"' "$key_json_file" > test.pem
sed 's/\\n/\
/g' test.pem > sample.pem
sed -i 's/"//g' sample.pem
# pvt_key=`cat sample.pem`

sa_email="$(awk -F'"' '/client_email/{print $4}' $key_json_file)"
echo "$data"
echo "$sa_email"

header='{"alg":"RS256","typ":"JWT"}'
claim=$(cat <<EOF | sed 's/\s\+//g' | tr -d '\n'
  {
    "iss": "$sa_email",
    "scope": "$scope",
    "aud": "https://www.googleapis.com/oauth2/v4/token",
    "exp": $(($(date +%s) + $valid_for_sec)),
    "iat": $(date +%s)
  }
EOF
)
request_body="$(base64var "$header").$(base64var "$claim")"
echo "request_body"
#signature=$(openssl dgst -sha256 -sign <(echo "$data") <(printf "$request_body") | base64)


signature=$(openssl dgst -sha256 -sign "${cat sample.pem}"  "$request_body" | base64 | tr '+/' '-_' | tr -d '=')
printf "$request_body.$signature"