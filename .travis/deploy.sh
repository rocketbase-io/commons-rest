#!/usr/bin/env bash

set -e

# only do deployment, when travis detects a new tag
if [ ! -z "$TRAVIS_TAG" ]
then
    echo "only perform for commons-rest not for sample etc."
    cd commons-rest

    echo "on a tag -> set pom.xml <version> to $TRAVIS_TAG"
    mvn --settings ../.travis/settings.xml org.codehaus.mojo:versions-maven-plugin:2.5:set -DnewVersion=$TRAVIS_TAG -Prelease

    # perform deploy to sonatype
    mvn --settings ../.travis/settings.xml -Dsettings.security=../.travis/settings-security.xml clean deploy -DskipTests=true -B -U -Prelease

else
    echo "not on a tag -> keep snapshot version in pom.xml"
fi