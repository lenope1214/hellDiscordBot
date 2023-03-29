docker-compose down
docker rmi lenope1214/hellbot
docker build -t lenope1214/hellbot .
docker-compose up -d
