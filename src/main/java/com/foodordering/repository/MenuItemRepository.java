package com.foodordering.repository;

import com.foodordering.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Spring Data JPA repository for the "menu_item" table.
 *
 * JpaRepository already gives us save(), findAll(), findById(), delete()...
 * We only add the small queries we need ourselves.
 */
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /** Every dish, grouped by category and then sorted by name. */
    List<MenuItem> findAllByOrderByCategoryAscNameAsc();

    /** Every dish of one category, sorted by name. */
    List<MenuItem> findByCategoryIgnoreCaseOrderByNameAsc(String category);

    /** Returns true when a dish with that name is already on the menu. */
    boolean existsByNameIgnoreCase(String name);

    /** The list of categories that are actually used, without repeats. */
    @Query("SELECT DISTINCT m.category FROM MenuItem m ORDER BY m.category")
    List<String> findDistinctCategories();
}
