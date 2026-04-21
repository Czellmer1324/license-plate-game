const signUpBtn = document.getElementById("signUpBtn");
const signInForm = document.getElementById("signInForm");
const signUpForm = document.getElementById("signUpForm");
const url = "https://license-plate-game-backend.onrender.com/user";

signUpBtn.onclick = function() {
    document.getElementById("hidden").classList.add("show");
    document.getElementById("signInDiv").classList.add("signup-active");
}

document.getElementById("bottomSign").addEventListener('click', function() {
    document.getElementById("hidden").classList.remove("show");
    document.getElementById("signInDiv").classList.remove("signup-active");
})

document.getElementById("topBackSign").addEventListener('click', function() {
    document.getElementById("hidden").classList.remove("show");
    document.getElementById("signInDiv").classList.remove("signup-active");
})

signInForm.onsubmit  = function(event) {
    event.preventDefault();
    const signInBtn = document.getElementById("signInBtn");
    signInBtn.disabled = true;
    signIn().then(()=> {
        getUserInfo().then(() => {
            signInBtn.disabled = false;
            window.location.replace("game.html")
        }).catch(() => {
            signInBtn.disabled = false;
        });
    }).catch(() => {
        signInBtn.disabled = false;
    });
}

document.querySelectorAll(".toggle-password").forEach(button => {
    button.addEventListener('click', () => {
        const inputId = button.dataset.target;
        const input = document.getElementById(inputId);
        const eye = button.children[0];

        if (input.type === "password") {
            input.type = "text";
            eye.classList.remove("fa-eye");
            eye.classList.add("fa-eye-slash");
        } else {
            input.type = "password";
            eye.classList.remove("fa-eye-slash");
            eye.classList.add("fa-eye");
        }
    })
})

signUpForm.onsubmit = function(event) {
    event.preventDefault();
    const createBtn = document.getElementById("createActSubmitBtn");
    createBtn.disabled = true;
    createAccount().then(() => {
        document.getElementById("createEmail").value = "";
        document.getElementById("createUserName").value = "";
        document.getElementById("firstName").value = "";
        document.getElementById("lastName").value = "";
        document.getElementById("createPassword").value = "";
        document.getElementById("confirmPassword").value = "";

        document.getElementById("hidden").classList.remove("show");
        document.getElementById("signInDiv").classList.remove("signup-active");
        createBtn.disabled = false;
    }).catch(() => {
        createBtn.disabled = false;
    });
}

async function signIn() {
    const email = document.getElementById("email").value.toLowerCase();
    const password = document.getElementById("password").value;

    if (email === "" || password === "") {
        alert("All fields must be filled out!");
        return;
    }

    let obj = {
        "email": email,
        "password": password
    };

    const response = await fetch(url + "/login", {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(obj)
    });

    if (response.ok) {
        // save the returned jwt
        // send them to the game page
        const data = await response.json();
        const key = data["Token"];

        localStorage.setItem("jwt", key);
    } else {
        const data = await response.json();
        if (response.status === 401) {
            alert(data["Message"]);
        }
        throw new Error("Something went wrong")
    }
}

async function getUserInfo() {
    const response = await fetch(url, {
        method: 'GET',
        headers: {'Authorization': "Bearer " + localStorage.getItem("jwt")}
    })

    if (response.ok) {
        const data = await response.json();
        localStorage.setItem("userInfo", JSON.stringify(data));
    } else {
        alert("Something went wrong try signing in again");
        throw new Error("Something went wrong");
    }
}

async function createAccount() {
    const email = document.getElementById("createEmail").value.toLowerCase();
    const userName = document.getElementById("createUserName").value;
    const firstName = document.getElementById("firstName").value;
    const lastName = document.getElementById("lastName").value;
    const password = document.getElementById("createPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    // Need to validate the email and password
    if (password !== confirmPassword) {
        alert("Your passwords do not match!");
        document.getElementById("createPassword").value = "";
        document.getElementById("confirmPassword").value = "";
        return;
    }

    if (validate_email(email) === false) {
        alert("Improper email format.");
        document.getElementById("createEmail").value = "";
        return;
    }

    let obj = {
        "userName": userName,
        "firstName": firstName,
        "lastName": lastName,
        "email": email,
        "password": password
    };

    const response = await fetch(url + "/create", {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(obj)
    });

    if (response.ok) {
        const data = await response.json();
        const message = data["Message"];

        alert(message);
    } else {
        if (response.status === 409) {
            const data = await response.json();
            const message = data["Message"];

            if (message === "User name already exists") {
                document.getElementById("createUserName").value = "";
                alert("User name is already taken, please select a new one");
            } else {
                document.getElementById("createEmail").value = "";
                alert("There is already an email with this account, please log into that one");
            }
        } else if (response.status === 500) {
            alert("There was an error trying to create your account please try again in a few moments");
        }
    }
    
}

function validate_email(email) {
    const regex = /^((?!\.)[\w\-_.]*[^.])(@\w+)(\.\w+(\.\w+)?[^.\W])$/
    if (regex.test(email) === false) {
        console.log("email invalid")
    }
    return regex.test(email)
}