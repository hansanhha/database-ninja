[트랜잭션에 필요한 리소스들](#트랜잭션에-필요한-리소스들)

[트랜잭션 수행 흐름](#트랜잭션-수행-흐름)

[자기 호출 문제](#자기-호출-문제)

[트랜잭션 전파](#트랜잭션-전파)

[REQUIRED의 동작 방식과 롤백 처리](#required의-동작-방식과-롤백-처리)

[REQUIRES_NEW의 동작 방식과 롤백 처리](#requires_new의-동작-방식과-롤백-처리)

[스프링의 롤백 처리 기준](#스프링의-롤백-처리-기준)


## 트랜잭션에 필요한 리소스들

트랜잭션이 수행될 때 스프링 트랜잭션 관리, JPA/하이버네이트, DB 커넥션 풀 등 다양한 리소스와 컴포넌트들이 유기적으로 동작하는데 이 요소들에 대해 알아보자

**@Transactional** 어노테이션은 스프링 AOP를 기반으로 트랜잭션 시작/커밋/롤백 제어를 처리하는 진입점 역할을 한다

**트랜잭션 매니저 (PlatformTransactionManager)**는 트랜잭션을 실질적으로 제어하는 요소로써 트랜잭션 경계 시작/종료, 커밋/롤백을 처리한다

JPA를 사용하면 JpaTransactionManager가 트랜잭션 매니저로 사용되며, JDBC를 사용하면 DataSourceTransactionManager가 사용된다

**트랜잭션 동기화 매니저 (TransactionSynchronizationManager)**는 ThreadLocal를 이용하여 현재 트랜잭션의 상태, 자원, 동기화 정보를 저장하는 역할을 가진다

**DataSource**는 커넥션 획득 SPI 추상화로 커넥션 풀에 접근하기 위해 사용된다 (HikariCP와 같은 커넥션 풀 라이브러리가 이를 구현함)

**커넥션 풀 (HikariCP)**은 애플리케이션과 데이터베이스 간의 커넥션을 재사용하기 위한 풀로 트랜잭션이 시작될 때 커넥션을 할당하고 트랜잭션이 끝나면 커넥션을 반환받는다

**DB 커넥션(java.sql.Connection)**은 실제 JDBC 커넥션으로 트랜잭션이 적용되는 물리적 자원이다 (트랜잭션 커밋/롤백 시 JDBC 커넥션 단위로 커밋/롤백이 수행된다)

**EntityManager, Session**은 JPA/하이버네이트가 트랜잭션 범위 내에서 SQL, 플러시, 더티 체킹 등을 수행하는 객체로, 트랜잭션 매니저가 관리하는 커넥션을 사용하여 DB와 상호작용한다

**JDBC 드라이버**는 SQL 실행, 커밋/롤백을 처리하는 실제 DB 통신 구현체이다 (트랜잭션 커밋/롤백 시 JDBC 드라이버를 통해 DB에 명령을 전달한다)

트랜잭션은 이러한 리소스들이 애플리케이션 내에서 사용하는 논리적인 단위로 트랜잭션 매니저가 커넥션 풀에서 커넥션을 가져와 트랜잭션 경계를 설정하고 EntityManager/Session을 통해 DB와 상호작용하며 트랜잭션이 끝나면 커넥션을 반환하는 방식으로 동작한다

커넥션이 트랜잭션의 실제 단위이며, 스레드 로컬을 통해 관리되므로 트랜잭션의 범위는 스레드 단위로 제한된다


## 트랜잭션 수행 흐름

```text
[ @Transactional ]
        ↓
[ 프록시 (AOP) ]
        ↓
[ PlatformTransactionManager ]
        ↓
[ TransactionSynchronizationManager ] ← 스레드 로컬
        ↓
[ DataSource ] → [ HikariCP ] → [ JDBC Connection ]
        ↓
[ 비즈니스 로직 ]
        ↓
[ 엔티티 매니저 (영속성 컨텍스트) ]
        ↓
[ SQL 실행 ]
        ↓
[ 커밋/롤백 ]
```

### 1. @Transactional 진입

프록시 객체가 대상 메서의 호출을 가로채고 `PlatformTransactionManager.getTransaction()` 메서드를 호출하여 트랜잭션을 시작한다

일반적으로 처음 @Transactional 어노테이션이 호출된 경우라면 새 트랜잭션을 시작하고 어노테이션에 설정된 트랜잭션 전파 옵션에 따라 기존 트랜잭션을 중지한 뒤 트랜잭션이 새로 시작되거나 기존 트랜잭션에 참여하게 된다

기존 트랜잭션에 참여하면 스레드 로컬에 저장된 커넥션, 엔티티 매니저를 공유한다

새 트랜잭션에 참여하면 새로운 커넥션과 엔티티 매니저가 생성되어 스레드 로컬에 저장된다

### 2. 커넥션 획득

PlatformTransactionManager는 DataSource(커넥션 풀, HikariCP 등)로부터 커넥션을 획득한다

해당 커넥션은 `AutoCommit = false` 상태로 설정되어 데이터베이스의 자동 커밋 기능을 비활성화되고, 애플리케이션에서 트랜잭션을 명시적으로 커밋하거나 롤백할 수 있도록 한다

### 3. TransactionSynchronizationManager을 통해 리소스 관리

TransactionSynchronizationManager을 이용하여 트랜잭션 경계 내에서 트랜잭션 참여 객체(커넥션과 엔티티 매니저, 트랜잭션 동기화 콜백 객체 등)를 스레드 로컬에 저장한다

이후 같은 스레드 내에서는 동일한 커넥션을 재사용할 수 있다 -> 메서드를 통해 커넥션이나 트랜잭션을 전달하지 않아도 된다

현재 스레드에서 어떤 트랜잭션이 열렸는지 추적할 수 있다

### 4. JPA (영속성 컨텍스트) 작업 수행

엔티티 매니저가 영속성 컨텍스트를 관리하며 쿼리는 flush() 시점에 JDBC 커넥션을 통해 전달된다

```text
엔티티 매니저 -> 하이버네이트 -> JDBC 드라이버 -> DB 커넥션 -> DB
```

영속성 컨텍스트는 트랜잭션과 함께 열리고 닫히며 데이터베이스 커밋/롤백 전까지 영속 상태의 객체들을 추적하고 관리하여 더티 체킹, 지연 로딩, 캐시 등을 처리한다

동일한 트랜잭션(REQUIRED)을 사용하면 DB 커넥션, 엔티티 매니저/영속성 컨텍스트를 사용한다

따라서 1차 캐시, 더티 체킹도 트랜잭션 전체에서 공유된다

반면 분리된 트랜잭션(REQUIRES_NEW)을 사용하면 별도의 DB 커넥션, 별도의 엔티티 매니저 인스턴스/영속성 컨텍스트를 사용한다

결과적으로 1차 캐시가 공유되지 않으며 외부 트랜잭션에서 영속한 엔티티가 내부 트랜잭션에서 조회되지 않는다

각각의 flush() 타이밍도 독립된다

### 5. 트랜잭션 종료 및 리소스 해제

PlatformTransactionManager.commit() 또는 rollback() 메서드를 호출한다

트랜잭션 동기화 매니저를 통해 스레드 로컬에 할당된 커넥션과 엔티티 매니저를 제거한 뒤 커넥션 풀에 커넥션을 반환한다


## 자기 호출 문제

[테스트 코드](../rdb/spring-data-jpa+querydsl/transaction/src/test/java/db/ninja/SelfCallingTest.java)

트랜잭션 어노테이션은 스프링 AOP 기반 프록시를 기반으로 동작한다

프록시는 대상 객체의 메서드를 감싸고 실행 호출 전후로 작업을 수행한다

이 때, **만약 대상 객체 내부에서 메서드를 직접 호출하면 프록시를 거치지 않고 곧바로 메서드가 실행되는 문제가 발생한다**

트랜잭션 어노테이션이 적용된 메서드가 프록시를 거치지 않고 실행되어 트랜잭션이 적용되지 않기 때문에 메서드에서 예외가 발생해도 트랜잭션이 롤백되지 않는다

이러한 문제를 자기 호출 문제라고 한다

```text
[ 클래스 A 메서드 ]
        ↓
[ 클래스 A 메서드 ]: @Transactional 미적용
```

```java
/*
    @Transactional 메서드를 자기 호출하는 경우
    트랜잭션 프록시를 거칠 수 없기 때문에 실질적으로 트랜잭션이 적용되지 않는다
    따라서 saveOrder 메서드에서 예외가 발생해도 트랜잭션이 롤백되지 않는다
    
    이러한 문제를 해결하려면 내부 호출을 외부 메서드로 분리해서 트랜잭션 프록시가 적용되도록 해야 한다
*/
public void placeOrder() {
    saveOrder();
}

@Transactional
public void saveOrder() {
    orderRepository.save(new Order());
    throw new RuntimeException("주문 저장 실패");
}
```

자기 호출 문제를 해결하기 위해서는 자기 호출 메서드를 외부 서비스로 분리하여 AOP를 적용받을 수 있도록 만들어야 한다

별도의 서비스로 분리하면 스프링의 IoC 컨테이너가 트랜잭션 프록시를 DI해주기 때문에 트랜잭션이 올바르게 적용될 수 있다

```text
[ 클래스 A 메서드 ]
        ↓
[ 트랜잭션 프록시 ]        
        ↓
[ 클래스 B 메서드 ]: @Transactional 적용
```

```java
/*
    자기 호출을 피하기 위해 OrderHelperService로 메서드를 분리하여 트랜잭션을 적용한다
 */
public void placeOrder() {
    orderHelperService.saveOrderWithTransaction();
}
```


## 트랜잭션 전파

[테스트 코드](../transaction/src/test/java/db/ninja/TxPropagationTest.java)

트랜잭션 전파는 트랜잭션이 없거나 있을 때 새로운 트랜잭션을 어떻게 처리할지 정의한다

JPA는 다음과 같은 트랜잭션 전파 옵션을 제공하며 필요에 따라 이를 선택하여 트랜잭션을 제어할 수 있다

**REQUIRED** (기본값): 기존 트랜잭션이 있으면 참여하고, 없으면 새로운 트랜잭션을 생성한다

**REQUIRES_NEW**: 항상 새로운 트랜잭션을 생성하고 기존 트랜잭션이 있으면 일시 중지한다 (독립적으로 커밋/롤백해야 되는 경우 사용함)

**NESTED**: 기존 트랜잭션 안에서 SAVEPOINT를 생성한다 (롤백 시 기존 트랜잭션까지 영향을 주지 않기 위함)

**MANDATORY**: 반드시 트랜잭션 안에서 실행해야 되며 트랜잭션이 없으면 예외를 던진다 (트랜잭션 안에서만 동작하도록 강제하고 싶을 때 사용함)

**NEVER**: 트랜잭션이 있으면 예외를 던진다 (트랜잭션 없이 실행되어야 하는 경우 사용함)

**NOT_SUPPORTED**: 트랜잭션을 중지하고 실행한다 (단순 조회 작업, 테스트에서 외부 트랜잭션을 적용하지 않고 싶을 때 사용함)

**SUPPORTS**: 트랜잭션이 있으면 참여하고, 없으면 트랜잭션 없이 실행한다

여기서 주로 사용되는 값은 REQUIRED, REQUIRES_NEW, NOT_SUPPORTED이다

REQUIRED 전파는 다음과 같이 단일 트랜잭션에 모든 메서드가 참여하는 구조로 동작한다

```text
REQUIRED 메서드 1: 트랜잭션 A 
    -> REQUIRED 메서드 2: 트랜잭션 A 
        -> REQUIRED 메서드 3: 트랜잭션 A
```

반면 REQUIRES_NEW 전파는 상위 트랜잭션을 일시 중지하고 새로운 트랜잭션을 생성하여 독립적으로 동작한다

```text
REQUIRED 메서드 1: 트랜잭션 A 
    -> REQUIRES_NEW 메서드 2: 트랜잭션 B 
        -> REQUIRES_NEW 메서드 3: 트랜잭션 C
```

NOT_SUPPORTED 전파는 트랜잭션을 중지하고 실행되며, 트랜잭션이 없는 상태로 동작한다

```text
REQUIRED 메서드 1: 트랜잭션 A 
    -> NOT_SUPPORTED 메서드 2: 트랜잭션 없음
                -> REQUIRED 메서드 3: 트랜잭션 A
```

## REQUIRED의 동작 방식과 롤백 처리

기본 전파인 REQUIRED 옵션을 사용하면 호출한 메서드와 호출된 메서드가 동일한 트랜잭션을 공유하게 된다

따라서 **트랜잭션을 공유하는 어딘가에서 롤백이 발생하면 전체 트랜잭션이 롤백된다**

만약 하위 메서드에서 예외가 발생하면 이미 트랜잭션 롤백이 이루어졌기 때문에 예외 전파를 막는다고 해도 REQUIRES_NEW 처럼 상위 트랜잭션의 롤백을 막을 수 없다

```java
// 내부 REQUIRED 전파는 외부와 동일한 트랜잭션을 사용한다
// 따라서 내부에서 예외가 발생하면 이미 롤백이 되었기 때문에 외부에서 예외를 처리해도 외부 트랜잭션도 롤백된다
@Transactional
public void requiredToRequiredAndInnerThrowsAndCatch() {
    try {
        logRepository.save(new TxLog("Outer REQUIRED"));
        innerService.innerRequiredThrows();
    } catch (RuntimeException ignored) {

    }
}

@Transactional
void innerRequiredThrows() {
    throw new RuntimeException("Inner Exception");
}
```

마찬가지로 상위 메서드에서 예외가 발생하면 하위 메서드로 전달되지 않지만, 트랜잭션이 동일하기 때문에 하위 메서드도 롤백된다

```java
// 외부의 예외가 하위 메서드에 전파되지 않지만 동일한 트랜잭션을 사용하기 때문에 하위 트랜잭션도 결국 롤백된다
@Transactional
public void requiredToRequiredAndOuterThrows() {
    logRepository.save(new TxLog("Outer REQUIRED"));
    innerService.innerRequired();
    throw new RuntimeException("Outer Exception");
}

@Transactional
void innerRequired() {
}
```


## REQUIRES_NEW의 동작 방식과 롤백 처리

[테스트 코드](../rdb/spring-data-jpa+querydsl/transaction/src/test/java/db/ninja/TransactionPropagationTest.java)

REQUIRES_NEW 전파는 기존 트랜잭션이 있는 경우 일시 중지하고 새로운 트랜잭션을 시작한다

일반적으로 메인 트랜잭션과 독립적으로 처리될 수 있으며 실패하더라도 영향을 주지 않아야 하는 작업에 사용된다

이메일 전송, 알림, 로그 저장, 외부 API 전송, 비즈니스 로직 상 부분 성공/실패를 허용하는 경우 등

기존 스레드 로컬에 할당된 커넥션 및 엔티티 매니저를 사용하지 않고 새로운 커넥션과 엔티티 매니저를 생성하여 독립적으로 동작한다

서로 다른 커넥션을 사용하면 물리적으로 분리되어 있어서 커밋/롤백도 독립적으로 진행된다

따라서 영속성 컨텍스트도 분리되어 서로 다른 상태를 유지하기 때문에 아래와 같이 하위 트랜잭션에서 상위 트랜잭션의 영속성 컨텍스트에 접근할 수 없다

```java
/*
    현재 트랜잭션에서 엔티티를 영속해도 내부 REQUIRES_NEW 전파는 별도의 영속성 컨텍스트를 가진다
    따라서 현재 트랜잭션에서 영속한 엔티티에 접근할 수 없다
 */
@Transactional
public List<TxLog> saveLogRequiredAndGetAllLogInnerRequiresNew() {
    TxLog saved = logRepository.save(new TxLog("Outer REQUIRED"));
    Long id = saved.getId();
    return innerService.getAllRequiresNew();
}

@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
List<TxLog> getAllRequiresNew() {
    return logRepository.findAll();
}
```

반대로 하위 트랜잭션에서 커밋한 엔티티는 이미 데이터베이스에 반영(flush)이 된 상태이기 때문에 상위 트랜잭션에서 조회할 수 있다

만약 예외가 발생해서 롤백이 된다면 접근할 수 없다

```java
// 내부 REQUIRES_NEW 트랜잭션이 커밋하면 flush된 시점이기 때문에 외부 트랜잭션에서 접근할 수 있다 (데이터베이스)
@Transactional(readOnly = true)
public List<TxLog> getLogsInSaveInnerRequiresNew() {
    innerService.innerRequiresNew();
    return logRepository.findAll();
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
void innerRequiresNew() {
    logRepository.save(new TxLog("Inner REQUIRES_NEW"));
}
```

트랜잭션 롤백은 **메서드가 종료하기 전 (트랜잭션 어노테이션이 커밋하기 전)** 예외가 발생하면 발생한다

REQUIRES_NEW 전파를 사용하면 상위 트랜잭션과 하위 트랜잭션이 독립적으로 동작하게 된다

다만, 비동기 메서드를 호출하여 트랜잭션을 분리하지 않는 이상 **하위 트랜잭션의 예외는 상위 트랜잭션의 롤백을 불러일으킬 수 있다**

예외와 트랜잭션을 구분해서 볼 필요가 있는데 일반적으로 예외는 호출된 메서드에서 처리하지 않으면 암묵적으로 호출한 메서드로 전파된다 (런타임 예외 기준)

따라서 트랜잭션을 분리한다고 하더라도 하위 메서드에서 예외가 발생하면 상위 메서드로 전파되어 상위 트랜잭션이 롤백된다

```java
@Transactional
public void requiredToRequiresNewAndInnerRollback() {
        logRepository.save(new TxLog("Outer REQUIRED"));
        innerService.innerRequiresNewThrows();
}

// 내부 REQUIRES_NEW 전파의 예외는 상위 메서드로 전파되어 상위 트랜잭션도 롤백시킬 수 있다
@Transactional(propagation = Propagation.REQUIRES_NEW)
void innerRequiresNewThrows() {
    throw new RuntimeException("Inner Exception");
}
```

반면 아래와 같이 상위 트랜잭션 메서드에서 하위 메서드의 예외를 잡아버리면 하위 트랜잭션만 롤백되고 상위 트랜잭션은 커밋된다

```java
// 내부 REQUIRES_NEW 전파의 예외가 상위 메서드로 전달될 때 처리하면 상위 트랜잭션은 롤백되지 않는다
@Transactional
public void requiredToRequiresNewAndInnerRollbackCatch() {
    logRepository.save(new TxLog("Outer REQUIRED"));
    try {
        innerService.innerRequiresNewThrows();
    } catch (Exception ignored) {
    }
}
```

반대로 상위 트랜잭션에서 발생한 예외는 하위 REQUIRES_NEW 트랜잭션에 어떤 영향을 줄까?

상위 메서드에서 발생한 예외가 하위 메서드로 전달되지 않기 때문에 분리된 하위 트랜잭션은 정상적으로 커밋되고, 상위 트랜잭션만 롤백된다

```java
// 외부에서 예외가 발생하더라도 내부 REQUIRES_NEW 전파는 별도의 트랜잭션으로 처리되므로 롤백되지 않는다
// 현재 트랜잭션만 롤백되고, 내부 REQUIRES_NEW 트랜잭션은 커밋된다
@Transactional
public void requiredToRequiredAndOuterRollback() {
    logRepository.save(new TxLog("Outer REQUIRED"));
    innerService.innerRequiresNew();

    throw new RuntimeException("Outer Exception");
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
void innerRequiresNew() {
}
```

REQUIRES_NEW를 중첩할수록 커넥션 수는 많아지며, 세밀한 트랜잭션 제어가 가능해진다

다만 커넥션 풀 부담이 증가하며 예외 처리 및 트랜잭션 관리가 복잡해질 수 있다


## 스프링의 롤백 처리 기준

스프링은 기본적으로 런타임 예외(unchecked exception)과 Error에 대해서만 트랜잭션을 자동으로 롤백한다

반면 체크 예외는 자동 롤백하지 않는다
- 런타임 예외: NullPointerException, IllegalArgumentException 등
- Error: OutOfMemoryError, StackOverflowError 등
- 체크 예외: IOException, SQLException 등

@Transactional의 rollbackFor 옵션을 사용하면 체크 예외도 자동 롤백할 수 있다

반대로 noRollbackFor 옵션을 사용하면 특정 예외에 대해서는 롤백하지 않도록 설정할 수 있다

```java
// 롤백되는 체크 예외
@Transactional(rollbackFor = IOException.class)
public void createUserWithCheckedExceptionWithRollback() throws IOException {
    userRepository.save(new User("test user"));

    throw new IOException();
}
```

```java
// 롤백되지 않는 언체크 예외
@Transactional(noRollbackFor = RuntimeException.class)
public void createUserWithUncheckedExceptionWithNoRollback() {
    userRepository.save(new User("test user"));

    throw new RuntimeException();
}
```

스프링의 롤백은 트랜잭션 프록시에서 동작하므로 트랜잭션 어노테이션이 적용된 메서드에서 발생한 예외에 대해서만 롤백이 적용된다

**메서드에서 예외를 잡아서 처리하면 트랜잭션 어노테이션이 감지할 수 없기 때문에 롤백되지 않는다는 것을 주의해야 한다**

그리고 일반적으로 비즈니스/커스텀 예외는 런타임 예외로 정의하여 트랜잭션 롤백을 유도한다