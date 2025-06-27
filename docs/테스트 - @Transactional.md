[스프링 @Transactional](./스프링%20-%20@Transactional.md)

[테스트에서 @Transactional 동작 방식](#테스트에서-transactional-동작-방식)


## 테스트에서 @Transactional 동작 방식

[테스트 코드](../rdb/spring-data-jpa+querydsl/transaction/src/test/java/db/ninja/SpringTransactionalTest.java)

스프링 테스트 컨텍스트 (@DataJpaTest, @SpringBootTest 등) 안에서 @Transactional 어노테이션을 사용하면 테스트 시작 시 트랜잭션을 시작하고, 테스트 종료 시 무조건 롤백한다

테스트 클래스에 적용하면 각 테스트 메서드 실행 전 트랜잭션이 시작되고, 테스트 메서드가 끝나면 트랜잭션이 롤백된다

스프링의 테스트 프레임워크는 테스트 결과에 상관없이 항상 롤백하여 테스트 간 독립성과 반복 가능성을 보장한다

만약 테스트 후 DB 상태를 유지하고 싶다면 @Commit 어노테이션 또는 @Rollback(false) 어노테이션을 사용하여 실제로 데이터베이스에 커밋할 수 있다

```java
@Test
@Rollback(false) // 테스트 후 DB에 커밋
@Commit // 테스트 후 DB에 커밋
void shouldCommitAfterTest() {
    
}
```

특정 테스트 메서드에 트랜잭션을 적용하고 싶지 않으면 NOT_SUPPORTED 전파 옵션을 사용하여 트랜잭션 없이 실행할 수 있다

```java
@Test
@Transactional(propagation = Propagation.NOT_SUPPORTED)
void shouldTestWithoutTransaction() {
    // 트랜잭션 없이 실행
}
```
