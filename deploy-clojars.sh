#!/bin/bash
op run --env-file="clojars.env" -- clojure -T:build deploy
