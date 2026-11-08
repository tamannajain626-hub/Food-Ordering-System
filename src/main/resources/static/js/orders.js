/* ============================================================
   Food Ordering System - orders list
   ============================================================ */

/* Currency formatter for Indian Rupees (INR).
   Uses Intl.NumberFormat for consistent formatting with ₹ symbol and 2 decimals. */
const formatPrice = (value) => new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
}).format(Number(value) || 0);

const ORDERS_API = "/api/orders";

const list = document.getElementById("ordersList");
const empty = document.getElementById("ordersEmpty");
const count = document.getElementById("ordersCount");
const messageBox = document.getElementById("ordersMessage");

function showMessage(type, text) {
    messageBox.className = "message " + type;
    messageBox.textContent = text;
    messageBox.classList.remove("hidden");
}

fetch(ORDERS_API)
    .then(r => r.json())
    .then(orders => {
        count.textContent = orders.length;
        if (orders.length === 0) {
            empty.classList.remove("hidden");
            return;
        }
        orders.forEach(order => {
            const card = document.createElement("article");
            card.className = "order-card-full";

            const head = document.createElement("div");
            head.className = "order-card-head";

            const title = document.createElement("div");
            title.className = "order-card-title";
            const strong = document.createElement("strong");
            strong.textContent = "Order #" + order.orderId;
            title.appendChild(strong);
            const meta = document.createElement("span");
            meta.className = "order-card-meta";
            meta.textContent = "Customer: " + (order.customerName || "#"+order.customerId) + " | " + (order.customerEmail || "") + " | Placed: " + (order.createdAt || "");
            title.appendChild(meta);
            head.appendChild(title);

            const status = document.createElement("span");
            status.className = "order-status";
            status.textContent = order.status || "PENDING";
            head.appendChild(status);
            card.appendChild(head);

            const table = document.createElement("table");
            table.className = "order-items-table";
            const thead = document.createElement("thead");
            thead.innerHTML = "<tr><th>Item</th><th>Quantity</th><th>Price at ordering</th><th>Line total</th></tr>";
            table.appendChild(thead);
            const tbody = document.createElement("tbody");
            (order.orderItems || []).forEach(item => {
                const tr = document.createElement("tr");
                tr.innerHTML = "<td>" + (item.itemName || "Item #"+item.itemId) + "</td><td>" + item.quantity + "</td><td>" + formatPrice(item.priceAtOrdering) + "</td><td>" + formatPrice(item.lineTotal) + "</td>";
                tbody.appendChild(tr);
            });
            table.appendChild(tbody);
            card.appendChild(table);

            const foot = document.createElement("div");
            foot.className = "order-card-foot";
            foot.innerHTML = "<span>Order Total</span><span>" + formatPrice(order.totalAmount) + "</span>";
            card.appendChild(foot);

            list.appendChild(card);
        });
    })
    .catch(err => showMessage("error", "Could not load orders: " + err.message));