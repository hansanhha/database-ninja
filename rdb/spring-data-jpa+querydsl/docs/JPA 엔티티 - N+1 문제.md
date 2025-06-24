[N+1 문제: ORM의 고질적인 문제](#n1-문제-orm의-고질적인-문제)

[해결 방법](#해결-방법)


## N+1 문제: ORM의 고질적인 문제

N+1 문제 (N+1 Select Problem)는 ORM이 하나의 쿼리를 실행한 뒤 엔티티를 가져왔지만 연관되어 있는 엔티티(컬렉션)를 가져오기 위해 N번의 추가 쿼리가 발생하는 현상을 말한다

예를 들어 게시글(Post)과 작성자(User) 엔티티가 있을 때, 게시글 목록을 조회하면 해당 게시글의 작성자 정보를 불러오기 위해 추가로 N개의 쿼리가 발생하는 경우가 있다

### 예시 엔티티

```java
@Entity
@Getter
class Post {
    
    @Id @GeneratedValue
    private Long id;
    
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
} 
```

```java
@Entity
@Table(name = "users")
@Getter
class User {
    
    @Id @GeneratedValue
    private Long id;
    
    private String username;
}
```

### 연관 엔티티 조회로 인한 N+1 문제 발생

게시글 전체 조회 후 각 게시글의 작성자 정보를 출력하기 위해 N번의 추가 쿼리가 발생한다

게시글을 한 번에 100개 조회하면 최대 101개의 쿼리가 발생한다

[테스트 코드](../performance/src/test/java/db/ninja/n1_problem/N1ProblemTests.java)

```java
List<Post> posts = (List<Post>) postRepository.findAll();

// 게시글마다 작성자 정보를 출력한다 -> N+1 문제 발생
for (Post post : posts) {
    System.out.println(post.getUser().getUsername());    
}
```

**N+1 쿼리 발생 SQL 로그**

```text
// 1번의 게시글 전체 조회 쿼리 발생
select p1_0.id ,p1_0.title ,p1_0.user_id 
from post p1_0; 

// N번의 User 조회 쿼리 발생
select u1_0.id ,u1_0.username 
from users u1_0 
where u1_0.id=1;

select u1_0.id ,u1_0.username 
from users u1_0 
where u1_0.id=2;

...

select u1_0.id ,u1_0.username 
from users u1_0 
where u1_0.id=100;
```


## 해결 방법

### 1. @EntityGraph 사용

[테스트 코드](../performance/src/test/java/db/ninja/n1_problem/N1ProblemTests.java)

엔티티 그래프는 JPQL이나 네이티브 쿼리를 작성하지 않고도 페치 조인 효과를 얻을 수 있는 선언적 방식이다

LAZY로 설정되어 있어도 연관된 엔티티를 즉시 로딩하도록 지시하여 해당 연관 관계를 자동으로 조인해서 가져온다

엔티티 그래프는 크게 두 가지 방법으로 사용할 수 있다

1. 엔티티 클래스에 페치 조인할 엔티티들을 명시하는 방법 (네임드 엔티티 그래프)
2. 쿼리 메서드에서 페치 조인할 엔티티들을 명시하는 방법

다만 몇 가지 주의점이 존재한다
- 조인 대상 속성 이름이 오타나면 런타임 에러가 발생한다
- 중첩된 경로는 페치조인 할 수 없다
- 일대다 조인 시 데이터 중복이 발생한다
- 네이티브 쿼리와 함께 사용하면 무시된다

네임드 엔티티 그래프는 아래와 같이 이름을 지정할 수 있어서 페치 조인을 일관적으로 관리할 수 있다 -> 재사용 가능한 페치 조인 쿼리 구성 가능 

```java
// 네임드 엔티티 그래프를 정의한 엔티티 정의
@Entity
@Getter
@NamedEntityGraph(name = "post.writer", attributeNodes = { @NamedAttributeNode("user") })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntityGraph {

    @Id @GeneratedValue
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public PostEntityGraph(String title, User user) {
        this.title = title;
        this.user = user;
    }

}
```

```java
public interface PostEntityGraphRepository extends CrudRepository<PostEntityGraph, Long> {

    // 네임드 엔티티 그래프에 지정된 이름 명시
    @EntityGraph("post.writer")
    List<PostEntityGraph> findAll();
    
}
```

또는 아래처럼 쿼리 메서드에서 페치 조인할 경로를 명시할 수 있다

```java
public interface PostEntityGraphRepository extends CrudRepository<PostEntityGraph, Long> {

    // 조인할 경로 직접 명시
    @EntityGraph(attributePaths = "user")
    List<PostEntityGraph> findAll();
    
}
```

참고로 @EntityGraph 어노테이션은 기본적으로 지정한 속성만 페치 조인하는데, 지정한 속성 외에도 기본 EAGER 속성도 조인하려면 type 속성을 아래와 같이 지정하면 된다

```java
public interface PostEntityGraphRepository extends CrudRepository<PostEntityGraph, Long> {

    // 조인할 경로 직접 명시
    @EntityGraph(attributePaths = "user", type = EntityGraph.EntityGraphType.FETCH)
    List<PostEntityGraph> findAll();
    
}
```

**엔티티 그래프가 적용된 쿼리 메서드의 SQL 로그**

```text
select peg1_0.id, peg1_0.title, u1_0.id, u 1_0.username 
from post_entity_graph peg1_0 
left join users u1_0 
on u1_0.id=peg1_0.user_id;
```

### 2. Fetch Join 사용

[테스트 코드](../performance/src/test/java/db/ninja/n1_problem/N1ProblemTests.java)

페치 조인은 연관된 엔티티를 한 꺼번에 조회하기 위한 JPQL 문법이다

엔티티 그래프의 경우 선언적인 방식으로 즉시 로딩을 지시한다면 페치 조인은 JPQL에 직접 명시한다 -> 복잡한 쿼리 조합 가능

일반 조인 시 단순히 조인(SQL 조인)만 하고 연관된 엔티티는 로딩하지 않는 반면, 페치 조인은 연관된 엔티티도 즉시 로딩한다 (실제 엔티티 인스턴스화)

따라서 `p.getUser().getUsername()`을 해도 추가 쿼리가 발생하지 않는다

```java
public interface PostRepository extends CrudRepository<Post, Long> {

    // Post 엔티티 조회 시 연관 엔티티 User도 함께 메모리에 로딩한다
    @Query("SELECT p FROM Post p JOIN FETCH p.user u")
    List<Post> findAllFetchJoin();
}
```

**페치 조인 SQL 로그 (엔티티 그래프와 동일함)**

```text
select p1_0.id, p1_0.title, u1_0.id, u1_0.username 
from post p1_0 
join users u1_0 
on u1_0.id=p1_0.user_id
```

페치 조인 주의점
- @OneToMany와 페치 조인을 함께 사용하면 데이터가 중복되어 페이징이 망가진다 -> @OneToMany는 DTO 매핑 또는 별도 쿼리로 우회
- @ManyToOne, @OneToOne과 같은 단건 연관 관계는 페이징과 함께 사용할 수 있다
- 한 쿼리에서 여러 컬렉션을 페치 조인할 수 없다 -> QueryException 발생

### 3. DTO 프로젝션 사용

[테스트 코드]()

연관 엔티티에서 필요한 속성만 DTO 프로젝션을 통해 매핑하여 사용하면 N+1 문제를 해결할 수 있다

엔티티 그래프나 페치 조인처럼 연관 엔티티의 모든 필드를 메모리에 로딩하지 않기 때문에 메모리 효율적이다

```java
// 프로젝션용 DTO
public record PostDto(String title, String username) {

}
```

```java
public interface PostRepository extends CrudRepository<Post, Long> {
    
    // DTO 프로젝션을 사용하여 필요한 정보만 메모리에 로딩한다
    @Query("SELECT new db.ninja.n1_problem.PostDto(p.title, u.username) " +
            "FROM Post p " +
            "JOIN p.user u")
    List<PostDto> findAllProjection();
}
```

주의점
- 프로젝션 이외의 정보에 접근하면 N+1 문제가 발생할 수 있다
- 엔티티 없이 DTO로만 처리하기 때문에 도메인 로직을 사용할 수 없다
- 잘못 사용하면 성능 저하 역효과가 발생할 수 있다

**DTO 프로젝션을 사용한 SQL 로그**

페치 조인, 엔티티 그래프와 달리 명시한 필드 정보만 가져온다

```text
select p1_0.title ,u1_0.username 
from post p1_0 
join users u1_0 
on u1_0.id=p1_0.user_id;
```