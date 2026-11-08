/* ============================================================
   Food Ordering System - registration form logic
   ------------------------------------------------------------
   Talks to the Spring Boot REST API:
       POST /api/customers   -> register a customer
   ============================================================ */

const form = document.getElementById("registerForm");
const messageBox = document.getElementById("message");
const submitButton = document.getElementById("submitButton");

const nameInput = document.getElementById("name");
const emailInput = document.getElementById("email");
const phoneInput = document.getElementById("phone");

// The API lives on the same server as this page, so a relative URL is enough.
const API_URL = "/api/customers";

/* ------------------------------------------------------------
   Shows a message above the form.
   type = "success" or "error"
   ------------------------------------------------------------ */
function showMessage(type, html) {
    messageBox.className = "message " + type;
    messageBox.innerHTML = html;
    messageBox.classList.remove("hidden");
}

function hideMessage() {
    messageBox.classList.add("hidden");
    messageBox.innerHTML = "";
}

/* ------------------------------------------------------------
   Clears the red error text under each input.
   ------------------------------------------------------------ */
function clearFieldErrors() {
    ["name", "email", "phone"].forEach(function (field) {
        document.getElementById(field + "Error").textContent = "";
    });
    [nameInput, emailInput, phoneInput].forEach(function (input) {
        input.classList.remove("invalid");
    });
}

/* ------------------------------------------------------------
   Shows an error under one input box.
   ------------------------------------------------------------ */
function showFieldError(field, text) {
    document.getElementById(field + "Error").textContent = text;
    document.getElementById(field).classList.add("invalid");
}

/* ------------------------------------------------------------
   Simple checks in the browser, so the user gets an answer
   immediately. The server checks the same things again.
   ------------------------------------------------------------ */
function validateForm(customer) {
    const errors = {};

    if (!customer.name.trim()) {
        errors.name = "Name is required";
    }

    if (!customer.email.trim()) {
        errors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(customer.email.trim())) {
        errors.email = "Email must be a valid email address";
    }

    if (!customer.phone.trim()) {
        errors.phone = "Phone is required";
    } else if (!/^[0-9]{10}$/.test(customer.phone.trim())) {
        errors.phone = "Phone must be exactly 10 digits";
    }

    return errors;
}

/* ------------------------------------------------------------
   Runs when the user presses the Register button.
   ------------------------------------------------------------ */
form.addEventListener("submit", function (event) {
    event.preventDefault();          // do not reload the page
    clearFieldErrors();
    hideMessage();

    const customer = {
        name: nameInput.value,
        email: emailInput.value,
        phone: phoneInput.value
    };

    // 1. Check the fields in the browser first.
    const errors = validateForm(customer);
    if (Object.keys(errors).length > 0) {
        for (const field in errors) {
            showFieldError(field, errors[field]);
        }
        return;
    }

    // 2. Send the data to the Spring Boot backend.
    submitButton.disabled = true;
    submitButton.textContent = "Saving...";

    fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(customer)
    })
        .then(function (response) {
            // The server always sends back JSON (even for errors).
            return response.json().then(function (data) {
                return { ok: response.ok, status: response.status, data: data };
            });
        })
        .then(function (result) {

            if (result.status === 201) {
                // Success! MySQL generated the customer id for us.
                showMessage(
                    "success",
                    "Customer registered successfully!" +
                    "<span class=\"big-id\">Customer ID: " + result.data.customerId + "</span>" +
                    "This ID was generated automatically by MySQL (AUTO_INCREMENT)."
                );
                form.reset();
                return;
            }

            if (result.status === 400) {
                // The server rejected the input and told us which fields are wrong.
                const serverErrors = result.data.errors || {};
                let handled = false;
                for (const field in serverErrors) {
                    showFieldError(field, serverErrors[field]);
                    handled = true;
                }
                showMessage("error", handled
                    ? "Please fix the highlighted fields."
                    : (result.data.message || "The details you entered are not valid."));
                return;
            }

            if (result.status === 409) {
                // The email is already registered.
                showFieldError("email", "This email is already registered");
                showMessage("error", result.data.message || "This email is already registered.");
                return;
            }

            // Anything else (server not running, database problem, ...).
            showMessage("error",
                "Something went wrong (HTTP " + result.status + "). " +
                (result.data.message || "Is the backend running?"));
        })
        .catch(function (error) {
            // The request never reached the server.
            showMessage("error",
                "Could not reach the server. Is the Spring Boot application running? " +
                "(" + error.message + ")");
        })
        .finally(function () {
            submitButton.disabled = false;
            submitButton.textContent = "Register";
        });
});
