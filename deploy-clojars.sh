#!/bin/bash

set -e

# prepare
clojure -T:build ci

# deploy 
op account get > /dev/null || eval $(op signin) && echo "signed in"
op run --env-file="clojars.env" -- clojure -T:build deploy

# tag
RELEASE_VERSION=$(grep 'def version "' build.clj | grep -o -E '\d+\.\d+\.\d+')
git tag -a "$RELEASE_VERSION" -m "Release $RELEASE_VERSION"
git push --follow-tags origin main

