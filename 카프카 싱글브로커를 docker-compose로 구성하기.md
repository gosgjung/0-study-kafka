# 카프카 싱글브로커를 docker-compose로 구성하기

테스트환경으로 kafka single broker 를 구동할 경우도 있기에 docker-compose 로 카프카를 구동시키는 과정을 정리해두기로 했다.<br>



### 참고자료

- [Docker Compose 를 이용하여 Single Broker 구성하기](https://devocean.sk.com/blog/techBoardDetail.do?ID=164007)

<br>



### compose yml파일 작성

내 경우에는 docker-compose.yml 이라는 파일명이 아닌 `simple-single-broker.yml` 이라는 파일명으로 도커 컴포즈 파일을 작성했다.<br>

**simple-single-broker.yml**

```yaml
version: '2'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_SERVER_ID: 1
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
      ZOOKEEPER_INIT_LIMIT: 5
      ZOOKEEPER_SYNC_LIMIT: 2
    ports:
     - "22181:2181"
  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "29092:29092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETES_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
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

내 경우는 docker-compose 파일 명을  `simple-single-broker.yml` 이라고 지정했기에 아래와 같이 -f 옵션을 주어서 실행했다.

```bash
$ docker-compose -f simple-single-broker.yml up -d
```



만약 docker-compose 파일 명을 `docker-compse.yml` 로 지정했을 경우는 아래와 같이 실행해주자.

```bash
$ docker-compose up -d
```



### docker 상태 로그 확인

#### docker 이미지 확인

```bash
$ docker ps
CONTAINER ID   IMAGE                              COMMAND                  CREATED          STATUS          PORTS                                         NAMES
ed064735b300   confluentinc/cp-kafka:latest       "/etc/confluent/dock…"   34 minutes ago   Up 34 minutes   9092/tcp, 0.0.0.0:29092->29092/tcp            simple-single-broker-kafka-1
55e23d667674   confluentinc/cp-zookeeper:latest   "/etc/confluent/dock…"   34 minutes ago   Up 34 minutes   2888/tcp, 3888/tcp, 0.0.0.0:22181->2181/tcp   simple-single-broker-zookeeper-1
```

<br>



#### docker 로그 확인

```bash
# zookeeper
$ docker logs 55e23d667674
... 로그 확인

# kafka 
$ docker logs
... 로그 확인 (카프카 로그는 꽤 길다.)

```

<br>



### topic 생성





















