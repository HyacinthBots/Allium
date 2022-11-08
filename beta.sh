git pull
rm -rf ./build/libs
./gradlew build
cp ./build/libs/SkyblockBot-*-build.local-*.jar ~/sbbot-beta/