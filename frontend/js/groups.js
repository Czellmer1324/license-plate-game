const url = "http://localhost:8080";
let invites;
let ownedGroup;

window.addEventListener("load", function() {
    getInvites().then(function () {
        createInvites();
    }).catch()
});

async function getInvites() {
    const response = await fetch(url + "/user/invites", {
        method: "GET",
        headers: {
            'Authorization': "Bearer " + localStorage.getItem("jwt")
        }
    })

    if (response.ok) {
        invites = await response.json();
    } else if (response.status === 401) {
        alert("you need to log in again!");
        window.location.replace("index.html");
    } else {
        alert("Something went wrong, try reloading page.")
    }
}

// CREATE INVITES
function createInvites() {
    const inviteContainer = document.getElementById("invitesList");
    document.getElementById("invite-loading").remove();
    if (invites.length === 0) {
        const h3 = document.createElement("h3");
        h3.classList.add("empty-state")
        h3.textContent = "No pending invites"
        inviteContainer.appendChild(h3);
    } else {
        invites.forEach( function (invite) {
            const inviteId = invite["inviteId"]
            // create the div that holds the invite and add the class to it
            const inviteDiv = document.createElement("div");
            inviteDiv.classList.add("list-item");
            inviteDiv.id = "invite" + inviteId;

            // create the div that holds the group name
            const innerDiv = document.createElement("div");
            innerDiv.classList.add("invite-info");

            // create the h3 of the group name
            const h3 = document.createElement("h3");
            h3.textContent = invite["groupGroupName"];
            innerDiv.appendChild(h3);

            // create the invited by
            const invitedBy = document.createElement("p");
            invitedBy.textContent = "Invited by: " + invite["groupGroupOwnerUserName"];
            innerDiv.appendChild(invitedBy);

            // append this inner div to the main invite div
            inviteDiv.appendChild(innerDiv)

            // create div to hold buttons
            const buttonDiv = document.createElement("div")
            buttonDiv.classList.add("invite-actions")

            //create the accept button
            const acceptBtn = document.createElement("button")
            acceptBtn.textContent = "Accept";
            acceptBtn.classList.add("accept-btn");
            acceptBtn.addEventListener('click', function () {
                acceptBtn.disabled = true;
                acceptInvite(inviteId).then(() => {
                    invites.filter(i => i["inviteId"] !== inviteId);
                    removeInviteFromScreen("invite" + inviteId);
                    acceptBtn.disabled = false;
                }).catch(() => {
                    acceptBtn.disabled = false;
                    alert("Something went wrong");
                })
            })
            buttonDiv.appendChild(acceptBtn);

            //create the decline button
            const declineBtn = document.createElement("button")
            declineBtn.textContent = "Decline";
            declineBtn.classList.add("decline-btn");
            declineBtn.addEventListener('click', function () {
                declineBtn.disabled = true;
                declineInvite(inviteId).then(() => {
                    invites.filter(i => i["inviteId"] !== inviteId);
                    removeInviteFromScreen("invite" + inviteId);
                    declineBtn.disabled = false;
                }).catch(() => {
                    declineBtn.disabled = false;
                    alert("Something went wrong");
                })
            })
            buttonDiv.appendChild(declineBtn);

            // append button div to invite div
            inviteDiv.appendChild(buttonDiv);

            //append invite div to parent
            inviteContainer.appendChild(inviteDiv);
        })
    }
}

function removeInviteFromScreen(divId) {
}

async function acceptInvite(inviteId) {

}

async function declineInvite(inviteId) {

}