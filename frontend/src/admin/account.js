sendRequestCheckSession().then(data => {
    if (data.msg !== "AUTH/OK") window.location.href = "./"
});

async function run() {
    const editPerm = await sendRequestCheckPerm("admin.account.edit");
    var adminOnly = document.getElementById("checkbox-admin").checked;
    localStorage.setItem("editAdminOnly",adminOnly);
    sendRequestUserList(adminOnly).then(a => {
        var obj = JSON.parse(a.msg);
        console.log(obj);

        let table = document.querySelector("#table");
        let tbody = table.querySelector("tbody");
        let template = table.querySelector("template");

        tbody.innerHTML = "";
        obj.forEach((entry) => {

            var clone = document.importNode(template.content, true);
            var td = clone.querySelectorAll("td");
            td[0].textContent = entry.name;
            td[1].textContent = entry.nick;
            td[2].textContent = entry.email;
            td[3].textContent = entry.classroom;
            td[4].textContent = entry.perm;

            let button = td[5].querySelector("button");
            button.onclick = function () {
                window.location.href = "./edit.html?id=" + entry.id
            };
            if (!editPerm) {
                button.style.display = 'none';
            }

            tbody.appendChild(clone);
        })
    });
}

run();

window.onload = function () {
    document.getElementById("checkbox-admin").onclick = function () {
      run();
    };
};