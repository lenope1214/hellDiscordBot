# 도커라이징 전

#rm hellbot.jar || true
#./gradlew clean build --stacktrace
#
##fuser -k $(jps -lv | awk '/hellbot.jar/ {print $1}')/tcp
#kill $(jps -lv | awk '/hellbot.jar/ {print $1}')
#
#cp build/libs/*.jar .
#nohup java -jar hellbot.jar > /dev/null 2>&1 &

# 도커라이징 빌드
docker build -t lenope1214/hellbot .

## 기존에 돌아가던 컨테이너 삭제
# docker ps -q --filter "name=laundry-prod-server" | grep -q . && docker stop laundry-prod-server && docker rm laundry-prod-server | true
docker ps -q --filter "name=hellbot" && docker stop hellbot | true && docker rm hellbot | true

## 빌드한 이미지 실행
docker run -p 10100:10100 -p 10101:10101 -d --name=hellbot --link mariadb lenope1214/hellbot -v logs:/logs