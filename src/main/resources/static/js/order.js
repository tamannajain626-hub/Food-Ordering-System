/* ============================================================
   Food Ordering System - order placement logic
   ============================================================ */

/* Currency formatter for Indian Rupees (INR).
   Uses Intl.NumberFormat for consistent formatting with ₹ symbol and 2 decimals. */
const formatPrice = (value) => new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
}).format(Number(value) || 0);

const MENU_API = "/api/menu-items";
const CUSTOMERS_API = "/api/customers";
const ORDERS_API = "/api/orders";

const customerSelect = document.getElementById("customerSelect");
const menuGrid = document.getElementById("orderMenuGrid");
const menuCount = document.getElementById("orderMenuCount");
const menuEmpty = document.getElementById("orderMenuEmpty");
const categoryFilters = document.getElementById("orderCategoryFilters");
const messageBox = document.getElementById("orderMessage");

const cartItemsBox = document.getElementById("orderCartItems");
const cartEmptyBox = document.getElementById("orderCartEmpty");
const orderTotalBox = document.getElementById("orderTotal");
const clearBtn = document.getElementById("orderClearButton");
const placeBtn = document.getElementById("placeOrderBtn");

const statCustomers = document.getElementById("statCustomers");
const statMenu = document.getElementById("statMenu");
const statItems = document.getElementById("statItems");

const cartBar = document.getElementById("orderCartBar");
const cartBarCount = document.getElementById("orderCartBarCount");
const cartBarTotal = document.getElementById("orderCartBarTotal");
const cartBarButton = document.getElementById("orderCartBarButton");

const state = {
    customers: [],
    items: [],
    itemsById: {},
    categories: [],
    activeCategory: "All",
    cart: {}
};

function showMessage(type, text) {
    messageBox.className = "message " + type;
    messageBox.textContent = text;
    messageBox.classList.remove("hidden");
}

function hideMessage() {
    messageBox.classList.add("hidden");
    messageBox.textContent = "";
}

function loadAll() {
    Promise.all([
        fetch(CUSTOMERS_API).then(r => r.json()),
        fetch(MENU_API).then(r => r.json())
    ]).then(([customers, items]) => {
        state.customers = customers;
        state.items = items.filter(i => i.available);
        state.itemsById = {};
        state.items.forEach(i => { state.itemsById[i.itemId] = i; });
        state.categories = [...new Set(items.filter(i => i.available).map(i => i.category))].sort();

        populateCustomers();
        buildFilters();
        renderMenu();
        updateStats();
        renderCart();
    }).catch(err => showMessage("error", "Failed to load data: " + err.message));
}

function populateCustomers() {
    customerSelect.innerHTML = "";
    if (state.customers.length === 0) {
        const opt = document.createElement("option");
        opt.value = "";
        opt.textContent = "No customers registered yet";
        customerSelect.appendChild(opt);
        statCustomers.textContent = 0;
        return;
    }
    const placeholder = document.createElement("option");
    placeholder.value = "";
    placeholder.textContent = "Choose a registered customer...";
    customerSelect.appendChild(placeholder);

    state.customers.forEach(c => {
        const opt = document.createElement("option");
        opt.value = c.customerId;
        opt.textContent = "#" + c.customerId + " - " + c.name + " (" + c.email + ")";
        customerSelect.appendChild(opt);
    });
    statCustomers.textContent = state.customers.length;
    customerSelect.addEventListener("change", updatePlaceButton);
}

function buildFilters() {
    categoryFilters.innerHTML = "";
    ["All"].concat(state.categories).forEach(cat => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "filter-button" + (cat === state.activeCategory ? " active" : "");
        btn.textContent = cat;
        btn.dataset.category = cat;
        categoryFilters.appendChild(btn);
    });
}

function renderMenu() {
    menuGrid.innerHTML = "";
    const list = state.activeCategory === "All"
        ? state.items
        : state.items.filter(i => i.category === state.activeCategory);

    menuCount.textContent = list.length + (list.length === 1 ? " available dish" : " available dishes");
    menuEmpty.classList.toggle("hidden", list.length !== 0);

    list.forEach(item => {
        const card = document.createElement("article");
        card.className = "dish-card";
        const media = document.createElement("div");
        media.className = "dish-media";
        if (item.imageUrl) {
            const img = document.createElement("img");
            img.src = item.imageUrl;
            img.alt = item.name;
            img.loading = "lazy";
            media.appendChild(img);
        } else {
            const ph = document.createElement("div");
            ph.className = "dish-placeholder";
            ph.textContent = "🍽️";
            media.appendChild(ph);
        }
        const badge = document.createElement("span");
        badge.className = "dish-badge";
        badge.textContent = "Available";
        media.appendChild(badge);
        const cat = document.createElement("span");
        cat.className = "dish-category";
        cat.textContent = item.category;
        media.appendChild(cat);
        card.appendChild(media);

        const body = document.createElement("div");
        body.className = "dish-body";
        const title = document.createElement("h4");
        title.textContent = item.name;
        body.appendChild(title);
        const desc = document.createElement("p");
        desc.textContent = item.description || "";
        body.appendChild(desc);

        const foot = document.createElement("div");
        foot.className = "dish-foot";
        const price = document.createElement("span");
        price.className = "dish-price";
        price.textContent = formatPrice(item.price);
        foot.appendChild(price);

        const qty = state.cart[item.itemId] || 0;
        if (qty > 0) {
            foot.appendChild(buildStepper(item, qty));
        } else {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "add-button";
            btn.textContent = "Add to Order";
            btn.dataset.action = "add";
            btn.dataset.id = item.itemId;
            foot.appendChild(btn);
        }
        body.appendChild(foot);
        card.appendChild(body);
        menuGrid.appendChild(card);
    });
}

