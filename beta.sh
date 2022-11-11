git pull
rm -rf ./build/libs
./gradlew build
cp ./build/libs/Allium-*-build.local-*-all.jar ~/allium-beta/