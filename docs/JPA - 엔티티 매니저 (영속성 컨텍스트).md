[영속성 컨텍스트](#영속성-컨텍스트)

[엔티티 매니저는 스레드 세이프하지 않다 !](#엔티티-매니저는-스레드-세이프하지-않다-)

[나는 인터페이스만 정의했는데 어떻게 DB를 쓸 수 있는거지 ? (리포지토리 인터페이스 구현 과정)](#나는-인터페이스만-정의했는데-어떻게-db를-쓸-수-있는거지--리포지토리-인터페이스-구현-과정)

[메서드 이름 기반 쿼리, @Query, Criteria API, Querydsl은 어떻게 실행되는 걸까](#메서드-이름-기반-쿼리-query-criteria-api-querydsl은-어떻게-실행되는-걸까)

[@PersistenceContext, @PersistenceUnit](#persistencecontext-persistenceunit)


## 영속성 컨텍스트

[영속성에 대한 위키백과 문서](https://en.wikipedia.org/wiki/Persistence_(computer_science))

[영속성 컨텍스트 설명 블로그](https://www.baeldung.com/jpa-hibernate-persistence-context)

[테스트 코드](../rdb/spring-data-jpa+querydsl/entity/src/test/java/db/ninja/entity_manager/PersistenceStateTest.java)

백엔드나 컴퓨터 과학을 학습하다보면 **영속(Persistence)** 이라는 단어를 줄곧 접하게 된다

영속 또는 영속성이란 단어의 의미는 영원히 계속되는 성질이나 능력을 의미한다

컴퓨터 과학에서의 영속은 프로세스가 만든 데이터를 데이터 스토리지에 저장하여 프로세스가 종료된 후에도 유지할 수 있게 하며, 프로세스와 스토리지 장치 간에 각각의 자료구조(객체, 레코드 등)로 매핑하여 데이터를 이동하는 것을 말한다 

이런 영속성 구현은 직교적 영속성과 비직교적 영속성이라는 두 가지 방법으로 구현할 수 있다
- 직교적 영속성(Orthogonal, Transparent)은 영속성이 실행 환경의 기본 특성으로 제공되어 개발자가 명시적으로 저장하거나 불러올 필요가 없는 것을 의미한다
- 비직교적 영속성(Non-Orthogonal)은 데이터를 저장하거나 불러올 때 코드로 명시적인 명령을 작성해야 하는 것을 말한다
- 즉 SQL을 써서 DB에 저장하고 꺼내는 방식이 비직교적 영속성이며 JPA는 이에 해당하여 엔티티 매니저의 메서드를 통해 영속성 컨텍스트에 엔티티를 저장하거나 불러오는 방식으로 동작한다

**영속성 컨텍스트**는 자바 프로그램의 데이터를 영속화하기 위해 자바(JPA)와 데이터베이스 간의 매핑을 관리하는 메모리 공간이다

데이터베이스 접근 비용이 크기 때문에 이를 최적화하고자 프로그램과 DB 사이에 **캐시 역할**을 하는 중간 계층을 두고 데이터베이스로부터 찾아온 데이터나 저장할 데이터를 관리한다

영속성 컨텍스트가 관리하는 데이터를 **엔티티**라고 부르며 이러한 엔티티를 조회, 변경, 삭제할 수 있는 기능을 제공한다 

**엔티티 매니저**는 영속성 컨텍스트와 상호작용할 수 있는 JPA의 인터페이스로, 내부적으로 영속성 컨텍스트를 사용하여 엔티티를 관리한다

영속성 컨텍스트를 사용하는 주요 목적은 데이터베이스 접근을 최소화하는 것이다. 이를 위해 1차 캐시, 엔티티 동일성 보장, 쓰기 지연, 변경 감지, 플러시 등의 기능을 제공한다

**1차 캐시**란 DB에 조회했거나 저장할 객체들을 모아두는 장소로 엔티티를 조회(find, getReference)하면 DB로 바로 접근하는 것이 아니라 영속성 컨텍스트의 캐시를 확인한다

만약 캐시에 없다면 DB에서 엔티티를 조회하고 영속성 컨텍스트에 저장한 후 반환한다. 이후 동일한 ID로 조회하면 영속성 컨텍스트의 캐시에서 엔티티를 반환한다

이는 영속성 컨텍스트가 동일한 트랜잭션 내에서 ID를 기반으로 엔티티를 추적하여 엔티티의 동일성을 보장할 수 있기 때문이다 (**엔티티 동일성(Identity) 보장**)

영속성 컨텍스트는 엔티티 매니저의 API(persist, remove 등)를 호출해도 즉각 DB에 반영하지 않고 내부 쿼리 저장소에 모아둔 다음, 트랜잭션의 커밋 또는 flush 시점에 한 번에 쿼리를 실행하는 특징을 가진다 (**쓰기 지연 (Write-Behind, Delayed-Write)**)

쓰기 지연은 성능 최적화에 도움을 주지만 플러시 타이밍을 잘못 관리하면 예상치 못한 DB 반영이 일어날 수 있다는 것을 유의해야 한다

영속성 컨텍스트는 엔티티의 동일성을 보장함과 동시에 **엔티티에 대한 스냅샷**을 남겨 엔티티의 변경 사항도 추적한다. 따라서 영속성 컨텍스트에 저장된 엔티티(영속 상태)의 변경된 필드가 트랜잭션 커밋 시점에 자동으로 감지되며 이에 대한 update 쿼리가 생성된다 (**변경 감지 (Dirty Checking)**)

영속성 컨텍스트가 DB에 변경 사항을 반영하는 시점을 **플러시(flush)**라고 하는데, 플러시는 `EntityManager.flush()`, 트랜잭션 커밋 시점이나 JPQL 실행 직전에 발생한다. 참고로 플러시를 해도 트랜잭션을 커밋하지 않으면 DB에 임시 반영 상태로만 존재한다

엔티티는 아래의 이미지와 같이 영속성 컨텍스트에 의해 관리되는 상태를 가진다 [이미지 출처](https://medium.com/javarevisited/spring-data-jpa-entity-lifecycle-model-c67fdae2d0c2)

![entity persistence lifecycle](../assets/entity-persistence-lifecycle.png)

NEW 상태는 새로 생성된 객체(엔티티)이며 아직 영속성 컨텍스트에 등록되지 않은 비영속 상태를 의미한다 (persist 호출 전)

MANAGED 상태는 find로 데이터베이스에서 불러오거나 persist를 호출하여 영속성 컨텍스트에 의해 관리되는 영속 상태를 의미한다 (persist는 엔티티를 영속성 컨텍스트에 등록하여 MANAGED 상태로 전이시키는 메서드로 이 시점에는 INSERT 쿼리가 나가지 않음)

DETACHED 상태는 clear, detach, close 등의 메서드로 영속성 컨텍스트로부터 분리된 상태를 의미하는데, 이 상태에 놓이면 위에서 언급한 영속성 컨텍스트의 기능을 사용할 수 없게 된다

REMOVED 상태는 remove 메서드로 데이터베이스에 삭제될 예정인 상태를 의미한다

엔티티 매니저의 `persist`, `merge`, `remove`, `detach`, `clear` 등의 메서드를 통해 엔티티의 상태를 변경할 수 있다

일반적으로 트랜잭션 범위 내에선 NEW 또는 MANAGED, REMOVED 상태로만 전이되고, 트랜잭션이 종료된 상태(트랜잭션이 없는 범위)의 엔티티는 사DETACHED 상태가 된다

```java
// 영속성 컨텍스트에 의해 관리되는 상태 (MANAGED)
User user = em.find(User.class, 1L); 

// 영속성 컨텍스트에서 분리한다 (DETACHED)
em.detach(user);

// 영속성 컨텍스트를 비운다 (모든 엔티티가 DETACHED 상태로 전이됨)
em.clear();
```

DETACHED 상태가 되면 엔티티가 메모리에 존재하지만 엔티티 매니저가 관리하지 않기 때문에 변경 감지, 플러시, 쿼리 생성, 1차 캐시, 지연 로딩 대상에서 제외된다

즉, 자바 객체로서의 상태를 변경하는 것은 가능하지만 영속성 컨텍스트에서 이를 추적하지 않기 때문에 DB에 반영되지 않는다

따라서 트랜잭션 내부에서만 엔티티를 조작하고 외부 계층에 전달하려면 DTO를 사용하는 것이 안전하다

```java
@Transactional
public User loadAndReturnUser() {
    // 트랜잭션 범위이므로 엔티티는 MANAGED가 된다
    return userRepository.findById(1L).orElseThrow(); 
}

/*
   다른 서비스나 컨트롤러에서 User 엔티티를 받아 변경한다
   이 User 엔티티는 트랜잭션 범위 밖에서 아용되므로 영속성 컨텍스트에 포함되지 않는다 (DETACHED 상태) 
   따라서 여기서 발생한 변경사항은 감지되지 않고 update 쿼리가 생성되지 않는다     
*/
public void updateName(User user) {
    user.setName("changed");
}
```

merge 메서드는 DETACHED 상태의 엔티티를 영속성 컨텍스트에 다시 연결하여 MANAGED 상태로 전이시키는 역할을 한다

merge 메서드의 동작은 다음과 같다
- 인자로 받은 DETACHED 엔티티의 식별자를 기준으로 현재 영속성 컨텍스트에 동일한 식별자를 가진 MANAGED 엔티티가 있는지 확인한다
- 있으면 그 영속 엔티티에 DETACHED 엔티티의 필드 값을 복사하여 업데이트한다 (덮어쓰기)
- 없으면 DB에서 조회한 다음 새로운 MANAGED 엔티티를 생성하고, DETACHED 엔티티의 필드 값을 복사하여 업데이트한다
- 그 다음 새로 생성한 MANAGED 엔티티를 반환하며 인자로 받은 DETACHED 객체는 여전히 DETACHED 상태로 남아있다

[테스트 코드](../rdb/spring-data-jpa+querydsl/entity/src/test/java/db/ninja/entity_manager/PersistenceStateTest.java)



여기서 포인트는 merge를 하게 되면 해당 엔티티의 값으로 기존 엔티티의 모든 값을 덮어쓴다는 것이다

실제로 엔티티 매니저 API를 호출하는 일은 드물겠지만 리포지토리 인터페이스를 사용하면 그 구현체인 SimpleJpaRepository의 `save(<T> entity)` 메서드에서 `entityManager.merge()`를 호출하게 된다

save 메서드의 `entityInformation.isNew(entity)`는 엔티티의 ID가 null이거나 0인 경우 새 엔티티로 판단한다

만약 영속 상태인 엔티티를 save 메서드로 전달하면 리포지토리 인터페이스는 기존 엔티티로 판단하고 merge를 호출하여 모든 필드 값을 덮어쓰게 된다

따라서 영속 상태의 엔티티를 save 메서드로 전달하는 것보다 변경 감지를 활용하는 방법이 더 안전하다

```java
@Transactional
public <S extends T> S save(S entity) {
    if (entityInformation.isNew(entity)) {
        entityManager.persist(entity);
        return entity;
    } 
    // 기존 엔티티인 경우 merge를 호출하여 모든 필드 값을 덮어쓴다
    else {
        return entityManager.merge(entity);
    }
}
```

영속성 컨텍스트의 범위는 트랜잭션의 제한 여부에 따라 달라진다

트랜잭션이 시작과 종료에 맞춰 생성 및 소멸되는 영속성 컨텍스트를 **트랜잭션 스코프 영속성 컨텍스트 (Transaction-scoped persistence context)**라고 한다

반면 트랜잭션이 없어도 유지되는 영속성 컨텍스트인 **확장 영속성 컨텍스트 (Extended persistence context)**는 여러 트랜잭션 동안 같은 컨텍스트를 공유할 수 있다 (플러시하려면 트랜잭션이 필요함)

여러가지 이유로 인해 스프링에선 기본적으로 트랜잭션 스코프를 사용(엔티티 매니저를 트랜잭션 범위 내에서만 사용)하고 확장 스코프를 거의 사용하지 않는다

스프링은 **무상태(stateless)**를 지향하여 각 요청에 독립적인 빈, 트랜잭션, 영속성 컨텍스트를 생성하고 처리한다 (싱글톤)

확장 스코프는 한 번 생성된 엔티티 매니저와 영속성 컨텍스트를 특정 세션에 걸쳐 오래 유지해야 하므로 상태를 가진 컴포넌트(stateful bean)를 필요로 하고 데이터베이스 커넥션을 낭비하게 된다

또한 트랜잭션 스코프는 @Transactional로 명확한 경계를 지을 수 있지만 확장 스코프는 하나의 영속성 컨텍스트가 여러 트랜잭션에 걸쳐 사용될 수 있으므로 트랜잭션 상태 추적이 어렵고 데이터 정합성(데이터가 일관된 상태를 유지하는 특성) 문제가 발생하기 쉽다 (사용자가 A 요청에서 엔티티를 수정하고 B 요청에서 커밋한다면 중간에 데이터가 변경되어도 감지되지 않거나 더티 체킹이 잘못 동작할 수 있음)

결국 상태 유지로 인해 복잡도와 관리 비용 문제가 증가하기 때문에 스프링은 트랜잭션 스코프 영속성 컨텍스트를 기본으로 사용한다


## 엔티티 매니저는 스레드 세이프하지 않다 !

@Transactional 어노테이션은 스프링에서 트랜잭션을 선언적으로 관리하기 위해 사용되며 트랜잭션 읽기 모드 전용, 트랜잭션 전파, 격리 수준 등 다양한 속성을 제공한다

관계형 데이터베이스와 상호작용할 때 트랜잭션 경계를 정의하고 커밋/롤백을 자동으로 처리한다

전통적인 스프링 부트 애플리케이션의 개발 워크플로우는 아래와 같이 리포지토리 인터페이스를 정의하고 서비스 클래스에서 해당 인터페이스를 주입받아 사용한다

```java
public interface UserRepository extends JpaRepository<User, Long> {
}
```

```java
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public Long createUser(String name) {
        User save = userRepository.save(new User(name));
        return save.getId();
    }
}
```

이 때 UserRepository 리포지토리 인터페이스의 구현체인 SimpleJpaRepository에 주입되는 엔티티 매니저의 실제 타입과 이 객체가 UserService의 createUser 메서드가 호출되는 시점에 어떻게 활용되는지 살펴보자

**전체 흐름 간단 요약**

```text
엔티티 매니저 주입: 스프링의 엔티티 매니저 프록시

요청 흐름 처리

[ HTTP 요청 / 디스패처서블릿 / 핸들러 매핑 ]
                ↓
[ @Transactional 메서드 / 트랜잭션 시작 / 트랜잭션 리소스 스레드 로컬 바인딩]
                ↓
        [ 비즈니스 로직 ]
                ↓
[ 엔티티 매니저 / 영속성 컨텍스트 / 1차 캐시 ]
                ↓
[ 트랜잭션 커밋 / 플러시 / 영속성 컨텍스트의 변경 사항 DB 반영 ]
                ↓
[ 트랜잭션 종료 / 영속성 컨텍스트 소멸 / 리소스 바인딩 해제 ]
                ↓
[ HTTP 응답 / 디스패처서블릿 / 핸들러 어댑터 ]
```

스프링의 핵심 철학 중 하나인 IoC는 객체 관리의 제어권을 개발자가 아닌 스프링 컨테이너가 가지는 것을 의미한다

그리고 개발자는 객체 간의 관계를 정의하여 런타임에 컨테이너로부터 객체를 주입받아 사용하는데, 이를 의존성 주입이라고 한다

의존성 주입은 객체 관계를 느슨한 결합을 만들어주는 특성이 있다. 이 점을 이용하면 의존성 대상을 실제 객체가 아닌 부가 기능을 수행하는 **프록시**로 주입할 수 있다

**엔티티 매니저는 트랜잭션 스코프에 묶인 객체로 내부 상태 (영속성 컨텍스트 등)를 보유하고 있기 때문에 여러 스레드에서 사용하면 안전하지 않다**

1차 캐시, 변경 감지 대상 목록, 쓰기 지연 SQL 저장소 등 이러한 상태를 가진 객체에서 동시성이 보장되지 않으면 플러시/커밋 타이밍 엉킴, 데이터 충돌 등의 문제가 발생할 수 있다

엔티티 매니저가 스프링 빈으로 등록되어 애플리케이션에서 전역적으로 사용되면 영속성 컨텍스트의 생명주기, 데이터베이스 커넥션 풀 관리 문제도 발생하게 된다  

따라서 스프링은 싱글톤, 멀티 스레딩 환경에서 **엔티티 매니저가 각 트랜잭션 스코프에 묶인 상태로 동작하도록 하기 위해 엔티티 매니저를 프록시로 주입한다**

리포지토리 인터페이스 구현체에 주입되는 엔티티 매니저의 프록시 객체는 `SharedEntityManagerBean` 클래스가 `shared` 필드에 보관한다

이 클래스가 초기화될 때 `SharedEntityManagerCreator.createSharedEntityManager()` 메서드를 호출하여 엔티티 매니저 프록시를 생성한다

생성된 엔티티 매니저 프록시는  `SharedEntityManagerCreator.SharedEntityManagerInvocationHandler` 타입이며 트랜잭션에 바인딩된 실제 엔티티 매니저에게 요청을 위임하는 역할을 한다

```java
// @PersistenceContext, EntityManager로 주입되는 객체를 제공하는 스프링 빈
// 프록시 객체를 필드로 보관하고 엔티티 매니저 주입이 필요한 대상에게 전달한다
public class SharedEntityManagerBean extends EntityManagerFactoryAccessor
        implements FactoryBean<EntityManager>, InitializingBean {

    @Nullable
    private EntityManager shared;

    @Override
    public final void afterPropertiesSet() {
        
        // 런타임에 트랜잭션에 바인딩된 엔티티 매니저에게 위임하는 프록시 객체를 생성한다
        // 실제 타입: SharedEntityManagerCreator.SharedEntityManagerInvocationHandler
        this.shared = SharedEntityManagerCreator.createSharedEntityManager(
                emf, getJpaPropertyMap(), this.synchronizedWithTransaction, this.entityManagerInterface);
    }

    // EntityManager를 주입받는 리포지토리 인터페이스 구현체는 이 메서드를 통해 엔티티 매니저 프록시를 주입받는다
    @Override
    @Nullable
    public EntityManager getObject() {
        return this.shared;
    }
    
}
```

```java
// 엔티티 매니저 대신 주입되는 프록시 객체
// SharedEntityManagerBean가 싱글톤으로 보관하고 있다
private static class SharedEntityManagerInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        
        // 프록시는 리포지토리 인터페이스의 엔티티 매니저 API 요청을 가로챈다
        // 스레드에 바인딩된 진짜 엔티티 매니저를 가져온 다음 요청을 위임한다
        EntityManager target = EntityManagerFactoryUtils.doGetTransactionalEntityManager(
                this.targetFactory, this.properties, this.synchronizedWithTransaction);

        Object result = method.invoke(target, args);
    }
}
```

**실제로 동작하는 엔티티 매니저는 @Transactional 메서드가 호출되면서 트랜잭션이 시작됨에 따라 JpaTransactionManager에 의해 생성되며 스레드 로컬(트랜잭션 동기화 매니저)에 바인딩된다**

엔티티 매니저 프록시는 현재 스레드에 바인딩된 엔티티 매니저를 가져와서 해당 엔티티 매니저에게 요청을 위임함으로써 영속성 컨텍스트와 DB 커넥션을 동시성 문제없이 안전하게 사용할 수 있도록 한다

```java
public class JpaTransactionManager extends AbstractPlatformTransactionManager {
    
    // 트랜잭션을 시작하는 메서드
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        // 트랜잭션 시작 시점에 엔티티 매니저를 생성한다
        EntityManager newEm = createEntityManagerForTransaction();
        
        // 트랜잭션에 엔티티 매니저 설정
        txObject.setEntityManagerHolder(new EntityManagerHolder(newEm), true);

        // 트랜잭션 동기화 매니저를 통해 현재 스레드에 엔티티 매니저를 바인딩한다
        TransactionSynchronizationManager.bindResource(
                obtainEntityManagerFactory(), txObject.getEntityManagerHolder());
    }
}
```

위의 JpaTransactionManager에서 createEntityManagerForTransaction 메서드는 엔티티 매니저 팩토리를 통해 새로운 엔티티 매니저를 생성한다

그렇다면 엔티티 매니저 팩토리는 어디서 생성될까?

엔티티 매니저 프록시 객체를 생성하는 SharedEntityManagerCreator 클래스처럼 엔티티 매니저 팩토리는 **LocalContainerEntityManagerFactoryBean**을 통해 생성된다

LocalContainerEntityManagerFactoryBean 클래스는 PersistenceUnitInfo와 JpaVendorAdapter를 사용해 **JPA 구현체와 통합**하고  DataSource, JtaTransactionManager, JPA 프로퍼티 등의 **스프링 자원들과 통합**하여 엔티티 매니저 팩토리를 생성한 뒤 컨텍스트에 등록시킨다

즉, 스프링과 JPA의 연결고리, 엔티티 매니저 팩토리의 생명주기 관리, JPA 구현체(하이버네이트)의 설정 자동화, 스프링이 제공하는 트랜잭션 및 DataSoucre와의 통합을 담당한다

LocalContainerEntityManagerFactoryBean 자체는 스프링 부트의 자동 설정 HibernateJpaAutoConfiguration에 의해 스프링 빈으로 등록되며, 스프링 빈 초기화 메서드에서 엔티티 매니저 팩토리를 생성한다

```text
                 [ApplicationContext]
                         │
                         ▼
      [LocalContainerEntityManagerFactoryBean] ← @Bean 또는 Spring Boot Auto Config
                         │
         ┌───────────────┼────────────────┐
         ▼               ▼                ▼
  DataSource       JpaVendorAdapter    JPA 설정(properties)
         │               │                │
         └───────→ EntityManagerFactory ◀─┘
                         │
                         ▼
                 EntityManager (런타임에 트랜잭션 시작 시 생성됨)
```

지금까지 살펴본 내용인 리포지토리 인터페이스 구현체에 주입되는 엔티티 매니저 프록시, 요청 위임, 엔티티 매니저 팩토리 등에 대한 전체적인 흐름을 정리하면 다음과 같다

```text
엔티티 매니저 팩토리 생성 과정

[ 스프링 부트 자동 구성 / HibernateJpaAutoConfiguration ]
                         ↓
[ LocalContainerEntityManagerFactoryBean ]
                         ↓         
[ DataSource, JpaVendorAdapter, JPA 설정(properties) ]
                         ↓
             [ EntityManagerFactory]



엔티티 매니저 프록시 생성 과정

[ SharedEntityManagerFactoryBean ]
                         ↓
[ SharedEntityManagerCreator.createSharedEntityManager() ]
                         


엔티티 매니저 프록시 주입 과정

[ 리포지토리 인터페이스 구현체 (SimpleJpaRepository) ]
                         ↓
[ 엔티티 매니저 프록시 주입 (SharedEntityManagerInvocationHandler) ]



엔티티 매니저 동작 과정

[ @Transactional 메서드 호출 ]
                    ↓
[ 트랜잭션 시작 / JpaTransactionManager.doBegin() ]
                    ↓
[ 엔티티 매니저 및 영속성 컨텍스트 생성 / 트랜잭션 동기화 매니저에 바인딩 ]
                    ↓
[ 리포지토리 인터페이스 메서드 호출 ]
                    ↓    
[ 리포지토리 구현체의 엔티티 매니저 메서드 호출 ]
                    ↓
[ 엔티티 매니저 프록시 객체의 요청 가로챔 ]
                    ↓
[ 현재 스레드에 바인딩된 엔티티 매니저에 요청 위임 ]
                    ↓
[ 영속성 컨텍스트에 접근하여 엔티티 조회/저장 등 작업 수행 ]
                    ↓
[ 트랜잭션 커밋 / 플러시 / 영속성 컨텍스트의 변경 사항 DB 반영 ]
                    ↓
[ 트랜잭션 종료 / 엔티티 매니저 및 영속성 컨텍스트 스레드 바인딩 해제 ]
```

참고로 Querydsl을 통해 쿼리를 생성할 때 사용되는 JPAQueryFactory도 엔티티 매니저를 필요로 하는데 이 때 주입되는 것도 위에서 설명한 SharedEntityManagerInvocationHandler 프록시 객체이다

```java
@Bean
public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
    return new JPAQueryFactory(entityManager);
}
```


## 나는 인터페이스만 정의했는데 어떻게 DB를 쓸 수 있는거지 ? (리포지토리 인터페이스 구현 과정)

스프링 데이터는 리포지토리 인터페이스만 정의하면 데이터베이스 상호작용을 수행할 수 있는 기능을 제공한다

아래의 코드처럼 User 엔티티에 대한 영속 작업을 담당하는 인터페이스만 정의해도 User 엔티티를 저장하고 조회하는 기능을 사용할 수 있다

단순한 쿼리가 아닌 경우엔 쿼리 메서드나 JPQL로 처리할 수 있는데, 이 부분은 쿼리 작성의 난이도를 떠나서 메서드를 정의하는 것만으로도 데이터베이스 상호작용을 할 수 있다는 점에서 정말 편리하다 (다만 Criteria API나 Querydsl을 사용하면 편의성이 어느정도 희생된다)

```java
public interface UserRepository extends JpaRepository<User, Long> {} 
```

정말 간편하고 생산성을 대폭 높여주는 기능이지만 스프링 특성상 추상화로 인해 많은 것들이 물밑에서 이뤄지기 때문에 이러한 일들이 마법같이 느껴지기만 한다

이번 섹션은 런타임에 스프링이 어떤 절차를 거쳐서 리포지토리 인터페이스를 구현하는지 살펴본 뒤 다음 섹션에서 이러한 리포지토리 인터페이스를 기반으로 사용자 정의 쿼리가 어떻게 동작하는 건지 살펴볼 것이다

이걸 안다고 크게 달라질 건 없지만 ... 스프링 데이터가 어떻게 리포지토리 인터페이스를 구현하고, 엔티티 매니저 프록시를 생성하여 트랜잭션 스코프에서 안전하게 동작하는지 이해하는 데 도움이 될 수 있다

**어지러움 주의**

리포지토리 인터페이스 구현은 스프링 부트의 자동 구성을 담당하는 JpaRepositoriesAutoConfiguration 클래스의 JpaRepositoriesRegistrar 임포트로 인해 동작한다

JpaRepositoriesRegistrar 클래스가 리포지토리 구현체 등록을 담당하는 중요한 객체로 다음과 같은 상속 관계도를 가진다

```text
ImportBeanDefinitionRegistrar
└── AbstractRepositoryConfigurationSourceSupport
        └── JpaRepositoriesRegistrar
```

JpaRepositoriesRegistrar가 간접적으로 확장하는 ImportBeanDefinitionRegistrar 인터페이스는 애플리케이션 초기화 시점에 추가적인 빈 정의(BeanDefinition)를 컨텍스트에 등록하는 역할을 한다

AbstractRepositoryConfigurationSourceSupport 추상 클래스는 **스프링 데이터 리포지토리의 자동 구성을 지원하기 위해 리포지토리의 구성을 추상화**한 클래스이다 (JPA 뿐만 아니라 레디스, 몽고 등 다른 스프링 데이터 모듈도 공통적으로 지원한다)

또한 ImportBeanDefinitionRegistrar의 메서드를 구현하여 런타임에 리포지토리 인터페이스에 대한 빈 정의를 추가적으로 등록하는 작업을 RepositoryConfigurationDelegate 클래스에 위임한다

즉, JpaRepositoriesRegistrar가 컨텍스트에 로드되면서 부모 클래스인 AbstractRepositoryConfigurationSourceSupport의 빈 정의 추가 작업이 트리거 되어 리포지토리 인터페이스 구현체 생성 및 빈 등록이 이뤄진다

```java
// JPA 리포지토리 인터페이스 구현체 생성 및 스프링 빈 등록 수행
// 정확히는 부모 클래스의 AbstractRepositoryConfigurationSourceSupport에서 작업들이 이뤄지고 필요한 정보들을 반환하는 역할을 한다 
class JpaRepositoriesRegistrar extends AbstractRepositoryConfigurationSourceSupport {

    // JpaRepositoryConfigExtension: JPA에 관한 확장 설정 (엔티티 매니저 프록시 등록 등)
    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new JpaRepositoryConfigExtension();
    }

    @EnableJpaRepositories
    private static final class EnableJpaRepositoriesConfiguration {
    }

}
```

```java
// 리포지토리 구성을 추상화한 클래스로 JPA 뿐만 아니라 다른 스프링 데이터의 모듈도 공통적으로 지원한다
// 또한 빈 정의 추가 작업을 RepositoryConfigurationDelegate에 위임한다
public abstract class AbstractRepositoryConfigurationSourceSupport
		implements ImportBeanDefinitionRegistrar {

    
    // 런타임에 리포지토리 인터페이스에 대한 빈 정의를 추가적으로 등록하는 메서드 (ImportBeanDefinitionRegistrar 구현)
    // RepositoryConfigurationDelegate 클래스에 위임한다
    @Override
    public void registerBeanDefinitions(...) {
        delegate.registerRepositoriesIn(registry, getRepositoryConfigurationExtension());
    }

    // 구성 정보 추상화
    protected abstract Class<?> getConfiguration();
    protected abstract RepositoryConfigurationExtension getRepositoryConfigurationExtension();
    
}
```

RepositoryConfigurationDelegate 클래스는 리포지토리 인터페이스를 스캔한 후, 스캔 목록을 기반으로 팩토리 빈(JpaRepositoryFactoryBean 등)을 지정하여 리포지토리 인터페이스에 대한 프록시 객체와 SimpleJpaRepository가 생성되도록 한다

또한 스프링 데이터 모듈 별로 특정 설정을 적용하기 위해 RepositoryConfigurationExtension 인터페이스를 구현한 클래스를 사용하는데, 이 시점에 JpaRepositoriesRegistrar가 반환한 JpaRepositoryConfigExtension 클래스가 사용되어 엔티티 매니저 프록시 등록 등의 작업을 수행한다

```java
public class RepositoryConfigurationDelegate {

    
    public List<BeanComponentDefinition> registerRepositoriesIn(BeanDefinitionRegistry registry,
                                                                RepositoryConfigurationExtension extension) {

        // 데이터 모듈마다 필수적인 빈들을 등록한다
        // JPA의 경우 엔티티 매니저 프록시, JPA 어노테이션 처리기 등을 등록한다
        extension.registerBeansForRoot(registry, configurationSource);
        
        // 익스텐션을 통해 리포지토리 인터페이스를 스캔하고 설정 정보를 가져온다 (@Repository, 커스텀 Impl 등)
        Collection<RepositoryConfiguration<RepositoryConfigurationSource>> configurations = extension
                .getRepositoryConfigurations(...);

        // 스캔된 각 리포지토리 인터페이스에 대한 빈 정의를 생성하고 등록한다
        for (RepositoryConfiguration<? extends RepositoryConfigurationSource> configuration : configurations) {

            // 구성 정보를 기반으로 리포지토리 인터페이스의 빈 정의를 생성한다
            // 커스텀 리포지토리 인터페이스에 대한 정보(RepositoryFragments)도 함께 생성한다
            // 이 정보는 프록시가 커스텀 리포지토리 메서드를 처리할 때 사용된다
            BeanDefinitionBuilder definitionBuilder = builder.build(configuration);
            
            // 리포지토리에 대한 후처리
            extension.postProcess(definitionBuilder, configurationSource);
            
            // 리포지토리 팩토리 빈을 지정한다 (JpaRepositoryFactoryBean 등)
            // 리포지토리 인터페이스 구현체는 이 팩토리 빈을 통해 생성된다
            RootBeanDefinition beanDefinition = (RootBeanDefinition) definitionBuilder.getBeanDefinition();
            beanDefinition.setTargetType(getRepositoryFactoryBeanType(configuration));

            // 생성한 빈 정의를 스프링 컨테이너에 등록한다
            registry.registerBeanDefinition(beanName, beanDefinition);
            
            definitions.add(new BeanComponentDefinition(beanDefinition, beanName));
        }

        // 스프링 네이티브 AOP 컴파일 환경 지원 메서드
        registerAotComponents(registry, extension, metadataByRepositoryBeanName);
        return definitions;
    }
}
```

RepositoryConfigurationExtension 인터페이스는 스프링 데이터 모듈 별로 리포지토리에 대해 확장 설정을 할 수 있도록 한다

JPA를 사용하면 JpaRepositoryConfigExtension 클래스가 사용되며 이 곳에서 SharedEntityManagerCreator를 통해 엔티티 매니저 프록시를 빈으로 등록하거나, 리포지토리 팩토리 빈에 대한 후처리 작업 등을 수행한다 

```java
public class JpaRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {

    // 엔티티 매니저 프록시를 빈으로 등록하는 메서드 (ShraredEntityManagerCreator에게 위임한다)
    // jpaSharedEM_entityManagerFactory라는 이름으로 등록한다
    private String registerSharedEntityMangerIfNotAlreadyRegistered(BeanDefinitionRegistry registry,
                                                                    RepositoryConfigurationSource config) {

        if (!registry.containsBeanDefinition(entityManagerBeanName)) {
            AbstractBeanDefinition entityManager = getEntityManagerBeanDefinitionFor(config, null);
            registry.registerBeanDefinition(entityManagerBeanName, entityManager);
        }

        entityManagerRefs.put(config, entityManagerBeanName);
        return entityManagerBeanName;
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {
        builder.addPropertyReference("entityManager", entityManagerRefs.get(source));
    }
}
```

리포지토리 인터페이스 빈 정의 등록 과정에서 지정된 팩토리 빈은 데이터 모듈 별로 리포지토리 인터페이스 구현체를 생성하기 위한 어댑터 역할을 한다

JPA는 JpaRepositoryFactoryBean 클래스가 사용되며, 이 클래스는 JPA 리포지토리를 생성하는 JpaRepositoryFactory 클래스를 생성한다 

```java
public class JpaRepositoryFactoryBean<T extends JpaRepository<S, ID>, S, ID>
        extends JpaRepositoryFactoryBeanSupport<T, S, ID> {

    // JPA 리포지토리를 생성하는 JpaRepositoryFactory를 생성한다
    // 이 때 EntityManager는 SharedEntityManagerCreator를 통해 생성된 엔티티 매니저 프록시가 주입된다
    // 엔티티 매니저 프록시는 이전 과정인 JpaRepositoryConfigExtension에서 jpaSharedEM_entityManagerFactory 이라는 이름으로 등록된다
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        JpaRepositoryFactory jpaRepositoryFactory = new JpaRepositoryFactory(entityManager);
        return jpaRepositoryFactory;
    }
}
```

그리고 JpaRepositoryFactory에서 리포지토리 인터페이스의 구현체(SimpleJpaRepository)가 리플렉션을 통해 생성된다

```java
public class JpaRepositoryFactory extends RepositoryFactorySupport {

    // 리포지토리 인터페이스에 대한 구현체인 SimpleJpaRepository를 생성한다
    // 주입되는 엔티티 매니저는 SharedEntityManagerCreator를 통해 생성된 프록시 객체이다
    protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information,
                                                                    EntityManager entityManager) {

        JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());
        Object repository = getTargetRepositoryViaReflection(information, entityInformation, entityManager);

        return (JpaRepositoryImplementation<?, ?>) repository;
    }
}
```

스프링은 쿼리 메서드, JPQL, Criteria API, Querydsl 등 다양한 기능을 추가적으로 지원하는데 이러한 작업들은 리포지토리 인터페이스에 정의된 메서드 이름이나 쿼리 등을 분석하고 생성하는 작업을 필요로 한다

그러기 위해선 부가 기능을 가진 객체가 필요하며 리포지토리 인터페이스 구현체의 요청을 가로채서 처리해야 하므로 프록시로써 동작해야 한다

이 프록시 객체를 생성하는 클래스는 JpaRepositoryFactory의 부모인 RepositoryFactorySupport이며 `getRepository` 메서드에서 리포지토리 인터페이스의 구현체(SimpleJpaRepository)를 생성한 후, 이 구현체를 프록시로 감싸서 반환한다

```text
   [ 리포지토리 인터페이스 ]
            ↓                            
[ SimpleJpaRepository 프록시 ]
            ↓
[ SimpleJpaRepository 인스턴스]
```

RepositoryFactorySupport는 템플릿 메서드 패턴을 통해 리포지토리 인터페이스의 구현체를 추상화하고 이를 기반으로 스프링 데이터에서 제공하는 편의 기능을 적용한 프록시를 생성한다

RepositoryFactorySupport는 JPA 뿐만 아니라 다른 스프링 데이터 모듈에서도 사용할 수 있도록 추상화되어 있으며 각 모듈에서 사용하는 리포지토리 구현체에 대한 프록시를 통해 해당 모듈이 편의 기능을 지원할 수 있게 한다

**최종적으로 SimpleJpaRepository를 감싼 프록시가 해당 인터페이스 타입으로 스프링 빈이 되며**, 이름은 기본적으로 인터페이스 이름을 카멜 케이스로 변환한 형태가 된다 (UserRepository -> userRepository)

```java
public abstract class RepositoryFactorySupport {

    // 리포지토리 인터페이스 구현체를 프록시로 감싸는 메서드
    public <T> T getRepository(Class<T> repositoryInterface, RepositoryFragments fragments) {

        // JpaRepositoryFactory는 이 메서드를 통해 구현체인 SimpleJpaRepository를 생성한다
        Object target = getTargetRepository(information);
        
        // 리포지토리 구현체를 대상으로 하는 프록시 생성
        // 스프링 데이터 JPA는 이 프록시를 통해 쿼리 메서드, JPQL, Criteria API, Querydsl 등의 기능을 제공한다
        ProxyFactory result = new ProxyFactory();
        result.setTarget(target);
        result.setInterfaces(repositoryInterface, Repository.class, TransactionalProxy.class);
        
        
        // 프록시가 쿼리 메서드, JPQL 등을 처리할 수 있도록 쿼리 조회 전략 객체와 AOP 어드바이스를 추가한다
        // JPA의 경우 JpaQueryLookupStrategy.CreateIfNotFoundQueryLookupStrategy가 사용된다
        Optional<QueryLookupStrategy> queryLookupStrategy = getQueryLookupStrategy(queryLookupStrategyKey,
                new ValueExpressionDelegate(
                        new QueryMethodValueEvaluationContextAccessor(getEnvironment(), evaluationContextProvider), VALUE_PARSER));
        
        // QueryExecutorMethodInterceptor: 메서드 이름 기반 쿼리, @Query를 처리하는 어드바이스
        result.addAdvice(new QueryExecutorMethodInterceptor(information, getProjectionFactory(), queryLookupStrategy,
                namedQueries, queryPostProcessors, methodInvocationListeners));

        // ImplementationMethodExecutionInterceptor: 커스텀 리포지토리 메서드를 처리하는 어드바이스
        // RepositoryConfigurationDelegate.repositoriesIn 메서드에서 생성한 RepositoryFragments를 통해 처리한다
        result.addAdvice(
                new ImplementationMethodExecutionInterceptor(information, compositionToUse, methodInvocationListeners));
        
        T repository = (T) result.getProxy();
        
        return repository;
    }
}
```

지금까지의 리포지토리 인터페이스 구현체 생성 및 등록 과정을 정리하면 다음과 같다

```text

[ 스프링 부트 @JpaRepositoriesAutoConfiguration ]
                         ↓
[ JpaRepositoriesRegistrar (ImportBeanDefinitionRegistrar) ]
                         ↓
[ AbstractRepositoryConfigurationSourceSupport (리포지토리 인터페이스 구현 추상화) ]
                         ↓
[ RepositoryConfigurationDelegate (리포지토리 인터페이스 스캔 및 빈 정의 등록) ]
                         ↓
[ JpaRepositoryConfigExtension (JPA 익스텐션 설정, 엔티티 매니저 프록시 등록) ]
                         ↓
[ JpaRepositoryFactoryBean (JPA 리포지토리 팩토리 빈 생성) ]
                         ↓
[ JpaRepositoryFactory (리포지토리 인터페이스 구현체 생성) ]
                         ↓
[ SimpleJpaRepository (리포지토리 인터페이스 구현체, 엔티티 매니저 프록시 주입) ]
                         ↓
[ RepositoryFactorySupport (SimpleJpaRepository를 감싸는 프록시 생성) ]
```

그리고 이렇게 등록된 리포지토리 인터페이스 구현체는 다음과 같은 흐름으로 사용된다

```text
             [ @Transactional 메서드 호출 ]
                           ↓
    [ 트랜잭션 시작 / JpaTransactionManager.doBegin() ]
                           ↓
[ 엔티티 매니저 및 영속성 컨텍스트 생성, 트랜잭션 동기화 매니저에 바인딩 ]
                           ↓
                    [ 서비스 객체 ]
                           ↓
                 [ 리포지토리 인터페이스 ]
                           ↓
[ 리포지토리 인터페이스 프록시 (쿼리 메서드, JPQL, Querydsl 등 처리)]
                           ↓
      [ SimpleJpaRepository (리포지토리 인터페이스 구현체) ]
                           ↓
[ 엔티티 매니저 프록시 (SharedEntityManagerInvocationHandler) ]
                           ↓
[ 트랜잭션 동기화 매니저에 바인딩된 엔티티 매니저 (트랜잭션 시작 시 생성됨) ]
                           ↓
           [ 영속성 컨텍스트 (1차 캐시, 변경 감지 등) ]
```

### 리포지토리 인터페이스 초기화 과정에서 커스텀할 수 있는 부분


## 메서드 이름 기반 쿼리, @Query, Criteria API, Querydsl은 어떻게 실행되는 걸까

리포지토리 인터페이스를 정의하는 것만으로는 단순한 CRUD만 가능하기에 좀 더 복잡한 쿼리가 필요하다면 상황에 따라 쿼리 메서드, JPQL, Criteria API, Querydsl 등을 이용해야 한다

이들은 내부적으로 어떻게 동작하길래 쿼리를 생성하고 실행할 수 있는걸까?

아래의 코드는 name 필드를 기반으로 한 User 엔티티 조회를 각 방법 별로 구현한 예시 코드이다

참고로 Criteria API를 서비스 객체나 DAO 클래스에 정의하지 않고 Querydsl처럼 커스텀 리포지토리를 활용한다고 가정한다 

```text
// 쿼리 메서드
Optional<User> findByName(String name);


// JPQL
@Query("Select u from User u where u.name = :name")


// Criteria API
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<User> query = cb.createQuery(User.class);
Root<User> userRoot = query.from(User.class);
query.select(userRoot).where(cb.equal(userRoot.get("name"), name));
User found = em.createQuery(query).getSingleResult();


// Querydsl (JPAQueryFactory)
QUser user = QUser.user;
query
    .selectFrom(user)
    .where(user.name.eq(name))
    .fetchOne();
```

[이전 섹션](#나는-인터페이스만-정의했는데-어떻게-db를-쓸-수-있는거지--리포지토리-인터페이스-구현-과정)에서 살펴보았듯이 리포지토리 인터페이스를 정의하면 스프링 데이터는 이에 대한 SimpleJpaRepository(구현체)와 프록시를 생성한다

이 프록시 객체가 서비스 객체의 리포지토리 인터페이스 메서드 호출을 가로채서 쿼리 메서드, JPQL, Criteria API, Querydsl 등의 요청을 처리한다

그 뿐만 아니라 데이터베이스에서 발생한 예외를 스프링 데이터 접근 예외로 변환하는 작업, 트랜잭션 처리 등의 부가 기능도 제공한다

```text
   [ 리포지토리 인터페이스 ]
            ↓
       [ 프록시 객체 ]
            ↓
[ 메서드 종류에 따라 처리 분기 / 예외 및 트랜잭션 처리 ]
            ↓
  [ EntityManager 프록시 ]
```

이러한 기능을 제공하기 위해 각 리포지토리 인터페이스에 대한 프록시를 생성할 때 AOP 어드바이스를 적용한다 

기본적으로 적용되는 AOP 어드바이스 목록 (호출되는 순서로 나열됨)
- CrudMethodMetadataPopulatingMethodInterceptor
- PersistenceExceptionTranslationInterceptor
- TransactionInterceptor
- DefaultMethodInvokingMethodInterceptor
- QueryExecutorMethodInterceptor
- ImplementationMethodExecutionInterceptor

이제 각각의 어드바이스가 어떤 역할을 수행하는지 살펴보자

**CrudMethodMetadataPopulatingMethodInterceptor**는 메서드 호출 시 @Lock, @QueryHints, @Modifying 등의 메타데이터를 읽어 CrudMethodMetadata에 저장하여 후속 처리(트랜잭션/쿼리 실행 등)가 참고할 수 있게 한다

**PersistenceExceptionTranslationInterceptor**는 JPA, 하이버네이트에서 발생하는 예외를 스프링 데이터 접근 예외(DataAccessException)로 변환하여 서비스 객체가 예외를 처리할 수 있도록 한다

**TransactionInterceptor**는 @Transactional 어노테이션에 따라 트랜잭션 시작/커밋/롤백을 제어한다 (트랜잭션 매니저에게 위임하여 트랜잭션 경계를 관리한다)

**DefaultMethodInvokingMethodInterceptor**는 자바 8 이상에서 사용할 수 있는 default 메서드를 실행하는 어드바이스로, 프록시 기반 구조에서 default 메서드를 호출할 수 있도록 한다

**QueryExecutorMethodInterceptor**는 리포지토리 인터페이스에 정의된 쿼리 메서드, @Query, NamedQuery 등 스프링 데이터가 파싱/해석하여 생성한 쿼리를 실행한다

**ImplementationMethodExecutionInterceptor**는 커스텀 리포지토리 구현체의 메서드를 실행하는 어드바이스이다

프록시 객체는 QueryExecutorMethodInterceptor와 ImplementationMethodExecutionInterceptor을 이용하여 종류에 따라 쿼리 메서드, JPQL, Criteria API, Querydsl 등의 요청을 처리한다

**메서드 이름 기반 쿼리**와 **@Query**는 **QueryExecutorMethodInterceptor**가 메서드에 매핑된 쿼리를 실행하고 결과를 반환한다

이 때 SimpleJpaRepository를 거치지 않고 EntityManager 프록시를 통해 영속성 컨텍스트에 접근하게 된다

그렇다면 QueryExecutorMethodInterceptor는 어떻게 메서드 이름 기반 쿼리와, @Query 쿼리를 처리할까?

각 프록시에 대한 QueryExecutorMethodInterceptor가 생성되는 시점에 해당 리포지토리 인터페이스에 정의된 메서드를 분석하여 메서드명 기반 쿼리, @Query 쿼리 객체(RepositoryQuery)를 생성한다

이 쿼리 객체들을 리포지토리 인터페이스 메서드와 매핑하여 보관하고 있다가 리포지토리 인터페이스 메서드가 호출되면 해당 메서드에 매핑된 쿼리 객체를 실행하여 결과를 반환하는 메커니즘이다

```java
// 리포지토리 인터페이스 구현체인 SimpleJpaRepository를 감싼 프록시를 생성하는 클래스 
public abstract class RepositoryFactorySupport {
    
    public <T> T getRepository(...) {
        
        // 각 리포지토리 인터페이스 프록시마다 QueryExecutorMethodInterceptor를 생성/적용한다 
        // JPA를 사용하면 queryLookupStrategy 타입은 JpaQueryLookupStrategy.CreateIfNotFoundQueryLookupStrategy가 된다
        result.addAdvice(new QueryExecutorMethodInterceptor(information, getProjectionFactory(), queryLookupStrategy,
                namedQueries, queryPostProcessors, methodInvocationListeners));
    }
    
}
```

QueryExecutorMethodInterceptor는 CreateIfNotFoundQueryLookupStrategy를 통해 메서드에 대한 쿼리(RepositoryQuery)를 생성한다

이후 생성된 쿼리와 메서드를 매핑하여 보관하고 있다가 리포지토리 인터페이스 메서드가 호출되면 해당 메서드에 매핑된 쿼리를 실행하고 결과를 반환한다

```java
class QueryExecutorMethodInterceptor implements MethodInterceptor {

    // 생성자에서 생성한 쿼리 객체를 메서드와 매핑하여 보관한다
    private final Map<Method, RepositoryQuery> queries;
    
    public QueryExecutorMethodInterceptor(...) {

        // 생성자에서 mapMethodsToQuery 메서드를 통해 리포지토리 인터페이스에 정의된 메서드들을 분석하여 쿼리 객체를 생성한다
        this.queries = queryLookupStrategy
                .map(it -> mapMethodsToQuery(repositoryInformation, it, projectionFactory))
                .orElse(Collections.emptyMap());
    }

    // 리포지토리에 정의된 메서드들을 분석하여 쿼리 객체를 생성하고 메서드와 매핑한다
    private Map<Method, RepositoryQuery> mapMethodsToQuery(...) {

        List<Method> queryMethods = repositoryInformation.getQueryMethods().toList();
        Map<Method, RepositoryQuery> result = new HashMap<>(queryMethods.size(), 1.0f);

        for (Method method : queryMethods) {

            Pair<Method, RepositoryQuery> pair = lookupQuery(method, repositoryInformation, lookupStrategy,
                    projectionFactory);
            result.put(pair.getFirst(), pair.getSecond());
        }

        return result;
    }

    // QueryLookupStrategy를 통해 메서드에 매핑된 쿼리 객체를 생성한다
    // JPA의 경우 CreateIfNotFoundQueryLookupStrategy가 사용된다
    private Pair<Method, RepositoryQuery> lookupQuery(...) {
        try {
            return Pair.of(method, strategy.resolveQuery(method, information, projectionFactory, namedQueries));
        } catch (QueryCreationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw QueryCreationException.create(e.getMessage(), e, information.getRepositoryInterface(), method);
        }
    }
}
```

JpaQueryLookupStrategy.CreateIfNotFoundQueryLookupStrategy 클래스는 **DeclaredQueryLookupStrategy**와 **CreateQueryLookupStrategy**를 가지고 있다

먼저 리포지토리 메서드에 대해 DeclaredQueryLookupStrategy를 이용하여 @Query 쿼리 파싱을 시도한다. DeclaredQueryLookupStrategy는 네이티브 쿼리인 경우 NativeQuery를, JPQL 쿼리인 경우 **SimpleJpaQuery**로 만든 후 **AbstractJpaQuery**로 반환하다

만약 실패한 경우 CreateQueryLookupStrategy를 이용하여 메서드 이름으로 쿼리 객체를 생성한다. CreateQueryLookupStrategy는 **PartTreeJpaQuery**를 반환한다

즉, @Query를 사용하면 SimpleJpaQuery가 생성되고 메서드 이름 기반 쿼리는 PartTreeJpaQuery가 생성된다

```java
public final class JpaQueryLookupStrategy {
    
    private static class CreateIfNotFoundQueryLookupStrategy {

        // @Query 쿼리 파싱
        private final DeclaredQueryLookupStrategy lookupStrategy;
        
        // 메서드 이름 기반 쿼리 파싱
        private final CreateQueryLookupStrategy createStrategy;
        
        @Override
        protected RepositoryQuery resolveQuery(JpaQueryMethod method, QueryRewriter queryRewriter, EntityManager em,
                                               NamedQueries namedQueries) {

            RepositoryQuery lookupQuery = lookupStrategy.resolveQuery(method, queryRewriter, em, namedQueries);

            if (lookupQuery != NO_QUERY) {
                return lookupQuery;
            }

            return createStrategy.resolveQuery(method, queryRewriter, em, namedQueries);
        }
    }
}
```

커스텀 리포지토리 인터페이스(Criteria API, Querydsl)의 경우엔 메서드마다 쿼리 객체를 만드는 대신 RepositoryComposition(RepositoryFragments)를 기반으로 ImplementationMethodExecutionInterceptor가 처리한다

스프링 데이터에서 하나의 리포지토리 인터페이스는 다음과 같이 여러 인터페이스를 확장할 수 있다

```java
public interface UserRepository extends JpaRepository<User, Long>, UserQuerydslRepository, UserCriteriaRepository {
    
}
```

이 때 프록시 객체는 메서드 호출에 따라 적절한 구현체에게 위임해야 한다

JpaRepository 관련 메서드는 SimpleJpaRepository에게, UserQuerydslRepository 관련 메서드는 UserQuerydslRepositoryImpl에게, UserCriteriaRepository 관련 메서드는 UserCriteriaRepositoryImpl에게 위임해야 한다

그래서 스프링은 여러 구현체를 조합해서 하나의 프록시 객체로 통합하기 위해 RepositoryFragment를 사용한다

RepositoryFragment는 스프링 데이터가 커스텀 리포지토리 구현체를 통합 관리하기 위해 사용하는 인터페이스로 하나의 구현체(implementation)와 해당 구현체가 구현한 인터페이스(fragment)를 나타낸다 (ImplementedRepositoryFragment 기준) 

리포지토리 인터페이스가 확장하고 있는 각 커스텀 리포지토리 인터페이스마다 생성되며 최종적으로 RepositoryComposition 객체에 모아진다

참고로 **커스텀 리포지토리 구현체는 각각 스프링 빈으로 등록된다**

이러한 과정은 RepositoryConfigurationDelegate.registerRepositoriesIn 메서드에서 리포지토리 인터페이스마다 빈 정의를 등록할 때 함께 이뤄진다

그리고 리포지토리 인터페이스의 프록시에 적용되는 ImplementationMethodExecutionInterceptor는 리포지토리 인터페이스, 커스텀 리포지토리 인터페이스 및 구현체에 대한 정보를 전달받고 런타임에 RepositoryCompoistion에게 요청을 위임하여 커스텀 리포지토리 메서드의 호출을 처리한다

```java
public abstract class RepositoryFactorySupport {
    
    // 커스텀 리포지토리 메서드 호출(querydsl, criteria api 등)을 처리하는 어드바이스 
    static class ImplementationMethodExecutionInterceptor implements MethodInterceptor {

        // 리포지토리 인터페이스 및 커스텀 리포지토리 인터페이스와 구현체 정보를 관리한다
        private final RepositoryComposition composition;

        // RepositoryComposition에게 커스텀 리포지토리 메서드 호출을 위임한다
        public Object invoke(@SuppressWarnings("null") MethodInvocation invocation) throws Throwable {

            Method method = invocation.getMethod();
            Object[] arguments = invocation.getArguments();

            return composition.invoke(invocationMulticaster, method, arguments);
    }
}
```

```java
public class RepositoryComposition {

    Object invoke(RepositoryInvocationMulticaster listener, Method method, Object[] args) throws Throwable {

        // 보관하고 있는 RepositoryFragment를 통해 메서드에 해당하는 구현체를 찾는다
        Method methodToCall = getMethod(method);

        // ImplementationMethodExecutionInterceptor는 가장 마지막에 호출되는데 메서드를 실행시킬 수 없다면
        // 스프링 데이터 JPA에서 지원하지 않는 기능이므로 예외를 던진다
        if (methodToCall == null) {
            throw new IllegalArgumentException(String.format("No fragment found for method %s", method));
        }

        // 실질적으로 커스텀 리포지토리 구현체의 메서드를 호출한다
        return fragments.invoke(metadata != null ? metadata.getRepositoryInterface() : method.getDeclaringClass(), listener,
                method, methodToCall, argumentConverter.apply(methodToCall, args));
    }
}
```


## @PersistenceContext, @PersistenceUnit

@PersistenceContext는 영속성 컨텍스트에 바인딩된 JPA 엔티티 매니저를 주입받기 위해 사용한다

엔티티 매니저는 스레드 세이프하지 않기 때문에 스프링은 프록시를 주입하여 각 트랜잭션마다 새로운 엔티티 매니저를 바인딩할 수 있게 한다

```java
@PersistenceContext
private EntityManager em;
```

@PersistenceUnit은 엔티티 매니저 팩토리를 주입받기 위해 사용한다

엔티티 매니저 팩토리는 스레드 세이프하며 일반적으로 싱글톤으로 관리된다

```java
@PersistenceUnit
private EntityManageFactory emf;
```

스프링 데이터 JPA를 사용하면 리포지토리 인터페이스만 정의하고도 추상화된 CRUD 기능을 이용할 수 있고, 스프링 부트 자동 설정으로 인해 @PersistenceContext 없이도 주입할 수 있게 되어 명시적으로 두 어노테이션을 사용하는 일이 드물다