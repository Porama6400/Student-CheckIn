sendRequestCheckSession().then(data => {
    if (data.msg !== "AUTH/OK") window.location.href = "./"
});

let listData;
let lastLogUpdate = 0;
let needUpdate = false;

window.onload = function () {
    var date = new Date();
    document.getElementById("search-datepicker").value =
        date.getFullYear()
        + "-" + ("00" + (date.getMonth() + 1)).substr(-2)
        + "-" + ("00" + date.getDate()).substr(-2);


    updateListData();

    document.getElementById("search-datepicker").onchange = function () {
        document.getElementById("search-datepicker-enable").checked = true;
        updateListData();
    };

    document.getElementById("search-name").oninput = function () {
        document.getElementById("search-name-enable").checked = true;
        needUpdate = true;
    };

    document.getElementById("search-class").oninput = function () {
        document.getElementById("search-class-enable").checked = true;
        needUpdate = true;
    };

    document.querySelector("#menu").querySelectorAll("input[type=radio]").forEach((a) => {
        a.onclick = (a) => {
            updateListData();
        }
    });

    sendRequestCheckPerm("admin.account.view").then(a => {
        if (a) document.getElementById("btn-manage-user").hidden = undefined;
    });
};

document.addEventListener('keyup', function (e) {
    if (e.key === "Enter") {
        updateListData(true);
    }
});

async function updateListData(verbose) {
    if (verbose === undefined) verbose = false;

    let hasDeletePerm = await sendRequestCheckPerm("admin.log.delete");

    let type = undefined;
    let value = undefined;

    if (document.getElementById('search-datepicker-enable').checked) {
        type = "date";
        value = document.getElementById('search-datepicker').value;
    } else if (document.getElementById('search-name-enable').checked) {
        type = "name";
        value = document.getElementById('search-name').value;
        if (value.replace(/[%]/g, "").length < 2) {
            if (verbose) setTimeout(() => {
                alert("Name have to be longer than 2 characters!");
            }, 100);
            return;
        }
    } else if (document.getElementById('search-class-enable').checked) {
        type = "class";
        value = document.getElementById('search-class').value;
    }

    sendRequestList(type, value).then((result) => {

        if (result.status !== 200) {
            if (result.status === 401) {
                if (verbose) alert("You do not have permission to see list");
                document.location.href = "./";
            }
            return;
        }

        needUpdate = false;
        sendRequestPoll().then((data) => {
            lastLogUpdate = parseInt(data);
        });

        listData = JSON.parse(result.msg);


        let table = document.querySelector("#list_table");
        let tbody = table.querySelector("tbody");
        let template = table.querySelector("template");

        tbody.innerHTML = "";
        listData.forEach((entry) => {

            var clone = document.importNode(template.content, true);
            var td = clone.querySelectorAll("td");
            td[0].textContent = entry.name;
            td[1].textContent = entry.nick;
            td[2].textContent = entry.classroom;
            td[3].textContent = entry.time;

            let button = td[4].querySelector("button");
            button.onclick = function () {
                sendRequestDeleteLog(entry.id).then((result) => {
                    updateListData();
                });
            };
            if (!hasDeletePerm) button.hidden = true;

            tbody.appendChild(clone);


        })
    });
}

setInterval(async () => {
    let polledLastUpdate = parseInt(await sendRequestPoll());
    if (lastLogUpdate !== polledLastUpdate) {
        needUpdate = false;
        updateListData(true);
    }
}, 3000);

setInterval(async () => {
    if (needUpdate) {
        needUpdate = false;
        updateListData();
    }
}, 500);