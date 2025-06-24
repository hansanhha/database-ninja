package db.ninja.category;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private int displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    // displayOrder 순으로 자식 카테고리 정렬
    @OrderBy("displayOrder ASC")
    private List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    private boolean enabled;

    @Builder
    public Category(Category parent, String name, int displayOrder) {
        this.parent = parent;
        this.name = name;
        this.displayOrder = displayOrder;
        this.enabled = true;
    }

    public void addChild(Category child) {
        this.children.add(child);
        child.parent = this;
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public void changeDisplayOrder(int order) {
        this.displayOrder = order;
    }

    public boolean disable() {
        return enabled = false;
    }

    public boolean activate() {
        return enabled = true;
    }

    public boolean isRootCategory() {
        return parent == null;
    }

}