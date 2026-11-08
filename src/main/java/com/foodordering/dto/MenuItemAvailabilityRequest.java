package com.foodordering.dto;

import jakarta.validation.constraints.NotNull;

/**
 * The JSON body used to mark a dish as available / sold out.
 *
 * Example:  { "available": false }
 */
public class MenuItemAvailabilityRequest {

    @NotNull(message = "available is required (true or false)")
    private Boolean available;

    public MenuItemAvailabilityRequest() {
    }

    public MenuItemAvailabilityRequest(Boolean available) {
        this.available = available;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
