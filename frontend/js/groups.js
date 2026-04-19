const url = "http://localhost:8080";
let invites;
let ownedGroup;
let groups;

/***************************************************
 DROP DOWN MENU
 ***************************************************/
document.getElementById("dropDownBtn").addEventListener("click", function (event) {
    document.getElementById("dropdownLinks").classList.toggle("show");
    event.stopPropagation();
});

// Close the dropdown if the user clicks outside of it
window.onclick = function(event) {
    if (!event.target.closest('.dropdown')) {
        document.getElementById("dropdownLinks").classList.remove("show");
    }
}

document.getElementById("homeBtn").addEventListener('click', () => {
    window.location.assign("game.html");
})

document.getElementById("logOutBtn").addEventListener("click", function () {
    localStorage.clear();
    window.location.replace("index.html")
})

const modal = document.getElementById("createGroupModal");

document.getElementById("createBtn").addEventListener("click", () => {
    createBtnActions();
})

function createBtnActions() {
    document.getElementById("dropdownLinks").classList.remove("show");
    modal.classList.add("show");
}

document.getElementById("cancelCreateGroup").addEventListener("click", () => {
    modal.classList.remove("show");
});

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

document.getElementById("createGroupForm").addEventListener('submit', (event)=> {
    event.preventDefault();
    const buttonClicked = document.getElementById("submitCreateGroup");
    buttonClicked.disabled = true;
    const groupName = document.getElementById("groupName").value;
    const date = document.getElementById("endDate").value;

    let formatedDate;

    if (date === "") {
        formatedDate = date;
    } else {
        formatedDate = formatDate(date);
    }

    createGroupForUser(groupName, formatedDate).then((group) => {
        // should probably make it so that the group returns everything
        ownedGroup = group;
        addGroup(group);
        updateOwnedGroup();
        buttonClicked.disabled = false;
        modal.classList.remove("show");
    }).catch()
})

function formatDate(date) {
    const offsetInMinutes = -(new Date().getTimezoneOffset());
    const sign = offsetInMinutes >= 0 ? "+" : "-";
    const hours = String(Math.floor(Math.abs(offsetInMinutes) / 60)).padStart(2, "0");
    const minutes = String(Math.abs(offsetInMinutes) % 60).padStart(2, "0");
    const offset = sign + hours + ":" + minutes;

    return (date + "T23:59:59.999" + offset);
}

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
    groupsList.replaceChildren();
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
            button.textContent = group["groupName"] + " by " + group["groupOwnerUserName"];
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

async function createGroupForUser(groupName, endDate) {

    const requestObj = {
        "groupName" : groupName,
        "endDate" : endDate
    }

    const response = await fetch(url + "/group/create", {
        method : "POST",
        headers : {
            'Authorization' : 'Bearer ' + localStorage.getItem('jwt'),
            'Content-Type': 'application/json'
        },
        body : JSON.stringify(requestObj)
    })

    if (response.ok) {
        const responseInfo = await response.json();
        const group = responseInfo["Group"];
        alert(responseInfo["Message"]);
        return group;
    } else if (response.status === 401) {
        alert("You need to sign in again");
        localStorage.clear();
        window.location.replace("index.html");
        throw new Error("Unauthorized");
    } else {
        alert("Something went wrong, try again");
        throw new Error("Something went wrong");
    }
}

/***************************************************
 OWNED GROUP
 ***************************************************/
function createOwnedGroup() {
    document.getElementById("owned-loading").remove();
    createOwnedGroupText()
}

function updateOwnedGroup() {
    document.getElementById("empty-owned").remove();
    createOwnedGroupText();
}

function removeOwnedGroupFromScreen() {
    document.getElementById("ownedGroupArea").replaceChildren();
    createOwnedGroupText();
}

