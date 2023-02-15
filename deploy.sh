rm hellbot.jar || true
./gradlew clean build --stacktrace

#fuser -k $(jps -lv | awk '/hellbot.jar/ {print $1}')/tcp
kill $(jps -lv | awk '/hellbot.jar/ {print $1}')

cp build/libs/*.jar .
nohup java -jar hellbot.jar > /dev/null 2>&1 &