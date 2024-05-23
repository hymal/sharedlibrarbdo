#!/bin/bash

set -euo pipefail

base64var() {
    printf "$1" | base64stream
}
base64stream() {
    base64 | tr '/+' '_-' | tr -d '=\n'
}

key_json_file="$1"
scope="$2"
valid_for_sec="${3:-3600}"

#rm test.pem  sample.pem

awk -F'"' '/private_key/{print $4}' $key_json_file > test.pem
sed 's/\\n/\
/g' test.pem > sample.pem
private_key="$(cat sample.pem)"
sa_email="$(awk -F'"' '/client_email/{print $4}' $key_json_file)"

header='{"alg":"RS256","typ":"JWT"}'
claim=$(cat <<EOF | sed 's/\s\+//g' |tr -d '\n'
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
signature=$(openssl dgst -sha256 -sign <(echo "$private_key") <(printf "$request_body") | base64stream)

printf "$request_body.$signature"
