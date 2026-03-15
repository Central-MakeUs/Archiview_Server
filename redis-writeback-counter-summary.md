# Redis Write-back 패턴 기반 카운터 최적화 정리

## 결론

현재 코드베이스에는 아래 내용이 실제로 구현되어 있습니다.

- Redis 기반 Write-back 구조
- Lua Script를 활용한 카운트 delta 누적 + Dirty ID 적재의 원자 처리
- 스케줄러 기반 flush
- Bulk Update 기반 DB 반영
- DB 반영 실패 시 requeue 로직

즉, 아래 이력서 문장은 전반적으로 코드와 부합합니다.

> 조회수, 저장수 등 고빈도 카운터 업데이트로 인한 DB 쓰기 부하를 줄이기 위해 Redis 기반 Write-back 구조 채택  
> Lua Script를 활용해 카운트 증감과 Dirty ID 관리를 원자적으로 처리하고, 스케줄러 기반 Bulk Update로 DB에 반영했습니다.  
> 장애 상황에서는 재처리 로직을 통해 데이터 유실 가능성을 낮추고 최종적 일관성을 보장.

다만 마지막 문장의 "최종적 일관성을 보장"은 의도 측면에서는 맞지만, 인터뷰에서는 "재처리 가능 구조를 통해 최종적 일관성을 지향했다" 정도로 표현하는 것이 더 안전합니다.

---

## 실제 구현 구조

### 1. 카운터 변경분을 Redis에 delta로 누적

카운터 증감 요청은 즉시 MySQL에 반영하지 않고 Redis에 delta 형태로 누적됩니다.

- 대상 메트릭
  - `view`
  - `save`
  - `instagram_inflow`
  - `direction`
- 저장 방식
  - `post-place:count:delta:{postPlaceId}` 해시에 증감값 누적
  - `post-place:count:dirty` set에 dirty ID 추가

관련 코드:

