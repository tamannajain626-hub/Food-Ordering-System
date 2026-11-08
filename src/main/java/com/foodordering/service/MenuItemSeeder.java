package com.foodordering.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Puts the sample dishes on the menu the first time the application starts.
 *
 * It is safe to run again and again: MenuItemService only inserts a dish when
 * there is no dish with that name yet, so restarting the application never
 * creates duplicates.
 *
 * Set "app.menu.seed=false" in application.properties (or as an environment
 * variable) to switch the seeding off.
 */
@Component
public class MenuItemSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MenuItemSeeder.class);

    private final MenuItemService menuItemService;
    private final boolean seedEnabled;

    public MenuItemSeeder(MenuItemService menuItemService,
                          @Value("${app.menu.seed:true}") boolean seedEnabled) {
        this.menuItemService = menuItemService;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Menu seeding is switched off (app.menu.seed=false).");
            return;
        }

        int inserted = menuItemService.seedSampleItems();
        log.info("Menu seeding finished: {} new dish(es) added to the menu.", inserted);
    }
}
