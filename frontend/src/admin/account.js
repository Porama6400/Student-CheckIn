sendRequestCheckSession().then(data => {
    if (data.msg !== "AUTH/OK") window.location.href = "./"
});

sendRequest("/admin?action=listadmin").then(a => {
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
            sessionStorage.setItem("sc_edit_id",entry.id);
            window.location.href = "./edit.html"
        };

        tbody.appendChild(clone);


    })
});