const url = "http://localhost:8080";
let invites;
let ownedGroup;
let groups;

/***************************************************
 EVENT LISTENERS
 ***************************************************/
window.addEventListener("load", function() {
    getInvites().then(() => {
        createInvites();
    }).catch()
    loadGroups().then(() => {
        createGroups();
        createOwnedGroup();
    }).catch()
});

document.getElementById("backToGame").addEventListener('click', () => {
    window.location.assign("game.html");
})

/***************************************************
 INVITES
 ***************************************************/

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
        throw new Error("Unauthorized");
    } else {
        alert("Something went wrong, try reloading page.");
        throw new Error("Something went wrong");
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
                    invites = invites.filter(i => i["inviteId"] !== inviteId);
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
                    invites = invites.filter(i => i["inviteId"] !== inviteId);
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
    document.getElementById(divId).remove();

    if (invites.length === 0) {
        const inviteContainer = document.getElementById("invitesList")
        const h3 = document.createElement("h3");
        h3.classList.add("empty-state")
        h3.textContent = "No pending invites"
        inviteContainer.appendChild(h3);
    }
}

async function acceptInvite(inviteId) {
    const responseObj = {
        "inviteId" : inviteId
    }
    const response = await fetch(url + "/user/accept-invite", {
        method: "POST",
        headers: {
            'Authorization': "Bearer " + localStorage.getItem("jwt"),
            'Content-Type': 'application/json'
        },
        body : JSON.stringify(responseObj)

    })

    if (response.ok) {
        const message = await response.json();
        console.log(message);
        const groupJoined = message["Group"];
        addGroup(groupJoined);
        alert(message["Message"]);
    } else if (response.status === 401) {
        localStorage.clear();
        alert("You need to log in again");
        window.location.replace("index.html");
        throw new Error("Unauthorized");
    } else {
        throw new Error("Something went wrong.")
    }
}

async function declineInvite(inviteId) {
    const responseObj = {
        "inviteId" : inviteId
    }
    const response = await fetch(url + "/user/decline-invite", {
        method: "PUT",
        headers: {
            'Authorization': "Bearer " + localStorage.getItem("jwt"),
            'Content-Type': 'application/json'
        },
        body : JSON.stringify(responseObj)

    })

    if (response.ok) {
        const message = await response.json();
        alert(message["Message"]);
    } else if (response.status === 401) {
        localStorage.clear();
        alert("You need to log in again");
        window.location.replace("index.html");
        throw new Error("Unauthorized");
    } else {
        throw new Error("Something went wrong.")
    }
}

/***************************************************
 GROUPS
 ***************************************************/
async function loadGroups() {
    const response = await fetch(url + "/group/get-groups", {
        method: 'GET',
        headers: {
            'Authorization' : 'Bearer ' + localStorage.getItem('jwt')
        }
    })

    if (response.ok) {
        groups = await response.json();
    } else if (response.status === 401) {
        throw new Error("Unauthorized");
    } else {
        alert("Something went wrong, try reloading page.");
        throw new Error("Something went wrong");
    }
}

function createGroups() {
    const groupsList = document.getElementById("memberGroupsList");
    document.getElementById("groups-loading").remove();
    console.log()
    if (groups.length === 0) {
        const h3 = document.createElement("h3");
        h3.classList.add("empty-state");
        h3.id = "empty-groups-list";
        h3.textContent = "You're not in any groups";
        groupsList.appendChild(h3);
    } else {
        groups.forEach(group => {
            if (group["groupOwnerUserName"] === (JSON.parse(localStorage.getItem("userInfo"))["User Name"])) {
                ownedGroup = group;
            }
            const button = document.createElement("button");
            button.classList.add("list-item");
            button.textContent = group["groupName"];
            button.addEventListener('click', () => {
                openGroup(group)
            })
            groupsList.appendChild(button);
        })
    }
}

function addGroup(groupJoined) {
    const groupsList = document.getElementById("memberGroupsList");
    if (groups.length === 0) {
        document.getElementById("empty-groups-list").remove();
    }
    groups.push(groupJoined);
    const button = document.createElement("button");
    button.classList.add("list-item");
    button.textContent = groupJoined["groupName"];
    button.addEventListener('click', () => {
        openGroup(groupJoined)
    })
    groupsList.appendChild(button);
}

function openGroup(group) {
    alert("Opening group");
}

/***************************************************
 OWNED GROUP
 ***************************************************/
function createOwnedGroup() {
    const ownedGroupSection = document.getElementById("ownedGroupArea");
    document.getElementById("owned-loading").remove();
    console.log(ownedGroup);
    if (ownedGroup === undefined) {
        const h3 = document.createElement("h3");
        h3.classList.add("empty-state");
        h3.textContent = "You do not own a group";
        ownedGroupSection.appendChild(h3);
    } else {
        const h3 = document.createElement("h3");
        h3.classList.add("group-name");
        h3.textContent = ownedGroup["groupName"];
        ownedGroupSection.appendChild(h3);

        const button = document.createElement("button");
        button.classList.add("manage-btn");
        button.textContent = "Manage";
        button.addEventListener('click', () => {
            manageGroup()
        })
        ownedGroupSection.appendChild(button);
    }
}

function manageGroup() {
    alert("group is being managed");
}