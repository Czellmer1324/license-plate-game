const hiddenForm = document.getElementById("hidden");
document.getElementById("signInDiv");
const signUpBtn = document.getElementById("signUpBtn");
const signInForm = document.getElementById("signInForm");
const signUpForm = document.getElementById("signUpForm");
const url = "https://license-plate-game-backend.onrender.com/user";

signUpBtn.onclick = function() {
    hiddenForm.style.display = "block";
}

document.getElementById("bottomSign").addEventListener('click', function() {
    hiddenForm.style.display = "none";
})

document.getElementById("topBackSign").addEventListener('click', function() {
    hiddenForm.style.display = "none";
})

signInForm.onsubmit  = function(event) {
    event.preventDefault();
    signIn();
}

signUpForm.onsubmit = function(event) {
    event.preventDefault();
    createAccount();
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

    try {
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
            // need to get user data and store it, then move to next page
            await getUserInfo();
            window.location.replace("game.html")
        } else {
            const data = await response.json();
            if (response.status === 401) {
                alert(data["Message"]);
            }
        }

    } catch (error) {
        console.error("Caught error:", error.message);
    }


}

async function getUserInfo() {
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {'Authorization': "Bearer " + localStorage.getItem("jwt")}
        })

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem("userInfo", JSON.stringify(data));
        }
    } catch (error) {
        console.log(error.message);
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

    try {
        const response = await fetch(url + "/create", {
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
    } catch (error) {
        console.error("Caught error:", error.message);
    }
    
}

function validate_email(email) {
    const regex = /^((?!\.)[\w\-_.]*[^.])(@\w+)(\.\w+(\.\w+)?[^.\W])$/
    if (regex.test(email) === false) {
        console.log("email invalid")
    }
    return regex.test(email)
}