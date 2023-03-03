#!/bin/bash
cd ..
# build java project -> generate openapi-zip
mvn clean install
# move back to generator
cd client-generation

mkdir -p ./tmp

NX_WORKSPACE=rcktbs-commons
PACKAGE=openapi-client
ARRAY=(
"../commons-rest-openapi/target/test.zip:openapi"
 )
for config in "${ARRAY[@]}" ; do
    KEY=${config%%:*}
    VALUE=${config#*:}

    rm -rf ./tmp/*
    unzip $KEY -d ./tmp

    # copy hooks
    rm -rf ./$NX_WORKSPACE/packages/$PACKAGE/src/hooks/$VALUE
    mkdir -p ./$NX_WORKSPACE/packages/$PACKAGE/src/hooks/$VALUE
    cp -r ./tmp/src/hooks/* ./$NX_WORKSPACE/packages/$PACKAGE/src/hooks/$VALUE
    # copy rest-client
    rm -rf ./$NX_WORKSPACE/packages/$PACKAGE/src/clients/$VALUE
    mkdir -p ./$NX_WORKSPACE/packages/$PACKAGE/src/clients/$VALUE
    cp -r ./tmp/src/clients/* ./$NX_WORKSPACE/packages/$PACKAGE/src/clients/$VALUE
done

#
# mkdir -p ./$NX_WORKSPACE/packages/$PACKAGE/src/model/
# cp ./$PACKAGE/target/typescript-generator/user.ts ./$NX_WORKSPACE/packages/$PACKAGE/src/model/
#
rm -rf ./tmp

cd $NX_WORKSPACE
cd packages/$PACKAGE
npm version "0.1.0-rc.$(date '+%Y%m%d-%H%M')"
npm i
cd ../..
npm install -g npx
npm install -g nx
npm i
npx nx lint $PACKAGE --fix
npx nx build $PACKAGE
