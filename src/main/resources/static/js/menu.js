/* ============================================================
   Food Ordering System - menu page logic
   ------------------------------------------------------------
   Talks to the Spring Boot REST API:
       GET    /api/menu-items                 -> every dish
       GET    /api/menu-items/categories      -> the category names
       PUT    /api/menu-items/{id}/availability -> available / sold out
   ============================================================ */

/* Currency formatter for Indian Rupees (INR).
   Uses Intl.NumberFormat for consistent formatting with ₹ symbol and 2 decimals. */
const formatPrice = (value) => new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
}).format(Number(value) || 0);

const API_URL = "/api/menu-items";

/* A little picture is shown when the image of a dish cannot be loaded. */
const CATEGORY_ICONS = {
    "Starters": "🥗",
    "Main Course": "🍝",
    "Desserts": "🍰",
    "Drinks": "🥤"
};

/* Everything the page needs to remember. */
const state = {
    items: [],            // all dishes from the database
    itemsById: {},        // { itemId: dish }  - used by the cart
    categories: [],       // ["Desserts", "Drinks", "Main Course", "Starters"]
    activeCategory: "All",
    cart: {}              // { itemId: how many }
};

/* ------------------------------------------------------------
   Finds the parts of the page we need to change.
   ------------------------------------------------------------ */
const grid = document.getElementById("menuGrid");
const filters = document.getElementById("categoryFilters");
const heading = document.getElementById("menuHeading");
const countText = document.getElementById("menuCount");
const emptyText = document.getElementById("emptyMessage");
const messageBox = document.getElementById("message");

const cartItemsBox = document.getElementById("cartItems");
const cartEmptyBox = document.getElementById("cartEmpty");
const subtotalBox = document.getElementById("subtotal");
const clearCartButton = document.getElementById("clearCartButton");

const statDishes = document.getElementById("statDishes");
const statCategories = document.getElementById("statCategories");
const statCart = document.getElementById("statCart");

const cartBar = document.getElementById("cartBar");
const cartBarCount = document.getElementById("cartBarCount");
const cartBarSubtotal = document.getElementById("cartBarSubtotal");
const cartBarButton = document.getElementById("cartBarButton");

/* ------------------------------------------------------------
   Small helpers.
   ------------------------------------------------------------ */
function showMessage(type, text) {
    messageBox.className = "message " + type;
    messageBox.textContent = text;
    messageBox.classList.remove("hidden");
}

function hideMessage() {
    messageBox.classList.add("hidden");
    messageBox.textContent = "";
}

function iconFor(category) {
    return CATEGORY_ICONS[category] || "🍽️";
}

/* ------------------------------------------------------------
   Loads the menu from the backend.
   ------------------------------------------------------------ */
function loadMenu() {
    grid.innerHTML = "";
    countText.textContent = "Loading...";
    hideMessage();

    Promise.all([
        fetch(API_URL).then(function (response) {
            if (!response.ok) {
                throw new Error("HTTP " + response.status);
            }
            return response.json();
        }),
        fetch(API_URL + "/categories").then(function (response) {
            if (!response.ok) {
                throw new Error("HTTP " + response.status);
            }
            return response.json();
        })
    ])
        .then(function (results) {
            state.items = results[0];
            state.categories = results[1];
            state.itemsById = {};

            state.items.forEach(function (item) {
                state.itemsById[item.itemId] = item;
            });

            buildFilters();
            renderDishes();
            updateStats();
            renderCart();
        })
        .catch(function (error) {
            countText.textContent = "Could not load the menu.";
            showMessage("error",
                "Could not reach the server. Is the Spring Boot application running? " +
                "(" + error.message + ")");
        });
}

/* ------------------------------------------------------------
   Builds the filter buttons (All + one per category).
   ------------------------------------------------------------ */
function buildFilters() {
    filters.innerHTML = "";

    ["All"].concat(state.categories).forEach(function (category) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "filter-button" + (category === state.activeCategory ? " active" : "");
        button.textContent = category;
        button.setAttribute("data-category", category);
        filters.appendChild(button);
    });
}

/* ------------------------------------------------------------
   Shows the dishes of the selected category.
   ------------------------------------------------------------ */
function renderDishes() {
    grid.innerHTML = "";

    const list = state.activeCategory === "All"
        ? state.items
        : state.items.filter(function (item) {
            return item.category.toLowerCase() === state.activeCategory.toLowerCase();
        });

    heading.textContent = state.activeCategory === "All"
        ? "All dishes"
        : state.activeCategory;

    countText.textContent = list.length + (list.length === 1 ? " dish" : " dishes");

    emptyText.classList.toggle("hidden", list.length !== 0);

    list.forEach(function (item) {
        grid.appendChild(buildCard(item));
    });
}

