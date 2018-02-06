# how to publish to sonartype

doesn't work with travis command history to perform release locally

````bash
git tag 0.0.2
git push --tags
mvn versions:set -DnewVersion=0.0.2 -Prelease
mvn versions:commit
mvn clean deploy -DskipTests=true -Prelease

mvn versions:set -DnewVersion=0.0.3-SNAPSHOT
mvn versions:commit
````