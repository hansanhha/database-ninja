package db.ninja.one_to_many;


import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class CollectionUpdateTests {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PostRepository postRepository;

    @AfterEach
    void tearDown() {
        Statistics stats = getStatistics();
        stats.setStatisticsEnabled(false);
        stats.clear();
    }

    @Test
    void OneToMany_컬렉션_전체_업데이트시_N번_삽입삭제_쿼리가_발생한다() {
        Statistics stats = getStatistics();
        int imageCount = 10;
        Post post = postRepository.save(new Post("test post"));

        // 이미지 엔티티 설정
        // 엔티티 컬렉션 업데이트 시 기존 컬렉션 자체를 교체하면 하이버네이트는 기존 컬렉션의 참조를 잃어버리기 때문에 예외가 발생할 수 있다
        // 따라서 기존 컬렉션을 비우고 새 요소들을 추가하는 방식이 안전하다
        post.updateImages(createImages(post, imageCount));
        em.flush();
        em.clear();

        stats.setStatisticsEnabled(true);

        // 이미지 엔티티 업데이트
        // 기존 이미지에 대한 삭제 쿼리가 N개 발생하고 새로운 이미지 엔티티에 대한 삽입 쿼리가 N개 발생한다
        Post postContainsEntityCollection = postRepository.findById(post.getId()).orElseThrow();
        postContainsEntityCollection.updateImages(createImages(postContainsEntityCollection, imageCount));
        em.flush();
        em.clear();

        // 기존 이미지 엔티티 삭제 쿼리 N회 발생
        long entityDeleteCount = stats.getEntityDeleteCount();

        // 새로운 이미지 엔티티 삽입 쿼리 N회 발생
        long entityInsertCount = stats.getEntityInsertCount();

        assertThat(entityDeleteCount).isEqualTo(imageCount);
        assertThat(entityInsertCount).isEqualTo(imageCount);
    }

    @Test
    void OneToMany_특정_엔티티추가시_1번_삽입쿼리가_발생한다() {
        Statistics stats = getStatistics();
        Post post = postRepository.save(new Post("test post"));

        // 이미지 엔티티 설정
        post.updateImages(createImages(post, 10));
        em.flush();
        em.clear();

        stats.setStatisticsEnabled(true);

        // 이미지 엔티티에 새로운 엔티티 추가
        // 기존 엔티티 컬렉션에 대한 삭제 쿼리는 발생하지 않고, 새로운 엔티티에 대한 삽입 쿼리가 발생한다
        Post postContainsEntityCollection = postRepository.findById(post.getId()).orElseThrow();
        postContainsEntityCollection.addImage(new PostImage(postContainsEntityCollection, "new_image"));
        em.flush();
        em.clear();

        // 기존 이미지 엔티티 컬렉션에 대한 삭제 쿼리는 발생하지 않는다
        long entityDeleteCount = stats.getCollectionRemoveCount();

        // 새로운 이미지 엔티티 삽입 쿼리 1회 발생
        long entityInsertCount = stats.getEntityInsertCount();

        assertThat(entityDeleteCount).isEqualTo(0);
        assertThat(entityInsertCount).isEqualTo(1);
    }

    @Test
    void OneToMany_특정_엔티티삭제시_1번_삭제와_N번_삽입쿼리가_발생한다() {
        Statistics stats = getStatistics();
        Post post = postRepository.save(new Post("test post"));

        // 이미지 엔티티 설정
        post.updateImages(createImages(post, 10));
        em.flush();
        em.clear();

        stats.setStatisticsEnabled(true);

        // 기존 이미지 엔티티에서 특정 엔티티 삭제
        // 기존 엔티티 컬렉션에 대한 삭제 쿼리가 발생하고, 특정 엔티티를 제외한 새로운 엔티티 컬렉션에 대한 삽입 쿼리가 N번 발생한다
        Post postContainsEntityCollection = postRepository.findById(post.getId()).orElseThrow();
        postContainsEntityCollection.removeImage();
        em.flush();
        em.clear();

        // 특정 이미지 엔티티 삭제 쿼리 1회 발생
        long entityDeleteCount = stats.getEntityDeleteCount();

        assertThat(entityDeleteCount).isEqualTo(1);
    }

    @Test
    void ElementCollection_전체_업데이트시_1번_삭제와_N번_삽입_쿼리가_발생한다() {
        Statistics stats = getStatistics();
        int imageCount = 10;
        Post post = postRepository.save(new Post("test post"));

        // 이미지 ElementCollection 설정
        post.updateImageVOs(createImageVOs(imageCount));
        em.flush();
        em.clear();

        stats.setStatisticsEnabled(true);

        // 이미지 ElementCollection 업데이트
        // 기존 VO 컬렉션에 대한 삭제 쿼리가 1회 발생하고 새로운 VO 컬렉션에 대한 삽입 쿼리가 N회 발생한다
        Post postContainsVOCollections = postRepository.findById(post.getId()).orElseThrow();
        postContainsVOCollections.updateImageVOs(createImageVOs(imageCount));
        em.flush();
        em.clear();

        // 이미지 ElementCollection 삭제 쿼리 1회 발생
        long voCollectionDeleteCount = stats.getCollectionRemoveCount();

        // 새로운 VO 컬렉션 재생성: 1회 (컬렉션 재생성 횟수가 1회이며, 삽입 쿼리는 N회 발생한다)
        long voCollectionRecreateCount = stats.getCollectionRecreateCount();

        assertThat(voCollectionDeleteCount).isEqualTo(1);
        assertThat(voCollectionRecreateCount).isEqualTo(1);
    }

    @Test
    void ElementCollection_특정_VO추가시_1번_삽입_쿼리가_발생한다() {
        Statistics stats = getStatistics();
        Post post = postRepository.save(new Post("test post"));

        // 이미지 ElementCollection 설정
        post.updateImageVOs(createImageVOs(10));
        em.flush();
        em.clear();

        stats.setStatisticsEnabled(true);

        // 이미지 ElementCollection에 새로운 VO 추가
        // 기존 VO 컬렉션에 대한 삭제 쿼리는 발생하지 않고, 새로운 VO에 대한 삽입 쿼리가 발생한다
        Post postContainsVOCollection = postRepository.findById(post.getId()).orElseThrow();
        postContainsVOCollection.addImageVO(new PostImageVO("new_image"));
        em.flush();
        em.clear();

        // 이미지 ElementCollection 삭제 쿼리는 발생하지 않는다
        long voCollectionDeleteCount = stats.getCollectionRemoveCount();

        // 컬렉션 업데이트 1회 (VO 추가로 인한 삽입 쿼리 1회 발생)
        long voCollectionUpdateCount = stats.getCollectionUpdateCount();

        assertThat(voCollectionDeleteCount).isEqualTo(0);
        assertThat(voCollectionUpdateCount).isEqualTo(1);
    }

    @Test
    void ElementCollection_특정_VO삭제시_1번_삭제와_N번_삽입_쿼리가_발생한다() {
        Statistics stats = getStatistics();
        Post post = postRepository.save(new Post("test post"));

        // 이미지 ElementCollection 설정
        post.updateImageVOs(createImageVOs(10));
        em.flush();
        em.clear();

        stats.setStatisticsEnabled(true);

        // 기존 이미지 ElementCollection에서 특정 VO 삭제
        // 기존 VO 컬렉션에 대한 삭제 쿼리가 발생하고, 특정 VO를 제외한 새로운 VO 컬렉션에 대한 삽입 쿼리가 N번 발생한다
        // ElementCollection에 @OrderColumn이 설정되어 있으면 삽입 쿼리 대신 업데이트 쿼리가 N번 발생한다
        Post postContainsVOCollection = postRepository.findById(post.getId()).orElseThrow();
        postContainsVOCollection.removeImageVO(new PostImageVO("image1"));
        em.flush();
        em.clear();

        // 이미지 ElementCollection 업데이트: 1회 (컬렉션 삭제 쿼리 1회 발생, VO 요소들에 대한 삽입 또는 업데이트 쿼리 N회 발생)
        long collectionUpdateCount = stats.getCollectionUpdateCount();

        assertThat(collectionUpdateCount).isEqualTo(1);
    }

    private Statistics getStatistics() {
        SessionFactory sessionFactory = em.getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class);
        return sessionFactory.getStatistics();
    }

    private List<PostImage> createImages(Post post, int count) {
        List<PostImage> images = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            images.add(new PostImage(post, "image" + i));
        }
        return images;
    }

    private List<PostImageVO> createImageVOs(int count) {
        List<PostImageVO> imageVOs = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            imageVOs.add(new PostImageVO("image" + i));
        }
        return imageVOs;
    }

}
