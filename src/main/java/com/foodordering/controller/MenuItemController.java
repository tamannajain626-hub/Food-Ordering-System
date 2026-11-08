package com.foodordering.controller;

import com.foodordering.dto.MenuItemAvailabilityRequest;
import com.foodordering.dto.MenuItemRequest;
import com.foodordering.entity.MenuItem;
import com.foodordering.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for the food menu.
 *
 *  GET    /api/menu-items                 -> every dish on the menu
 *  GET    /api/menu-items/categories      -> the list of categories
 *  GET    /api/menu-items/category/{name} -> the dishes of one category
 *  GET    /api/menu-items/{id}            -> one dish
 *  POST   /api/menu-items                 -> add a new dish
 *  PUT    /api/menu-items/{id}            -> change a dish
 *  PUT    /api/menu-items/{id}/availability -> mark a dish available / sold out
 *  DELETE /api/menu-items/{id}            -> remove a dish
 */
@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    /**
     * Returns every dish that is on the menu.
     */
    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllMenuItems() {
        return ResponseEntity.ok(menuItemService.getAllItems());
    }

    /**
     * Returns the categories that are used on the menu (no repeats).
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(menuItemService.getCategories());
    }

    /**
     * Returns the dishes of one category.
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<MenuItem>> getMenuItemsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(menuItemService.getItemsByCategory(category));
    }

    /**
     * Returns one dish.
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<MenuItem> getMenuItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(menuItemService.getItemById(itemId));
    }

    /**
     * Adds a new dish to the menu.
     *
     * @Valid makes Spring check the fields of the request body and return
     *         a 400 Bad Request if something is wrong (empty name, bad price...).
     */
    @PostMapping
    public ResponseEntity<MenuItem> addMenuItem(@Valid @RequestBody MenuItemRequest request) {
        MenuItem savedItem = menuItemService.addItem(request);
        // 201 Created + the saved dish (including the generated item_id).
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    /**
     * Changes an existing dish.
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long itemId,
                                                    @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.updateItem(itemId, request));
    }

    /**
     * Marks a dish as available or sold out.
     */
    @PutMapping("/{itemId}/availability")
    public ResponseEntity<MenuItem> setAvailability(@PathVariable Long itemId,
                                                     @Valid @RequestBody MenuItemAvailabilityRequest request) {
        return ResponseEntity.ok(menuItemService.setAvailability(itemId, request.getAvailable()));
    }

    /**
     * Removes a dish from the menu.
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long itemId) {
        menuItemService.deleteItem(itemId);
        // 204 No Content - there is nothing to send back.
        return ResponseEntity.noContent().build();
    }
}
