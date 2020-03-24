# kafka-beginners-course

Learning purpose repository to learn Apache Kafka using the Udemy course [Apache Kafka series - Learn Apache Kafka for beginners v2](https://www.udemy.com/course/apache-kafka/).

## Prerequisites

Software needed:

    - JAVA 8
    - Docker

## Build

```sh
git clone https://github.com/mmmherma/kafka-beginners-course.git
bash compile.sh
```

## Run

```sh
docker run -it --rm --name producer-demo --network ais-ingestion_kafka-net producer-demo:v1
```