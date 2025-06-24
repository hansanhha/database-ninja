[@DataJpaTest](#datajpatest)

[동작 방식](#동작-방식)

[테스트 설정](#테스트-설정)

[@Sql](#sql)


## @DataJpaTest

@DataJpaTest 어노테이션은 스프링 부트에서 스프링 데이터 JPA 관련 컴포넌트만 로드하여 리포지토리 슬라이스 테스트를 수행할 수 있도록 도와준다

동적 쿼리(QueryDSL 등), 관계 매핑 동작 확인, DB 제약 조건 검증 등 리포지토리/데이터베이스에 대한 단위 테스트를 작성할 때 사용된다

애플리케이션에 로드되는 대상은 다음과 같다

엔티티 관련
- @Entity
- @Repository (리포지토리 인터페이스)

쿼리/데이터 관련
- JdbcTemplate
- TestEntityManager

설정 및 컴포넌트
- DataSource
- Hibernate/JPA 관련 설정


## 동작 방식

기본적으로 내장된 임베디드 H2 메모리 데이터베이스를 사용하여 테스트를 진행한다

테스트마다 트랜잭션이 자동으로 시작되고 테스트 종료 시 모든 트랜잭션을 롤백한다


## 테스트 설정

@Rollback(false) 어노테이션을 적용하면 명시적으로 커밋할 수 있다

아래와 같이 datasource 설정을 통해 테스트 데이터베이스를 설정할 수 있다

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/testdb
    username: testuser
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
```


## @Sql

@Sql 어노테이션은 테스트 실행 전후로 SQL 스크립트를 실행할 수 있도록 도와준다

주로 테스트용 데이터 준비, DB 초기화, 테스트 종료 후 정리 작업 등에 사용된다

어노테이션은 클래스와 메서드에 적용할 수 있으며 클래스에 붙이면 모든 테스트 메서드 실행 전 공통으로 적용된다

일반적으로 테스트 메서드가 끝나면 자동으로 트랜잭션이 롤백되므로 각 테스트가 실행될 때는 @Sql 스크립트에 설정한 초기 상태가 보장된다

**Sql 스크립트 파일 또는 인라인 SQL 쿼리 지정**

```java
@Sql("/sql/user-init.sql")
@DataJpaTest
class UserRepositoryTests {
    
}
```

```java
@Sql(statements = "INSERT INTO users (id, username) VALUES (1, 'testuser')")
@DataJpaTest
class UserRepositoryTests {
    
}
```

**스크립트 실행 시점**:  테스트 실행 전/후에 SQL을 실행할 수 있다

```java
// 테스트 실행 전 SQL 실행
@Sql(scripts = "/sql/user-init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)

// 테스트 실행 후 SQL 실행
@Sql(scripts = "/sql/user-init.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
```

**여러 파일 지정**: @Sql의 실행 순서는 파일명 정렬 순서를 따른다

```java
@Sql({
        "/sql/insert-user.sql",
        "/sql/insert-post.sql",
})
```

조건에 따라 다른 데이터를 주입하여 시나리오를 분리할 수 있다

```java
@Nested
@Sql("/sql/participant-init.sql")
class 참여자가_있는_경우 { ... }

@Nested
class 참여자가_없는_경우 { ... }
```