/* ------------------------------------------------------------
   Builds one dish card.
   ------------------------------------------------------------ */
function buildCard(item) {
    const card = document.createElement("article");
    card.className = "dish-card" + (item.available ? "" : " sold-out");

    /* ----- the picture ----- */
    const media = document.createElement("div");
    media.className = "dish-media";

    if (item.imageUrl) {
        const image = document.createElement("img");
        image.src = item.imageUrl;
        image.alt = item.name;
        image.loading = "lazy";
        // If the picture cannot be loaded, show a little icon instead.
        image.addEventListener("error", function () {
            showPlaceholder(media, item);
        });
        media.appendChild(image);
    } else {
        showPlaceholder(media, item);
    }

    const badge = document.createElement("span");
    badge.className = "dish-badge" + (item.available ? "" : " sold");
    badge.textContent = item.available ? "Available" : "Sold out";
    media.appendChild(badge);

    const category = document.createElement("span");
    category.className = "dish-category";
    category.textContent = item.category;
    media.appendChild(category);

    card.appendChild(media);

    /* ----- the text ----- */
    const body = document.createElement("div");
    body.className = "dish-body";

    const title = document.createElement("h4");
    title.textContent = item.name;
    body.appendChild(title);

    const description = document.createElement("p");
    description.textContent = item.description || "";
    body.appendChild(description);

    /* ----- price + button ----- */
    const foot = document.createElement("div");
    foot.className = "dish-foot";

    const price = document.createElement("span");
    price.className = "dish-price";
    price.textContent = formatPrice(item.price);
    foot.appendChild(price);

    const quantity = state.cart[item.itemId] || 0;
    if (quantity > 0) {
        foot.appendChild(buildStepper(item, quantity, false));
    } else {
        const addButton = document.createElement("button");
        addButton.type = "button";
        addButton.className = "add-button";
        addButton.textContent = "Add to Order";
        addButton.disabled = !item.available;
        addButton.setAttribute("data-action", "add");
        addButton.setAttribute("data-id", item.itemId);
        foot.appendChild(addButton);
    }

    body.appendChild(foot);

    /* ----- restaurant admin link (writes to MySQL) ----- */
    const adminLink = document.createElement("button");
    adminLink.type = "button";
    adminLink.className = "availability-link";
    adminLink.textContent = item.available
        ? "Restaurant admin: mark as sold out"
        : "Restaurant admin: mark as available again";
    adminLink.setAttribute("data-action", "availability");
    adminLink.setAttribute("data-id", item.itemId);
    body.appendChild(adminLink);

    card.appendChild(body);
    return card;
}

function showPlaceholder(media, item) {
    media.innerHTML = "";
    const placeholder = document.createElement("div");
    placeholder.className = "dish-placeholder";
    placeholder.textContent = iconFor(item.category);
    media.appendChild(placeholder);
}

/* ------------------------------------------------------------
   The - and + buttons that change how many of a dish we want.
   ------------------------------------------------------------ */
function buildStepper(item, quantity, small) {
    const stepper = document.createElement("div");
    stepper.className = "stepper" + (small ? " small" : "");

    const minus = document.createElement("button");
    minus.type = "button";
    minus.className = "step-button";
    minus.textContent = "−";
    minus.setAttribute("data-action", quantity === 1 ? "remove" : "decrease");
    minus.setAttribute("data-id", item.itemId);
    stepper.appendChild(minus);

    const count = document.createElement("span");
    count.className = "step-count";
    count.textContent = quantity;
    stepper.appendChild(count);

    const plus = document.createElement("button");
    plus.type = "button";
    plus.className = "step-button";
    plus.textContent = "+";
    plus.setAttribute("data-action", "increase");
    plus.setAttribute("data-id", item.itemId);
    stepper.appendChild(plus);

    return stepper;
}

/* ------------------------------------------------------------
   Adds a dish to the order.
   ------------------------------------------------------------ */
function addToCart(itemId) {
    const item = state.itemsById[itemId];
    if (!item || !item.available) {
        return;
    }
    state.cart[itemId] = (state.cart[itemId] || 0) + 1;
    renderDishes();
    renderCart();
    updateStats();
}

/* ------------------------------------------------------------
   Changes the number of a dish in the order.
   ------------------------------------------------------------ */
function changeQuantity(itemId, change) {
    const current = state.cart[itemId] || 0;
    const next = current + change;

    if (next <= 0) {
        delete state.cart[itemId];
    } else {
        state.cart[itemId] = next;
    }

    renderDishes();
    renderCart();
    updateStats();
}

function removeFromCart(itemId) {
    delete state.cart[itemId];
    renderDishes();
    renderCart();
    updateStats();
}

