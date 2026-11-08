/* ============================================================
   Food Ordering System - customers list logic
   ------------------------------------------------------------
   Talks to the Spring Boot REST API:
       GET /api/customers   -> read every customer from MySQL
   ============================================================ */

const tableBody = document.getElementById("customersTableBody");
const countText = document.getElementById("countText");
const emptyText = document.getElementById("emptyText");
const messageBox = document.getElementById("message");
const refreshButton = document.getElementById("refreshButton");

const API_URL = "/api/customers";

function showMessage(type, text) {
    messageBox.className = "message " + type;
    messageBox.textContent = text;
    messageBox.classList.remove("hidden");
}

function hideMessage() {
    messageBox.classList.add("hidden");
    messageBox.textContent = "";
}

/* ------------------------------------------------------------
   Adds one row to the table.
   We use textContent (not innerHTML) for the values, so text
   coming from the database can never break the page.
   ------------------------------------------------------------ */
function addRow(customer) {
    const row = tableBody.insertRow();

    const idCell = row.insertCell();
    idCell.className = "id";
    idCell.textContent = customer.customerId;

    row.insertCell().textContent = customer.name;
    row.insertCell().textContent = customer.email;
    row.insertCell().textContent = customer.phone;
}

/* ------------------------------------------------------------
   Loads the customers from the backend and fills the table.
   ------------------------------------------------------------ */
function loadCustomers() {
    tableBody.innerHTML = "";
    hideMessage();
    countText.textContent = "Loading...";
    emptyText.classList.add("hidden");
    refreshButton.disabled = true;

    fetch(API_URL)
        .then(function (response) {
            if (!response.ok) {
                throw new Error("HTTP " + response.status);
            }
            return response.json();
        })
        .then(function (customers) {
            customers.forEach(addRow);

            const count = customers.length;
            countText.textContent = count === 1
                ? "1 customer found in the database."
                : count + " customers found in the database.";

            emptyText.classList.toggle("hidden", count !== 0);
        })
        .catch(function (error) {
            countText.textContent = "Could not load customers.";
            showMessage("error",
                "Could not reach the server. Is the Spring Boot application running? " +
                "(" + error.message + ")");
        })
        .finally(function () {
            refreshButton.disabled = false;
        });
}

refreshButton.addEventListener("click", loadCustomers);

// Load the list as soon as the page opens.
loadCustomers();
