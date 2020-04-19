#!/bin/bash

# file that gets run when the docker container starts up
# Sam Fryer.

#first, start the nodejs server to receive any initial commands
node run/run.js $1 &

#then wait 10 seconds to make sure the commands came and executed
sleep 5

#finally, start the initial program
java PodManager $1