function createOwnedGroupText() {
    const ownedGroupSection = document.getElementById("ownedGroupArea");
    if (ownedGroup === undefined) {
        const h3 = document.createElement("h3");
        h3.classList.add("empty-state");
        h3.id = "empty-owned"
        h3.textContent = "You do not own a group";
        ownedGroupSection.appendChild(h3);
    } else {
        // REMOVE THE CREATE GROUP BUTTON FROM THE DROPDOWN MENU
        document.getElementById("createBtn").remove();
        document.getElementById("endDateInputLabel").textContent = "Current: " + dateLabel();
        createOwnedGroupMembers();
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

function dateLabel() {
    if (ownedGroup["endDate"] === null) {
        return  "None";
    } else {
        const utcDate = new Date(ownedGroup["endDate"]);
        return utcDate.toLocaleString();
    }
}

function openManageGroup() {
    document.getElementById("manageGroupModal").classList.add("show");
}

function closeManageGroup() {
    document.getElementById("manageGroupModal").classList.remove("show");
}

function manageGroup() {
    openManageGroup();
}

document.getElementById("closeManageBtn").addEventListener("click", closeManageGroup);

document.getElementById("updateEndDateBtn").addEventListener('click', () => {
    const buttonClicked = document.getElementById("updateEndDateBtn");
    buttonClicked.disabled = true;
    const endDate = document.getElementById("endDateInput").value;
    let formattedDate;
    if (endDate === "") {
        formattedDate = endDate;
    } else {
        formattedDate = formatDate(endDate);
    }
    const currentEnd = new Date(ownedGroup["endDate"]).toUTCString()

    if (new Date(formattedDate).toUTCString() === currentEnd) {
        alert("End date updated");
        document.getElementById("endDateInput").value = "";
        buttonClicked.disabled = false;
        return
    }
    updateEnd(formattedDate).then((newDate) => {
        document.getElementById("endDateInput").value = "";
        ownedGroup["endDate"] = newDate;
        document.getElementById("endDateInputLabel").textContent = "Current: " + dateLabel();
    }).catch()

    buttonClicked.disabled = false;
})

async function updateEnd(endDate) {
    const requestObj = {
        "newDate" : endDate
    }
    const response = await fetch(url + "/group/update-end", {
        method : "PUT",
        headers : {
            'Authorization' : 'Bearer ' + localStorage.getItem('jwt'),
            'Content-Type': 'application/json'
        },
        body : JSON.stringify(requestObj)
    })

    if (response.ok) {
        const responseInfo = await response.json();
        alert(responseInfo["Message"]);
        return responseInfo["NewDate"];
    } else if (response.status === 401) {
        alert("You need to sign in again");
        localStorage.clear();
        window.location.replace("index.html");
        throw new Error("Unauthorized");
    } else {
        alert("Something went wrong, try again");
        throw new Error("Something went wrong");
    }
}

function createOwnedGroupMembers() {
    loadGroupMembers().then((members) => {
        createOwnedGroupMembersCards(members);
    }).catch()
}

async function loadGroupMembers() {
    const response = await fetch(url + "/group/info/" + ownedGroup["groupId"], {
        method : "GET",
        headers : {
            "Authorization" : "Bearer " + localStorage.getItem("jwt")
        }
    })

    if (response.ok) {
        const responseInfo = await response.json();
        return responseInfo["members"];
    } else if (response.status === 401) {
        alert("You need to sign in again");
        localStorage.clear();
        window.location.replace("index.html");
        throw new Error("Unauthorized");
    } else {
        alert("Something went wrong, try again");
        throw new Error("Something went wrong");
    }
}

function createOwnedGroupMembersCards(members) {
    const memberContainer = document.getElementById("memberList");
    if (members.length === 1) {
        const h3 = document.createElement("h3");
        h3.classList.add("empty-state");
        h3.textContent = "No current members";
        memberContainer.appendChild(h3);
    } else {
        members.forEach( function (member) {
            if (member["userName"] !== ownedGroup["groupOwnerUserName"]) {
                // create the div that holds the invite and add the class to it
                const memberDiv = document.createElement("div");
                memberDiv.classList.add("list-item");

                // create the div that holds the group name
                const innerDiv = document.createElement("div");
                innerDiv.classList.add("member-info");

                // create the h3 of the group name
                const h3 = document.createElement("h3");
                h3.textContent = member["userName"];
                innerDiv.appendChild(h3);

                // append this inner div to the main invite div
                memberDiv.appendChild(innerDiv)

                // create div to hold buttons
                const buttonDiv = document.createElement("div")
                buttonDiv.classList.add("member-actions")

                //create the accept button
                const removeBtn = document.createElement("button")
                removeBtn.textContent = "Remove";
                removeBtn.classList.add("decline-btn");
                removeBtn.addEventListener('click', function () {
                    removeBtn.disabled = true;
                    removeMember(member["userName"]).then(() => {
                        memberDiv.remove();
                        removeBtn.disabled = false;
                        const h3 = document.createElement("h3");
                        h3.classList.add("empty-state");
                        h3.textContent = "No current members";
                        memberContainer.appendChild(h3);
                    }).catch(() => {
                        removeBtn.disabled = false;
                    })
                })
                buttonDiv.appendChild(removeBtn);

                // append button div to invite div
                memberDiv.appendChild(buttonDiv);

                //append invite div to parent
                memberContainer.appendChild(memberDiv);
            }
        })
    }
}

async function removeMember(memberUserName) {
    const response = await fetch(url + "/group/remove-user/" + memberUserName, {
        method : "PUT",
        headers : {
            "Authorization" : "Bearer " + localStorage.getItem("jwt")
        }
    })

    if (response.ok) {
        const responseInfo = await response.json();
        alert(responseInfo["Message"]);
    } else if (response.status === 401) {
        alert("You need to sign in again");
        localStorage.clear();
        window.location.replace("index.html");
        throw new Error("Unauthorized");
    } else {
        alert("Something went wrong, try again");
        throw new Error("Something went wrong");
    }
}

document.getElementById("deleteGroupBtn").addEventListener('click', () => {
    const buttonClicked = document.getElementById("deleteGroupBtn")
    buttonClicked.disabled = true;
    const confirm = window.confirm("Are you sure you want to delete your group?");
    if (confirm) {
        deleteGroup().then(() => {
            groups = groups.filter(i => i["groupId"] !== ownedGroup["groupId"]);
            ownedGroup = undefined;
            createGroups();
            removeOwnedGroupFromScreen();
            const dropDown = document.getElementById("dropdownLinks");
            const createBtn = document.createElement("button");
            createBtn.id = "createBtn";
            createBtn.textContent = "Create Group";
            createBtn.addEventListener('click', () => {
                createBtnActions();
            })
            dropDown.appendChild(createBtn);
            buttonClicked.disabled = false;
            closeManageGroup();
        }).catch()
    }
})

async function deleteGroup() {
    const response = await fetch(url + "/group/delete", {
        method : 'DELETE',
        headers: {
            'Authorization' : 'Bearer ' + localStorage.getItem('jwt')
        }
    })

    if (response.ok) {
        const message = await response.json();
        alert(message["Message"]);
    } else if (response.status === 401) {
        alert("You need to sign in again");
        localStorage.clear();
        window.location.replace("index.html");
        throw new Error("Unauthorized");
    } else {
        alert("Something went wrong, try again");
        throw new Error("Something went wrong");
    }
}