rm hellbot.jar || true
./gradlew clean build --stacktrace
cp build/libs/*.jar .
nohup java -jar hellbot.jar > /dev/null 2>&1 &