- [PostPlaceCountRedisRepository.java](/Users/taek/Documents/Spring/archiview/src/main/java/zero/conflict/archiview/post/infrastructure/redis/PostPlaceCountRedisRepository.java#L13)
- [RedisPostPlaceCountService.java](/Users/taek/Documents/Spring/archiview/src/main/java/zero/conflict/archiview/post/application/command/RedisPostPlaceCountService.java#L12)

### 2. Lua Script로 증감 + Dirty ID 적재를 원자적으로 처리

`PostPlaceCountRedisRepository`에서는 아래 Lua Script를 사용합니다.

- `HINCRBY`로 특정 메트릭 delta 증가
- `SADD`로 dirty set에 `postPlaceId` 추가

이 둘을 하나의 Redis script 실행으로 묶어 race condition 가능성을 줄였습니다.

관련 코드:

- [PostPlaceCountRedisRepository.java](/Users/taek/Documents/Spring/archiview/src/main/java/zero/conflict/archiview/post/infrastructure/redis/PostPlaceCountRedisRepository.java#L15)
- [PostPlaceCountRedisRepository.java](/Users/taek/Documents/Spring/archiview/src/main/java/zero/conflict/archiview/post/infrastructure/redis/PostPlaceCountRedisRepository.java#L33)

### 3. 스케줄러가 주기적으로 dirty 대상 flush

스케줄러가 dirty set에서 일정 수의 ID를 꺼내고, 각 ID별 delta를 조회한 뒤 DB 반영 대상으로 변환합니다.

설정값:

- flush 주기: `3초`
- 배치 크기: `200`

관련 코드:

- [PostPlaceCountWriteBackScheduler.java](/Users/taek/Documents/Spring/archiview/src/main/java/zero/conflict/archiview/post/infrastructure/redis/PostPlaceCountWriteBackScheduler.java#L16)
- [application.yml](/Users/taek/Documents/Spring/archiview/src/main/resources/application.yml#L104)

### 4. MySQL에 Bulk Update로 반영

DB 반영은 건별 update가 아니라 `CASE WHEN` 기반 단일 SQL update로 처리됩니다.

이 방식으로 여러 `post_place` row의 카운터를 한 번에 갱신합니다.

관련 코드:

- [PostPlaceCountBulkUpdater.java](/Users/taek/Documents/Spring/archiview/src/main/java/zero/conflict/archiview/post/infrastructure/persistence/PostPlaceCountBulkUpdater.java#L19)

### 5. DB 반영 실패 시 requeue

flush 중 예외가 발생하면, 이번 배치에서 꺼낸 delta들을 다시 Redis로 적재합니다.

즉, DB 반영 실패 1회로 delta가 바로 폐기되지 않도록 복구 경로를 마련해 둔 상태입니다.

관련 코드:

- [PostPlaceCountWriteBackScheduler.java](/Users/taek/Documents/Spring/archiview/src/main/java/zero/conflict/archiview/post/infrastructure/redis/PostPlaceCountWriteBackScheduler.java#L37)
- [PostPlaceCountRedisRepository.java](/Users/taek/Documents/Spring/archiview/src/main/java/zero/conflict/archiview/post/infrastructure/redis/PostPlaceCountRedisRepository.java#L75)

---

## 코드 기준으로 방어 가능한 설명

### 안전하게 말할 수 있는 것

- 고빈도 카운터성 쓰기를 Redis에 임시 적재한 뒤 배치 반영하는 Write-back 구조를 구현했다.
- Lua Script를 이용해 카운트 delta 누적과 Dirty ID 적재를 원자적으로 처리했다.
- 스케줄러를 통해 Redis의 변경분을 주기적으로 수집하고 MySQL에 Bulk Update로 반영했다.
- 반영 실패 시 requeue 로직을 통해 재처리 가능 구조를 마련했다.

### 표현을 조금 조심해야 하는 것

- "데이터 유실을 방지했다"
- "최종적 일관성을 완전히 보장했다"

이유:

- 현재 구현은 flush 과정에서 dirty set pop과 hash delete를 사용합니다.
- 일반적인 실패 상황에 대한 requeue는 존재하지만, 프로세스 중간 crash나 Redis 자체 장애까지 포함해 강한 무손실 보장을 증명하는 구조라고 단정하긴 어렵습니다.

즉, 아래처럼 표현하는 것이 더 정확합니다.

- 장애 상황에서 재처리 가능한 구조를 두어 데이터 유실 가능성을 낮췄다.
- 배치 반영 기반의 최종적 일관성을 지향했다.

---

## 이력서용 문장 추천

### 버전 1

- 조회수, 저장수 등 고빈도 카운터 업데이트의 DB 쓰기 부하를 줄이기 위해 Redis 기반 Write-back 구조를 설계했습니다.
- Lua Script로 카운트 delta 누적과 Dirty ID 적재를 원자적으로 처리하고, 스케줄러 기반 배치 flush와 Bulk Update로 MySQL 반영을 최적화했습니다.
- DB 반영 실패 시 delta requeue 로직을 통해 재처리 가능한 구조를 마련했습니다.

### 버전 2

- 장소 조회수/저장수 카운터에 Redis Write-back 아키텍처를 적용해 실시간 DB update를 배치 반영 구조로 전환했습니다.
- Redis Lua Script를 이용해 카운터 증감과 Dirty ID 관리를 원자적으로 처리하고, 3초 주기 배치 flush 및 최대 200건 Bulk Update로 쓰기 부하를 분산했습니다.
- flush 실패 시 재적재 로직을 구현해 장애 상황에서도 데이터 유실 가능성을 낮췄습니다.

---

## 면접에서 설명할 때의 한 줄 요약

"카운터는 정합성보다 처리량이 중요한 영역이라고 판단해서, DB에 즉시 쓰지 않고 Redis에 delta를 쌓아두었다가 주기적으로 bulk 반영하는 Write-back 구조로 최적화했습니다. 이때 Lua Script로 원자성을 확보했고, flush 실패 시 requeue로 복구 가능성을 확보했습니다."
