[1:N 관계 (@OneToMany, @ManyToMany, @ElementCollection)](#1n-관계-onetomany-manytomany-elementcollection)


## 1:N 관계 (@OneToMany, @ManyToMany, @ElementCollection)

### 하이버네이트가 엔티티 컬렉션을 관리하는 방식과 주의점

하이버네이트는 @OneToMany, @ManyToMany, @ElementCollection을 모두 컬렉션으로 간주한다

| 컬렉션 타입             | 관리 방식        | 특징                                            |
|--------------------|--------------|-----------------------------------------------|
| @OneToMany         | 연관 엔티티 관리    | 외래 키가 대상 엔티티에 있다. cascade/orphanRemoval 사용 가능 |
| @ManyToMany        | 조인 테이블로 다대다 관리 | 양 쪽에서 관리 (주의 필요)                              |
| @ElementCollection | 값 타입을 컬렉션으로 관리 | 별도 테이블에서 값만 관리하며, 연관 관계가 아니다                  |

또한 내부적으로 각 컬렉션 필드의 참조를 기반으로 다음과 같은 정보를 유지한다
- 스냅샷: 컬렉션의 기존 상태
- 현재 상태: 코드에서 변경한 상태 (add, remove 등)

하이버네이트는 **flush 시 객체 주소나 구성 요소 등에 대한 변경사항을 비교해서 INSERT, DELETE 쿼리를 생성**한다

**주의점**: @OneToMany(orphanRemoval = true) 설정이 있는 컬렉션에서 컬렉션 필드 자체가 교체되면 하이버네이트는 이전 값과 비교할 수 없다고 판단하여 예외를 던진다

orphanRemoval을 위해 기존 컬렉션을 반드시 추적해야 하는데 컬렉션 필드 자체가 새 객체로 교체되면 이전 상태를 잃어버리기 때문이다

```java
// 예시 엔티티
@Entity
class Post {
    
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();
}

// 엔티티 컬렉션이 메모리에 로드됐다고 가정한다
// 아래와 같이 기존 컬렉션 필드에 대한 참조를 없애버리면 orphanRemoval을 작동할 수 없게 되어 예외가 발생한다
post.setImages(new ArrayList<>(image1, image2)); // 컬렉션 필드 자체를 새 객체로 교체
post.setImages(null); // 컬렉션 필드 자체를 null로 설정
```

이러한 상황이 발생하면 하이버네이트는 `A collection with orphan deletion was no longer referenced by the owning entity instance` 이라는 에러 메시지를 던진다

### 엔티티 컬렉션 업데이트 발생하는 쿼리

[테스트 코드](../entity-association/src/test/java/db/ninja/one_to_many/CollectionUpdateTests.java)

엔티티 컬렉션 업데이트에 따른 쿼리는 다음과 같이 발생한다
- 전체 업데이트 시: 새로운 엔티티 목록에 대한 삽입 쿼리 N번, 기존 엔티티 목록에 대한 삭제 쿼리 N번 발생
- 엔티티 1개 추가 시: 새롭게 추가되는 엔티티에 대한 삽입 쿼리 1번 발생
- 엔티티 1개 삭제 시: 삭제되는 엔티티에 대한 삭제 쿼리 1번 발생


### VO 컬렉션 업데이트 시 발생하는 쿼리

[테스트 코드](../entity-association/src/test/java/db/ninja/one_to_many/CollectionUpdateTests.java)

VO 컬렉션 업데이트는 엔티티 컬렉션과 다르게 동작한다
- 전체 업데이트 시: VO 컬렉션 삭제 쿼리 1번, 새롭게 추가되는 VO에 대한 삽입 쿼리 N번 발생
- VO 1개 추가 시: 새롭게 추가되는 VO에 대한 삽입 쿼리 1번 발생
- VO 1개 삭제 시: VO 컬렉션 삭제 쿼리 1번, 삭제할 VO를 제외한 나머지 VO에 대한 삽입 쿼리 N번 발생 (@OrderColumn인 경우 업데이트 쿼리 N번 발생)

엔티티 컬렉션은 1개 삭제 시 해당 엔티티만 삭제할 수 있지만 VO 컬렉션에서 1개 삭제 시 전체 VO 컬렉션을 삭제하고 새롭게 삽입하는 방식으로 동작한다