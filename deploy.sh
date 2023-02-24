# 도커라이징 전

#kill $(jps -lv | awk '/hellbot.jar/ {print $1}') || true

#java -version
#rm hellbot.jar || true
#cp build/libs/*.jar hellbot.jar
#nohup java -jar hellbot.jar > /dev/null 2>&1 &
#java -jar cms_prod_server.jar --spring.profiles.active=prod --server.port=8989

# 도커라이징 빌드

#  기존 hellbot image 삭제
#docker rmi lenope1214/hellbot

#docker build -t lenope1214/hellbot .

## 기존에 돌아가던 컨테이너 삭제
#docker ps -q --filter "name=laundry-prod-server" | grep -q . && docker stop laundry-prod-server && docker rm laundry-prod-server | true
docker-compose down

rm -rf build || true
rm -f hellbot.jar

## 빌드한 이미지 실행
#docker run -p 10100:10100 -p 10101:10101 -d --name=hellbot lenope1214/hellbot -v logs:/logs
docker-compose up -d