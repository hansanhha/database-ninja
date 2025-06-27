package db.ninja.category;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Long createCategory(Long parentId, String name, int displayOrder) {
        Category parent = null;

        if (parentId != null) parent = categoryRepository.findById(parentId).orElseThrow();

        Category category = categoryRepository.save(new Category(parent, name, displayOrder));
        return category.getId();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow();
    }

}
