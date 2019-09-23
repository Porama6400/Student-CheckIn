const id = sessionStorage.getItem("sc_edit_id");
sessionStorage.removeItem("sc_edit_id");

sendRequest("/admin?action=listadmin").then(a => {
    var obj = JSON.parse(a.msg);

    obj.forEach(b => {
        console.log(JSON.stringify(b) + " - " + id);
        if (b.id == id) {
            load(b);
        }
    })
});

function load(data) {
    console.log(data);
}