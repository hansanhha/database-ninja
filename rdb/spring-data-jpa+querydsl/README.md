[이전으로](../README.md)

## 개요

data-jpa+querydsl 디렉토리는 크게 두 가지 파트를 다룬다

1. 예제 프로젝트([mini-carrot-market](./mini-carrot-market))를 기반으로 JPA 엔티티 설계, 스프링 데이터 JPA 및 Querydsl의 사용법을 익히는 부분
2. ORM 및 성능 최적화 등과 관련되어 있는 주요 개념들을 테스트하며 벤치마크하는 부분 (인덱싱에 따른 성능 차이 비교 등)

**스프링**
- [스프링 데이터 JPA, QueryDSL 주요 키워드](#스프링-데이터-jpa-querydsl-주요-키워드)
- [@Transactional](../../docs/스프링%20-%20@Transactional.md)

**예제 프로젝트**
- [예제 프로젝트 소개](#소개)
- [요구사항 정리 및 JPA 엔티티 설계](#요구사항-정리-및-jpa-엔티티-설계)
- [기능 구현 (일반 쿼리, QueryDSL)](#기능-구현--일반-쿼리-querydsl-)

**JPA**
- [엔티티 매니저(영속성 컨텍스트)](../../docs/JPA%20-%20엔티티%20매니저%20(영속성%20컨텍스트).md)
- [ID 전략](../../docs/JPA%20엔티티%20-%20ID%20전략.md)
- [N+1 문제](../../docs/JPA%20엔티티%20-%20N%2B1%20문제.md)
- [도메인 모델 (JPA 엔티티와 DDD의 관계)](../../docs/JPA%20엔티티%20-%20도메인%20모델%20(DDD와의%20관계).md)
- [연관관계](../../docs/JPA%20엔티티%20-%20연관관계.md)
- [삭제 전략](../../docs/JPA%20엔티티%20-%20삭제%20전략.md)
- [시간 자료형 선택지](../../docs/JPA%20엔티티%20-%20시간%20자료형%20선택지.md)
- [리치 도메인 모델과 애너믹 도메인 모델, 도메인 서비스](../../docs/기타%20-%20리치%20도메인%20모델과%20애너믹%20도메인%20모델,%20도메인%20서비스.md)

**테스트**
- [@DataJpaTest, @Sql](../../docs/테스트%20-%20@DataJpaTest,%20@Sql.md)
- [테스트 @Transactional](../../docs/테스트%20-%20@Transactional.md)
- [testcontainers](https://github.com/hansanhha/kick-the-testcontainers)

**성능 최적화**
- [인덱싱](../../docs/성능%20최적화%20-%20인덱싱.md)

**동시성**
- [경쟁 조건](../../docs/동시성%20제어%20-%20경쟁%20조건.md)



## 스프링 데이터 JPA, QueryDSL 주요 키워드

### JPA 엔티티 관련 키워드
- @Column
- 연관관계 (단방향, 양방향)
- FetchType.LAZY vs FetchType.EAGER
- CascadeType / orphanRemoval
- @Embedded, @Enumerated
- @EntityGraph
- @Version (낙관적 락)
- @ManyToMany 한계
- Auditing

### 쿼리 관련 키워드
- 리포지토리 인터페이스 관리
- 커스텀 리포지토리
- 쿼리 메서드 (메서드 이름 기반)
- JPQL
- 페치 조인
- 배치 처리 (벌크 수정/삭제 시 flush, clear 필요성)
- 페이징 처리, 정렬
- 영속성 컨텍스트 (persist, merge, detach, flush, clear)
- 더티 체킹
- @Modifying
- @SqlResultSetMapping

### Querydsl 관련 키워드
- Q타입 클래스
- 동적 조건: whereIf, BooleanBuilder
- 결과 조회: fetch(), fetchOne(), fetchFirst(), fetchResults()
- 프로젝션: Projections.fields, constructor
- Join, GroupBy, having()
- 페이징 처리, 정렬
- 서브쿼리
- DTO 매핑 최적화

### 성능 최적화 관련 키워드
- N+1 문제 (Lazy + Collection)
- Hibernate Statistics
- 로깅: show_sql, format_sql, p6spy
- 쿼리 최적화 (Index, Explain Plan)
- 페치 조인과 페이지네이션의 충돌
- 커넥션 풀 관리


## 예제 프로젝트

### 소개

예제 프로젝트 ([mini-carrot-market](./mini-carrot-market))는 당근을 벤치마킹한 간단한 서비스로 사용자가 판매할 중고 상품 게시글을 올리면 채팅을 통해 거래를 진행하는 기능을 제공한다

참고로 API(컨트롤러)는 제외하고 서비스-리포지토리 계층까지만 구현 및 테스트한다

| 기능         | 상세                                      |
|------------|-----------------------------------------|
| 사용자        | 회원가입, 로그인, 회원탈퇴, 프로필 조회, 동네 위치 설정       | 
| 중고 상품 게시글  | 게시글 작성/수정/삭제, 상태 변경, 숨기기, 게시글 조회/정렬     |
| 중고 상품 카테고리 | 카테고리 조회/등록/수정/삭제                        |
| 관심 상품 (좋아요) | 관심 등록/해제, 내 관심 상품 조회                    |
| 채팅         | 채팅방 조회/생성/삭제, 채팅 메시지 조회/전송/검색, 읽음 여부 표시 |
| 리뷰         | 거래 리뷰 작성/조회 |

프로젝트를 진행하면서 다음과 같은 역량을 익히고자 한다

**1. ORM 및 JPA 엔티티 설계 능력 강화**
- 도메인 모델 기반의 JPA 엔티티 설계
- 연관관계 설정 (단방향/양방향, 연관관계 주인 등)
- @Embeddable, @ElementCollection, VO 활용
- Soft Delete, Auditing 적용

**2. 스프링 데이터 JPA 활용 능력 강화**
- 쿼리 메서드, @Query, EntityGraph, 페치 조인, 페이징 등
- 쿼리 캐시 VS 2차 캐시
- 연관관계 기반의 조회 최적화 (N+1 해결)
- 리포지토리 추상화 설계

**3. Querydsl로 동적 쿼리 설계 및 최적화 경험**
- 다중 조건 동적 필터링
- 정렬, 페이징, 조인 활용
- 커스텀 리포지토리 구현

**4. 도메인 중심 설계 (DDD Lite) 경험**
- 엔티티 책임 분리 (도메인 서비스 vs 엔티티 메서드)
- VO 활용, 상태 변화에 대한 명확한 책임 부여
- 핵심 규칙과 비즈니스 요구사항을 코드에 반영

**5. 조회 성능 및 트래픽 진단 기반의 최적화 경험**
- 불필요한 쿼리 색출/제거
- 페치 조인 vs batch size vs EntityGraph 비교
- 조회 쿼리 수 모니터링
- 로그 분석 (하이버네이트 SQL 로그)

**6. 실용적인 요구사항 분석 및 구현 경험**
- 기능 명세 -> 도메인 요구사항 -> 엔티티 설계 -> 기능 구현 흐름
- 페이징, 정렬, 검색, 상태 변경 등 실전에서 자주 나오는 기능 직접 구현

**7. 기초적인 테스트 및 디버깅 경험**
- JPA 테스트 환경 구성 (@DataJpaTest, @Transactional)
- 디버깅을 통한 상태 추적 연습


#### [요구사항 정리 및 JPA 엔티티 설계](../../docs/예제%20프로젝트%201%20-%20요구사항%20정리%20및%20JPA%20엔티티%20설계.md)

#### [기능 구현 (일반 쿼리, QueryDSL)](../../docs/예제%20프로젝트%202%20-%20기능%20구현%20(일반%20쿼리,%20QueryDSL).md)


