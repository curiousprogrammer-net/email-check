#!/bin/bash

set -e

# prepare
clojure -T:build ci

# deploy 
op run --env-file="clojars.env" -- clojure -T:build deploy
