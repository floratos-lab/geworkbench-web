# deploy geworkbench app with docker

copy the necessary app file `cp ../target/geworkbench.war .`

build the image `docker build -t zhouji2018/geworkbench .`

run local test `docker run -d -p 8080:8080 --network=host zhouji2018/geworkbench`

* `--network=host`  : run in host network mode, so the app can access the host machine (for mysql); otherwise, we need to modify the persistent file to point to the accessible mysql IP.