function buildStepper(item, qty) {
    const step = document.createElement("div");
    step.className = "stepper";
    const minus = document.createElement("button");
    minus.type = "button";
    minus.className = "step-button";
    minus.textContent = "−";
    minus.dataset.action = qty === 1 ? "remove" : "dec";
    minus.dataset.id = item.itemId;
    step.appendChild(minus);
    const count = document.createElement("span");
    count.className = "step-count";
    count.textContent = qty;
    step.appendChild(count);
    const plus = document.createElement("button");
    plus.type = "button";
    plus.className = "step-button";
    plus.textContent = "+";
    plus.dataset.action = "inc";
    plus.dataset.id = item.itemId;
    step.appendChild(plus);
    return step;
}

function addToCart(id) {
    const item = state.itemsById[id];
    if (!item) return;
    state.cart[id] = (state.cart[id] || 0) + 1;
    renderMenu();
    renderCart();
    updateStats();
    updatePlaceButton();
}

function changeQty(id, delta) {
    const cur = state.cart[id] || 0;
    const next = cur + delta;
    if (next <= 0) delete state.cart[id];
    else state.cart[id] = next;
    renderMenu();
    renderCart();
    updateStats();
    updatePlaceButton();
}

function removeItem(id) {
    delete state.cart[id];
    renderMenu();
    renderCart();
    updateStats();
    updatePlaceButton();
}

function clearCart() {
    state.cart = {};
    renderMenu();
    renderCart();
    updateStats();
    updatePlaceButton();
}

function renderCart() {
    cartItemsBox.innerHTML = "";
    const ids = Object.keys(state.cart);
    let total = 0;
    ids.forEach(id => {
        const item = state.itemsById[id];
        if (!item) return;
        const qty = state.cart[id];
        const line = Number(item.price) * qty;
        total += line;
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
        row.appendChild(buildStepper(item, qty));
        const t = document.createElement("span");
        t.className = "cart-row-total";
        t.textContent = formatPrice(line);
        row.appendChild(t);
        cartItemsBox.appendChild(row);
    });
    const count = ids.reduce((s, id) => s + state.cart[id], 0);
    cartEmptyBox.classList.toggle("hidden", count !== 0);
    orderTotalBox.textContent = formatPrice(total);
    cartBarCount.textContent = count + (count === 1 ? " item" : " items");
    cartBarTotal.textContent = formatPrice(total);
    cartBar.classList.toggle("hidden", count === 0);
}

function updateStats() {
    const count = Object.keys(state.cart).reduce((s, id) => s + state.cart[id], 0);
    statMenu.textContent = state.items.length;
    statItems.textContent = count;
}

function updatePlaceButton() {
    const hasCustomer = customerSelect.value !== "";
    const hasItems = Object.keys(state.cart).length > 0;
    placeBtn.disabled = !(hasCustomer && hasItems);
}

function placeOrder() {
    const customerId = Number(customerSelect.value);
    const items = Object.keys(state.cart).map(id => ({ itemId: Number(id), quantity: state.cart[id] }));
    placeBtn.disabled = true;
    placeBtn.textContent = "Placing order...";
    hideMessage();

    fetch(ORDERS_API, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ customerId, items })
    }).then(async res => {
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || "Failed to place order");
        return data;
    }).then(order => {
        showMessage("success", "Order placed successfully! Order ID: " + order.orderId + " | Total: " + formatPrice(order.totalAmount) + " | Status: " + order.status);
        state.cart = {};
        renderMenu();
        renderCart();
        updateStats();
        updatePlaceButton();
    }).catch(err => {
        showMessage("error", err.message);
    }).finally(() => {
        placeBtn.disabled = !(customerSelect.value !== "" && Object.keys(state.cart).length > 0);
        placeBtn.textContent = "Place Order";
    });
}

categoryFilters.addEventListener("click", e => {
    const btn = e.target.closest("[data-category]");
    if (!btn) return;
    state.activeCategory = btn.dataset.category;
    buildFilters();
    renderMenu();
});

menuGrid.addEventListener("click", e => {
    const btn = e.target.closest("[data-action]");
    if (!btn) return;
    const id = Number(btn.dataset.id);
    if (btn.dataset.action === "add") addToCart(id);
});

cartItemsBox.addEventListener("click", e => {
    const btn = e.target.closest("[data-action]");
    if (!btn) return;
    const id = Number(btn.dataset.id);
    if (btn.dataset.action === "inc") changeQty(id, 1);
    if (btn.dataset.action === "dec") changeQty(id, -1);
    if (btn.dataset.action === "remove") removeItem(id);
});

clearBtn.addEventListener("click", clearCart);
placeBtn.addEventListener("click", placeOrder);
cartBarButton.addEventListener("click", () => document.getElementById("orderCartPanel").scrollIntoView({ behavior: "smooth" }));

loadAll();