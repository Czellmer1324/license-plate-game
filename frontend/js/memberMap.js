const currentMember = JSON.parse(localStorage.getItem("memberInfo"));

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

document.getElementById("backToGroupBtn").addEventListener('click', ()=> {
    localStorage.removeItem("memberInfo");
    window.location.assign("group.html");
})

document.getElementById("groupButton").addEventListener("click", function () {
    localStorage.removeItem("memberInfo");
    localStorage.removeItem("group");
    window.location.assign("groups.html");
})

document.getElementById("homeBtn").addEventListener('click', () => {
    localStorage.removeItem("memberInfo");
    localStorage.removeItem("group");
    window.location.assign("game.html");
})

document.getElementById("logOutBtn").addEventListener("click", function () {
    localStorage.clear();
    window.location.replace("index.html")
})

document.getElementById("replaceWithUser").textContent = JSON.parse(localStorage.getItem("userInfo"))["User Name"];

document.getElementById("currentMemberName").textContent = currentMember["userName"] + "'s Map";

document.getElementById("replaceStateCount").textContent = currentMember["statesFound"] + "/51";

const ul = document.getElementById("stateList");
const foundList = currentMember["statesFoundList"];
const found = [];
foundList.forEach((state) => {
    found.push(state["stateCode"]);
})
console.log(found);

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

states.forEach (function(value, key) {
    const button = document.createElement("button");
    const li = document.createElement("li");
    li.textContent = key;

    button.appendChild(li);
    button.classList.add("stateButton");
    button.id = key;

    if (found.includes(value)) {
        button.classList.add("found");
        const mapPath = document.querySelector('[data-id="' + value + '"]');
        mapPath.classList.add("mapFound");
    } else {
        button.classList.add("notFound");
    }

    ul.appendChild(button);
});