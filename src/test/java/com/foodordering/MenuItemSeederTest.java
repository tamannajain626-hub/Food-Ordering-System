package com.foodordering;

import com.foodordering.entity.MenuItem;
import com.foodordering.repository.MenuItemRepository;
import com.foodordering.service.MenuItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the sample menu data.
 *
 * The important part: seeding the menu a second time must NOT create
 * duplicates, because the application is restarted many times.
 */
@SpringBootTest
class MenuItemSeederTest {

    /** How many sample dishes the seeder knows about. */
    private static final int SAMPLE_ITEM_COUNT = 11;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @BeforeEach
    void clearDatabase() {
        menuItemRepository.deleteAll();
    }

    @Test
    void seedSampleItems_shouldAddTheSampleDishes() {
        int inserted = menuItemService.seedSampleItems();

        assertThat(inserted).isEqualTo(SAMPLE_ITEM_COUNT);
        assertThat(menuItemRepository.count()).isEqualTo(SAMPLE_ITEM_COUNT);
    }

    @Test
    void seedSampleItems_shouldNotCreateDuplicates_whenItRunsAgain() {
        menuItemService.seedSampleItems();

        // The second run must not add anything.
        int insertedAgain = menuItemService.seedSampleItems();

        assertThat(insertedAgain).isZero();
        assertThat(menuItemRepository.count()).isEqualTo(SAMPLE_ITEM_COUNT);
    }

    @Test
    void seededItems_shouldHaveNamePriceCategoryAndAvailability() {
        menuItemService.seedSampleItems();

        List<MenuItem> items = menuItemRepository.findAll();
        assertThat(items).isNotEmpty();

        for (MenuItem item : items) {
            assertThat(item.getName()).isNotBlank();
            assertThat(item.getCategory()).isNotBlank();
            assertThat(item.getPrice()).isNotNull().isGreaterThan(java.math.BigDecimal.ZERO);
            assertThat(item.getItemId()).isNotNull();
        }

        // The sample data contains a sold out dish, so the "Sold out" badge
        // is visible on the menu straight away.
        assertThat(items).anyMatch(item -> !item.isAvailable());
    }

    @Test
    void seedSampleItems_shouldNotTouchDishesThatAreAlreadyOnTheMenu() {
        // A dish the user added themselves, with the same name as a sample dish.
        MenuItem mine = new MenuItem("Cappuccino", "My own version",
                new java.math.BigDecimal("5.00"), "Drinks",
                "https://example.com/mine.jpg", true);
        menuItemRepository.save(mine);

        int inserted = menuItemService.seedSampleItems();

        // The other samples were added, but the existing Cappuccino was kept.
        assertThat(inserted).isEqualTo(SAMPLE_ITEM_COUNT - 1);
        assertThat(menuItemRepository.count()).isEqualTo(SAMPLE_ITEM_COUNT);
        assertThat(menuItemRepository.findAll().stream()
                .filter(item -> item.getName().equals("Cappuccino"))
                .findFirst().orElseThrow()
                .getDescription()).isEqualTo("My own version");
    }
}
