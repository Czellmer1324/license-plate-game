const url = "https://license-plate-game-backend.onrender.com";
let currentGroup;
let endDateSeconds;
let userName;
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
    localStorage.removeItem("group");
    window.location.assign("game.html");
})

document.getElementById("groupsBtn").addEventListener('click', () => {
    localStorage.removeItem("group");
    window.location.assign("groups.html");
})

document.getElementById("logOutBtn").addEventListener("click", function () {
    localStorage.clear();
    window.location.replace("index.html")
})

/***************************************************
 EVENT LISTENERS
 ***************************************************/
window.addEventListener("load", function() {
    const groupInfo = JSON.parse(localStorage.getItem("group"));
    userName = JSON.parse(localStorage.getItem("userInfo"))["User Name"]
    document.getElementById("displayUserName").textContent = userName;
    loadGroupInfo(groupInfo).then((group) => {
        currentGroup = group;
        if (userName === currentGroup["groupOwnerUserName"]) {
            document.getElementById("dangerPanel").remove();
        } else {
            document.getElementById("leaveGroupBtn").addEventListener('click', () => {
                leaveGroup();
            });
        }
        document.getElementById("groupName").textContent = currentGroup["groupName"];
        endDateSeconds = Math.floor((new Date(currentGroup["endDate"]).getTime()) / 1000);
        updateCountDown();
        createMemberCards();
    }).catch()
});

/***************************************************
 COUNT DOWN TIMER
 ***************************************************/
let timeInterval = setInterval(updateCountDown, 1000);

 function updateCountDown() {
    let currentDateSeconds = Math.floor(Date.now() / 1000);
    if (endDateSeconds === 0) {
        const countDown = document.getElementById("countdownTimer");
        countDown.replaceChildren();
        const span = document.createElement("span");
        span.classList.add("time-label");
        span.textContent = "No end date";
        countDown.appendChild(span);
    }

    if (currentDateSeconds >= endDateSeconds) {
        clearInterval(timeInterval);
        return;
    }

    let timeTilEnd = endDateSeconds - currentDateSeconds

    let days = Math.floor(timeTilEnd / 3600 / 24);
    let remainingSeconds = timeTilEnd % (3600 * 24);
    let hours = Math.floor(remainingSeconds / 3600);
    remainingSeconds = (remainingSeconds % 3600);
    let minutes = Math.floor(remainingSeconds / 60);
    let seconds = remainingSeconds % 60;

    document.getElementById("days").textContent = days.toString();
    document.getElementById("hours").textContent = hours.toString();
    document.getElementById("minutes").textContent = minutes.toString();
    document.getElementById("seconds").textContent = seconds.toString();

    if (timeTilEnd <= 0) clearInterval(timeInterval);
}

/***************************************************
    MEMBERS
 ***************************************************/
function createMemberCards() {
    const members = currentGroup["members"];
    const memberList = document.getElementById("memberList");
    members.forEach((member) => {
        if (member["userName"] !== userName) {
            // create button
            const button = document.createElement("button");
            button.classList.add("member-item");

            // add the username
            const memberName = document.createElement("span");
            memberName.classList.add("member-name");
            memberName.textContent = member["userName"];
            button.appendChild(memberName);

            // add the state progress
            const progress = document.createElement("span");
            progress.classList.add("member-progress");
            progress.textContent = member["numFound"] + " / 51 states";
            button.appendChild(progress);

            // add the event listener
            button.addEventListener('click', () => {
                button.disabled = true;
                loadMemberInfo(member["userId"]).then( (memberStates)=> {
                    const memberInfo = {
                        "userName" : member["userName"],
                        "statesFound" : member["numFound"],
                        "statesFoundList" : memberStates,
                        "color" : member["userColor"]
                    }

                    localStorage.setItem("memberInfo", JSON.stringify(memberInfo));
                    button.disabled = false;
                    window.location.assign("memberMap.html")
                }).catch( ()=> {
                    button.disabled = false;
                })
            })

            memberList.appendChild(button);
        }
    })

    if (members.length === 1) {
        const h3 = document.createElement("h3");
        h3.textContent = "No current members";
        memberList.appendChild(h3);
    }
}

async function loadMemberInfo(memberId) {
    const response = await fetch(url + "/group/member-map/" + memberId + "/" + currentGroup["groupId"], {
        method : "GET",
        headers : {
            "Authorization" : "Bearer " + localStorage.getItem("jwt")
        }
    })

    if (response.ok) {
        return await response.json();
    } else if (response.status === 401) {
        alert("You need to sign in again");
        localStorage.clear();
        window.location.replace("index.html");
        throw new Error("Unauthorized");
    } else {
        alert("Something went wrong try again");
        throw new Error("Something went wrong");
    }
}

async function loadGroupInfo(group) {
    const response = await fetch(url + "/group/info/" + group["groupId"], {
        method : "GET",
        headers : {
            "Authorization" : "Bearer " + localStorage.getItem("jwt")
        }
    })

    if (response.ok) {
        return await response.json();
    } else if (response.status === 401) {
        alert("You need to sign in again");
        localStorage.clear();
        window.location.replace("index.html");
        throw new Error("Unauthorized");
    } else {
        alert("Something went wrong try again");
        throw new Error("Something went wrong");
    }
}

/***************************************************
 LEAVE GROUP
 ***************************************************/
function leaveGroup() {
    const confirm = window.confirm("Are you sure you want to leave this group?");
    const buttonClicked = document.getElementById("leaveGroupBtn");
    if (confirm) {
        buttonClicked.disabled = true;
        dbLeaveGroup().then(() => {
            localStorage.removeItem("group");
            buttonClicked.disabled = false;
            window.location.replace("game.html");
        }).catch(() => {
            buttonClicked.disabled = false;
        })
    }
}

async function dbLeaveGroup() {
    const response = await fetch(url + "/group/leave-group/" + currentGroup["groupId"], {
        method : "DELETE",
        headers : {
            "Authorization" : "Bearer " + localStorage.getItem("jwt")
        }
    })

    if (response.ok) {
        alert("You have left the group.")
    } else if (response.status === 401) {
        alert("You need to sign in again");
        localStorage.clear();
        window.location.replace("index.html");
        throw new Error("Unauthorized");
    } else {
        alert("Something went wrong try again");
        throw new Error("Something went wrong");
    }
}