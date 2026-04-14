const url = "localhost:8080/game";
const found = new Map;
const states = new Map([
    ["Arizona", 'AZ'],
    ['Alabama', 'AL'],
    ['Alaska', 'AK'],
    ['Arkansas', 'AR'],
    ['California', 'CA'],
    ['Colorado', 'CO'],
    ['Connecticut', 'CT'],
    ['Delaware', 'DE'],
    ['Florida', 'FL'],
    ['Georgia', 'GA'],
    ['Hawaii', 'HI'],
    ['Idaho', 'ID'],
    ['Illinois', 'IL'],
    ['Indiana', 'IN'],
    ['Iowa', 'IA'],
    ['Kansas', 'KS'],
    ['Kentucky', 'KY'],
    ['Louisiana', 'LA'],
    ['Maine', 'ME'],
    ['Maryland', 'MD'],
    ['Massachusetts', 'MA'],
    ['Michigan', 'MI'],
    ['Minnesota', 'MN'],
    ['Mississippi', 'MS'],
    ['Missouri', 'MO'],
    ['Montana', 'MT'],
    ['Nebraska', 'NE'],
    ['Nevada', 'NV'],
    ['New Hampshire', 'NH'],
    ['New Jersey', 'NJ'],
    ['New Mexico', 'NM'],
    ['New York', 'NY'],
    ['North Carolina', 'NC'],
    ['North Dakota', 'ND'],
    ['Ohio', 'OH'],
    ['Oklahoma', 'OK'],
    ['Oregon', 'OR'],
    ['Pennsylvania', 'PA'],
    ['Rhode Island', 'RI'],
    ['South Carolina', 'SC'],
    ['South Dakota', 'SD'],
    ['Tennessee', 'TN'],
    ['Texas', 'TX'],
    ['Utah', 'UT'],
    ['Vermont', 'VT'],
    ['Virginia', 'VA'],
    ['Washington', 'WA'],
    ['Washington, D.C.', 'DC'],
    ['West Virginia', 'WV'],
    ['Wisconsin', 'WI'],
    ['Wyoming', 'WY']
]);

window.addEventListener("load", function() {
    replaceUserName();
    createStateList();
});

function replaceUserName() {
    const old = document.getElementById("replaceWithUser");
    const newEl = document.createElement("p");
    newEl.textContent = "Hello, " + JSON.parse(localStorage.getItem("userInfo"))["User Name"] + "!";
    newEl.id = "displayUserName";
    old.replaceWith(newEl);
}

async function createStateList() {
    await getUserFoundStates();
    const ul = document.getElementById("stateList")
    states.forEach (function(value, key) {
        const button = document.createElement("button");
        const li = document.createElement("li");
        li.textContent = key;

        button.appendChild(li);
        button.classList.add("stateButton");
        button.id = key;
        button.addEventListener("click", () => {
            stateClick(button.id)
        })

        if (found.has(value)) {
            button.classList.add("found");
            const mapPath = document.querySelector('[data-id="' + value + '"]');
            mapPath.classList.add("mapFound");
        } else {
            button.classList.add("notFound");
        }
        
        ul.appendChild(button);
    });

    updateStateCount();
}

function stateClick(id) {
    const stateCode = states.get(id);
    const buttonClicked = document.getElementById(id);

    if (buttonClicked.classList.contains("notFound")) {
        markState(stateCode, buttonClicked);
    } else {
        unmarkState(stateCode, buttonClicked);
    }
}

async function unmarkState(stateCode, button) {
    const markedId = found.get(stateCode);
    const response = await fetch(url + "/unmark-state/" + markedId, {
        method: "DELETE",
        headers: {
            'Authorization': "Bearer " + localStorage.getItem("jwt")
        },
    })

    if (response.ok) {
        const data = await response.json();
        // remove the state from found
        found.delete(stateCode);
        // update the state count
        updateStateCount();
        // add found to button class
        button.classList.remove("found");
        button.classList.add("notFound");
        // TODO: update the map to change color of the state found
        const mapPath = document.querySelector('[data-id="' + stateCode + '"]');
        mapPath.classList.remove("mapFound");
    } else {
        if (response.status == 401) {
            alert("you need to log in again!");
            window.location.replace("index.html");
        } else {
            alert("something went wrong please try again");
        }
    }
}

async function markState(stateCode, button) {
    const requestObj = {
        "stateCode": stateCode
    }
    const response = await fetch(url + "/mark-state", {
        method: "POST",
        headers: {
            'Authorization': "Bearer " + localStorage.getItem("jwt"),
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestObj)
    })

    if (response.ok) {
        const data = await response.json();
        // add the state to found
        found.set(data["stateCode"], data["spottedId"]);
        // update the state count
        updateStateCount();
        // add found to button class
        button.classList.remove("notFound");
        button.classList.add("found");
        // TODO: update the map to change color of the state found
        const mapPath = document.querySelector('[data-id="' + data.stateCode + '"]');
        mapPath.classList.add("mapFound");
    } else {
        if (response.status == 401) {
            alert("you need to log in again!");
            window.location.replace("index.html");
        } else {
            alert("something went wrong please try again");
        }
    }
}

async function getUserFoundStates() {
    const response = await fetch(url + "/marked", {
        method: 'GET',
        headers: {'Authorization': "Bearer " + localStorage.getItem("jwt")}
    });

    if (response.ok) {
        const data = await response.json();
    
        for (var i = 0; i < data.length; i++) {
            var obj = data[i];
            found.set(obj["stateCode"], obj["spottedId"]);
        }

    } else {
        alert("You need to sign in again!");
        window.location.replace("index.html");
    }
}

function updateStateCount() {
    const old = document.getElementById("replaceStateCount");
    const newEl = document.createElement("p");
    
    newEl.textContent = found.size + "/51";
    newEl.id = "replaceStateCount";
    old.replaceWith(newEl);
}

function filterStates() {
    const input = document.getElementById("stateSearch").value.toLowerCase();
    const stateButtons = document.getElementsByClassName("stateButton");

    for (var i = 0; i < stateButtons.length; i++) {
        const button = stateButtons[i];
        const val = button.textContent.toLowerCase();
        
        if (val.includes(input)) {
            button.classList.remove("hidden")
        } else {
            button.classList.add("hidden");
        }
    }

}