function clearCart() {
    state.cart = {};
    renderDishes();
    renderCart();
    updateStats();
}

/* ------------------------------------------------------------
   Draws the order panel and works out the subtotal.
   ------------------------------------------------------------ */
function renderCart() {
    cartItemsBox.innerHTML = "";

    const ids = Object.keys(state.cart);
    const totalInCents = ids.reduce(function (sum, id) {
        const item = state.itemsById[id];
        if (!item) {
            return sum;
        }
        return sum + Math.round(Number(item.price) * 100) * state.cart[id];
    }, 0);

    ids.forEach(function (id) {
        const item = state.itemsById[id];
        if (!item) {
            return;
        }
        const quantity = state.cart[id];

        const row = document.createElement("div");
        row.className = "cart-row";

        const info = document.createElement("div");
        info.className = "cart-row-info";

        const name = document.createElement("span");
        name.className = "cart-row-name";
        name.textContent = item.name;
        info.appendChild(name);

        const each = document.createElement("span");
        each.className = "cart-row-price";
        each.textContent = formatPrice(item.price) + " each";
        info.appendChild(each);

        row.appendChild(info);
        row.appendChild(buildStepper(item, quantity, true));

        const total = document.createElement("span");
        total.className = "cart-row-total";
        // Work in "cents" so the sum never gets a rounding mistake.
        total.textContent = formatPrice(Math.round(Number(item.price) * 100) * quantity / 100);
        row.appendChild(total);

        cartItemsBox.appendChild(row);
    });

    const itemCount = ids.reduce(function (sum, id) {
        return sum + state.cart[id];
    }, 0);

    cartEmptyBox.classList.toggle("hidden", itemCount !== 0);

    const subtotal = totalInCents / 100;
    subtotalBox.textContent = formatPrice(subtotal);

    /* the small bar that is shown on phones */
    cartBarCount.textContent = itemCount + (itemCount === 1 ? " item" : " items");
    cartBarSubtotal.textContent = formatPrice(subtotal);
    cartBar.classList.toggle("hidden", itemCount === 0);
}

/* ------------------------------------------------------------
   The three numbers under the hero picture.
   ------------------------------------------------------------ */
function updateStats() {
    const itemCount = Object.keys(state.cart).reduce(function (sum, id) {
        return sum + state.cart[id];
    }, 0);

    statDishes.textContent = state.items.length;
    statCategories.textContent = state.categories.length;
    statCart.textContent = itemCount;
}

/* ------------------------------------------------------------
   Marks a dish as sold out / available again in the database.
   ------------------------------------------------------------ */
function toggleAvailability(itemId) {
    const item = state.itemsById[itemId];
    if (!item) {
        return;
    }

    const newValue = !item.available;

    fetch(API_URL + "/" + itemId + "/availability", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ available: newValue })
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error("HTTP " + response.status);
            }
            return response.json();
        })
        .then(function (updated) {
            // Save the new value and redraw the page.
            state.itemsById[itemId] = updated;
            state.items.forEach(function (one) {
                if (one.itemId === itemId) {
                    one.available = updated.available;
                }
            });

            // A dish that just became sold out must not stay in the order.
            if (!updated.available) {
                delete state.cart[itemId];
            }

            renderDishes();
            renderCart();
            updateStats();
            showMessage("success",
                updated.name + " is now marked as " + (updated.available ? "available" : "sold out") + ".");
        })
        .catch(function (error) {
            showMessage("error",
                "Could not change the availability of this dish (" + error.message + ").");
        });
}

/* ------------------------------------------------------------
   Clicks on the buttons (event delegation).
   ------------------------------------------------------------ */
function handleClick(event) {
    const button = event.target.closest("[data-action]");
    if (!button) {
        return;
    }

    const action = button.getAttribute("data-action");
    const id = button.getAttribute("data-id");

    if (action === "add") {
        addToCart(Number(id));
    } else if (action === "increase") {
        changeQuantity(Number(id), 1);
    } else if (action === "decrease") {
        changeQuantity(Number(id), -1);
    } else if (action === "remove") {
        removeFromCart(Number(id));
    } else if (action === "availability") {
        toggleAvailability(Number(id));
    }
}

grid.addEventListener("click", handleClick);
cartItemsBox.addEventListener("click", handleClick);

filters.addEventListener("click", function (event) {
    const button = event.target.closest("[data-category]");
    if (!button) {
        return;
    }
    state.activeCategory = button.getAttribute("data-category");
    buildFilters();
    renderDishes();
});

clearCartButton.addEventListener("click", clearCart);

cartBarButton.addEventListener("click", function () {
    document.getElementById("cartPanel").scrollIntoView({ behavior: "smooth" });
});

// Load the menu as soon as the page opens.
loadMenu();