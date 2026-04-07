const hiddenForm = document.getElementById("hidden");
const signUpBtn = document.getElementById("signUpBtn");
const signInForm = document.getElementById("signInForm");
const signUpForm = document.getElementById("signUpForm");

signUpBtn.onclick = function() {
    hiddenForm.style.display = "block";
}

window.onclick = function(event) {
    if (event.target == hidden) {
        hiddenForm.style.display = "none";
    }
}

signInForm.onsubmit  = function(event) {
    event.preventDefault();
    signIn();
}

signUpForm.onsubmit = function(event) {
    event.preventDefault();
    createAccount();
}

async function signIn() {
    const email = document.getElementById("email");
    const password = document.getElementById("password");

    var obj = {
        "email" : email,
        "password" : password
    }

    try {
        const response = await fetch("http://localhost:8080/user/login", {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(obj)
        });

        if (response.ok) {

        } else {

        }

    } catch (error) {
        console.error("Caught error:", error.message);
    }


}

async function createAccount() {
    const email = document.getElementById("createEmail").value;
    const userName = document.getElementById("createUserName").value;
    const firstName = document.getElementById("firstName").value;
    const lastName = document.getElementById("lastName").value;
    const password = document.getElementById("createPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    // Need to validate the email and password
    if (password != confirmPassword) {
        alert("Your passwords do not match!");
        document.getElementById("createPassword").value = "";
        document.getElementById("confirmPassword").value = "";
        return;
    }

    if (validate_email(email) == false) {
        alert("Improper email format.");
        document.getElementById("createEmail").value = "";
        return;
    }

    if (validate_password(password) == false) {
        alert("Password does not meet the requirements.");
        document.getElementById("createPassword").value = "";
        document.getElementById("confirmPassword").value = "";
        return;
    }

    var obj = {
        "userName": userName,
        "firstName": firstName,
        "lastName": lastName,
        "email": email,
        "password": password
    };

    try {
        const response = await fetch("http://localhost:8080/user/create", {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(obj)
        });

        if (response.ok) {
            const data = await response.json();
            const message = data["Message"];

            document.getElementById("createEmail").value = "";
            document.getElementById("createUserName").value = "";
            document.getElementById("firstName").value = "";
            document.getElementById("lastName").value = "";
            document.getElementById("createPassword").value = "";
            document.getElementById("confirmPassword").value = "";

            hiddenForm.style.display = "none";

            alert(message);
        } else {
            if (response.status == 409) {
                const data = await response.json();
                const message = data["Message"];

                if (message == "User name already exists") {
                    document.getElementById("createUserName").value = "";
                    alert("User name is already taken, please select a new one");
                } else {
                    document.getElementById("createEmail").value = "";
                    alert("There is already an email with this account, please log into that one");
                }
            } else if (response.status == 500) {
                alert("There was an error trying to create your account please try again in a few momenets");
            }
        }
    } catch (error) {
        console.error("Caught error:", error.message);
    }
    
}

function validate_password(password) {
    const regex = /^(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{6,16}$/;
    if (regex.test(password) == false) {
        console.log("password invalid");
    }
    return regex.test(password)
}

function validate_email(email) {
    const regex = /^((?!\.)[\w\-_.]*[^.])(@\w+)(\.\w+(\.\w+)?[^.\W])$/
    if (regex.test(email) == false) {
        console.log("email invalid")
    }
    return regex.test(email)
}