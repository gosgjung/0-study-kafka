# 카프카 싱글브로커를 docker-compose로 구성하기

사실은 single broker 로 굉장히 편하게 넘어가려 했는데 결국 멀티브로커로 구성해야 했었다.



### 참고자료

- [kafka로 비디오 스트림 처리하기](https://velog.io/@djm0727/kafka%EB%A1%9C-%EB%B9%84%EB%94%94%EC%98%A4-%EC%8A%A4%ED%8A%B8%EB%A6%BC-%EC%B2%98%EB%A6%AC%ED%95%98%EA%B8%B0-w-Yolox)
  - 최종적으로는 이 자료를 보고 잘 수행되게 되었다.

- [Docker Compose로 멀티브로커 Kafka 구성하기](https://devocean.sk.com/blog/techBoardDetail.do?ID=164016)
  - 아래 자료 다음으로 참고했던 자료.

- [Docker Compose 를 이용하여 Single Broker 구성하기](https://devocean.sk.com/blog/techBoardDetail.do?ID=164007)
  - 제일 처음으로 참고했던 자료.
  - 하다가 안되서 1시간 정도... 헤맸다.

<br>



### compose yml 파일 작성

**docker-compose.yml**

싱글브로커이지만 아래 컴포즈 파일처럼 브로커용도의 노드를 3개 선언해야 오류없이 실행된다.

```yaml
version: '2'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOO_MY_ID: 1
      ZOO_PORT: 1
      ZOOKEEPER_SERVER_ID: 1
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
      ZOOKEEPER_INIT_LIMIT: 5
      ZOOKEEPER_SYNC_LIMIT: 2
    ports:
     - "22181:2181"
    volumes:
      - ./data/zookeeper/data:/data
      - ./data/zookeeper/datalog:/datalogco
  kafka1:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "29091:9091"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: "zookeeper:2181"
      KAFKA_ADVERTISED_LISTENERS: LISTENER_DOCKER_INTERNAL://kafka1:9091,LISTENER_DOCKER_EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:29091
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: LISTENER_DOCKER_INTERNAL:PLAINTEXT,LISTENER_DOCKER_EXTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: LISTENER_DOCKER_INTERNAL
      KAFKA_OFFSETES_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
    volumes:
      - ./data/kafka1/data:/tmp/kafka-logs
  kafka2:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "29092:9092"
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_ZOOKEEPER_CONNECT: "zookeeper:2181"
      KAFKA_ADVERTISED_LISTENERS: LISTENER_DOCKER_INTERNAL://kafka2:9092,LISTENER_DOCKER_EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: LISTENER_DOCKER_INTERNAL:PLAINTEXT,LISTENER_DOCKER_EXTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: LISTENER_DOCKER_INTERNAL
      KAFKA_OFFSETES_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
    volumes:
      - ./data/kafka2/data:/tmp/kafka-logs
  kafka3:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "29093:9093"
    environment:
      KAFKA_BROKER_ID: 3
      KAFKA_ZOOKEEPER_CONNECT: "zookeeper:2181"
      KAFKA_ADVERTISED_LISTENERS: LISTENER_DOCKER_INTERNAL://kafka3:9093,LISTENER_DOCKER_EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:29093
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: LISTENER_DOCKER_INTERNAL:PLAINTEXT,LISTENER_DOCKER_EXTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: LISTENER_DOCKER_INTERNAL
      KAFKA_OFFSETES_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
    volumes:
      - ./data/kafka3/data:/tmp/kafka-logs
```



image:

- kafka 브로커는 confluentinc/cp-kafka:latest 를 이용했다.
- zookeeper 브로커는 confluentinc/cp-zookeeper:latest 를 이용했다.
- 태그는 latest보다는 지정된 버젼을 사용하는것이 권장된다.
- latest라는 태그를 지정하면 매번 컨테이너를 실행할 때마다 최신버전을 다운받아 실행하기에 변경된 버전으로 인해 원하지 않는 결과를 보게 될수도 있다.(주의 필요)

<br>



- version
  - docker-compose 의 버전을 명시

- services.zookeeper
  - zookeeper의 서비스 명을 `zookeeper` 로 지정했다.
  - 로컬 PC에서 중복되는 이름이 싫다면 고유한 이름을 만들어서 지정해주자.
- services:zookeeper.environment
  - zookeeper 이미지 내부에서 사용헐 환경변수들을 지정한다.
- ZOOKEEPER_SERVER_ID
  - zookeeper 클러스터에서 유일하게 주키퍼를 식별할 아이디
  - 같은 클러스터 내에서 이 값은 중복되면 안된다.
  - 싱글 브로커이므로 이 값은 의미가 없다.

- ZOOKEEPER_CLIENT_PORT
  - zookeeper_client_port 를 지정한다.
  - 기본 주키퍼 포트인 2181을 사용하기로 했다.
  - 컨테이너 내부에서 2181 포트에서 실행된다.
- ZOOKEEPER_TICK_TIME
  - zookeeper가 클러스터를 구성할 때 동기화를 위한 기본 틱 타임을 지정한다.
  - millisecond 단위다.
  - 여기서는 2000으로 지정했기에 2초 단위로 동기화를 수행한다.
- ZOOKEEPER_INIT_LIMIT
  - 주키퍼 초기화를 위한 제한시간
  - 주키퍼 클러스터는 쿼럼이라는 과정으로 마스터를 선출한다. 이때 주키퍼들이 리더에게 커넥션을 맺을 때 커넥션 타임아웃을 `ZOOKEEPER_INIT_LIMIT` 이라는 프로퍼티에 지정한다.
  - millisecond 단위다.
  - 여기서는 10초로 지정해줬다.
- ZOOKEEPER_SYNC_LIMIT
  - 주키퍼 리더와 나머지 서버들 간의 싱크를 하는 시간이다.
  - 이 시간 내에 싱크 응답이 들어오면 클러스터가 정상적으로 구성되어 있음을 확인할수 있다.
  - 멀티 브로커에서 유효한 속성이다.
  - 여기서는 2로 잡았기 때문에 2000 x 2 = 4000 으로 계산되어 4초가 된다.



- services.kafka
  - zookeeper의 서비스 명을 `kafka` 로 지정했다.
  - 로컬 PC에서 중복되는 이름이 싫다면 고유한 이름을 만들어서 지정해주자.
- services.kafka.depends_on
  - docker-compose 에서는 서비스들의 우선순위를 지정해주기 위해 depends_on 을 이용한다.
  - zookeeper 라고 지정했기에 kafka 는 zookeeper 가 먼저 실행되어야 컨테이너가 구동을 시작한다.
- services.kafka.ports
  - kafka 브로커의 포트를 의미
  - 외부포트:내부포트 형식
- services.kafka.environment
  - kafka이미지 내부에서 사용헐 환경변수들을 지정한다.

- KAFKA_BROKER_ID
  - kafka 브로커 아이디를 지정한다.
  - 유니크해야 하며 지금 예제는 단일 브로커이기 때문에 없어도 무관
- KAFKA_ZOOKEEPER_CONNECT
  - kafka 가 zookeeper 에 접속하기 위한 대상을 지정한다.
  - 여기서는 'zookeeper:2181' 과 같이 명시했다.
  - zookeeper(서비스명):2181(컨테이너 내부 포트) 로 대상을 지정했다.
- KAFKA_ADVERTISED_LISTENERS
  - 외부에서 접속하기 위한 리스너 설정
- KAFKA_LISTENER_SECURITY_PROTOCOL_MAP
  - 보안을 위한 프로토콜 매핑
  - KAFKA_ADVERTISED_LISTENERS 와 함께 key/value 로 매핑된다.
- KAFKA_INTER_BROKER_LISTENER_NAME
  - 도커 내부에서 사용할 리스너 이름을 지정
  - 이전에 매핑된 PLAINTEXT가 사용되었다.
- KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
  - single 브로커로 운영할 것이기에 1로 지정했다.
  - 멀티 브로커는 기본값을 사용하기에 이 설정이 없다.
- KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS
  - 카프카 그룹이 초기 리밸런싱할 때 컨슈머들이 컨슈머 그룹에 조인할 때 대기 시간이다.



### docker-compose 실행

```bash
$ docker-compose up -d
```



### docker 상태 로그 확인

#### docker 이미지 확인

```bash
$ docker ps
CONTAINER ID   IMAGE                              COMMAND                  CREATED       STATUS       PORTS                                         NAMES
dcac63fe59bf   confluentinc/cp-kafka:latest       "/etc/confluent/dock…"   7 hours ago   Up 7 hours   0.0.0.0:29092->9092/tcp                       simple-single-broker-kafka2-1
36fa7298408f   confluentinc/cp-kafka:latest       "/etc/confluent/dock…"   7 hours ago   Up 7 hours   9092/tcp, 0.0.0.0:29091->9091/tcp             simple-single-broker-kafka1-1
813c6a5a2235   confluentinc/cp-kafka:latest       "/etc/confluent/dock…"   7 hours ago   Up 7 hours   9092/tcp, 0.0.0.0:29093->9093/tcp             simple-single-broker-kafka3-1
f24b1569e057   confluentinc/cp-zookeeper:latest   "/etc/confluent/dock…"   7 hours ago   Up 7 hours   2888/tcp, 3888/tcp, 0.0.0.0:22181->2181/tcp   simple-single-broker-zookeeper-1
```

<br>



#### docker 로그 확인

```bash
# zookeeper
$ docker logs f24b1569e057
... 로그 확인

# kafka 
$ docker logs 36fa7298408f

# ... 하나씩 모두 조회해보자

```

<br>



### topic 생성

topic 명은 `helloworld-topic` 으로 지어줬다.

```bash
$ docker-compose exec kafka1 kafka-topics --create --topic helloworld-topic --bootstrap-server kafka1:9091 --replication-factor 3 --partitions 2
Created topic helloworld-topic.
```



- kafka 
  - docker-compose yml 파일에 정의한 브로커 컨테이너에 대한 서비스 이름
- kafka-topics
  - 카프카 토픽에 대한 명령을 실행
- --create
  - 토픽을 생성하겠다는 의미
- --topic
  - 생성할 토픽 명을 지정
- --bootstrap-server service:port
  - bootstrap-server 는 kafka 브로커 서비스를 의미한다.
  - 서비스:포트로 카프카 브로커를 지정해줬다.
- --replication-factor
  - 복제 갯수
  - 싱글노드이기에 1로 지정해줬다.
- --partition
  - 토픽 내에 파티션 갯수를 지정



### 생성된 topic 확인

```bash
$ docker-compose exec kafka1 kafka-topics --describe --topic helloworld-topic --bootstrap-server kafka1:9092

Topic: helloworld-topic TopicId: tC9TmTtaR-KCQKnWePCjng PartitionCount: 1       ReplicationFactor: 1    Configs:
        Topic: helloworld-topic Partition: 0    Leader: 1       Replicas: 1     Isr: 1
```

<br>



### 컨슈머 실행

만들어놓은 카프카 브로커 노드 들 중 kafka1 브로커 노드에 접속하자.

```bash
$ docker-compose exec kafka1 bash
```



컨슈머를 실행한다.

```bash
$ kafka-console-consumer --topic helloworld-topic --bootstrap-server kafka1:9091
```

<br>



### 프로듀서 실행

터미널을 새로 열어서 만들어놓은 카프카 브로커 노드 들 중 kafka1 브로커 노드에 접속한다.

```bash
$ docker-compose exec kafka1 bash
```

<br>



```bash
$ kafka-console-producer --topic helloworld-topic --broker-list kafka1:9091
>
```

<br>



#### 프로듀서에서 메시지 전송

프로듀서를 실행시킨 터미널에서 메시지를 전송해보자.

```plain
>hello
>nice to meet you
>i'm fine, and you
```

<br>



#### 컨슈머 터미널에서 결과 확인

프로듀서를 띄우기 전에 컨슈머를 띄워둔 터미널에서 아래와 같은 메시지가 나타난다면 제대로 설치된 것이다.

```plain
hello
nice to meet you
i'm fine, and you
```

<br>



### 도커 컴포즈 삭제

더 이상 사용을 안한다면 삭제해주자.

(로컬 개발PC가 힘들어함)

![1](C:\Users\soong\workspace\gosgjung\0-study-kafka\img\DOCKER-COMPOSE-SINGLE-NODE\1.png)

















