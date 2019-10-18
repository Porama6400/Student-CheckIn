//const id = sessionStorage.getItem("sc_edit_id");
//sessionStorage.removeItem("sc_edit_id");
const uid = getGetParam("id");

var adminOnly = localStorage.getItem("editAdminOnly");
if (adminOnly === undefined) adminOnly = true;

var data;

function display() {
    if (data.name === undefined) data.name = "";
    if (data.nick === undefined) data.nick = "";
    if (data.classroom === undefined) data.classroom = "";
    if (data.email === undefined) data.email = "";
    if (data.perm === undefined) data.perm = "";

    document.getElementById("input-id").value = data.id;
    document.getElementById("input-name").value = data.name;
    document.getElementById("input-nick").value = data.nick;
    document.getElementById("input-classroom").value = data.classroom;
    document.getElementById("input-email").value = data.email;

    var buffer = null;
    JSON.parse(data.perm).forEach(perm => {

        if (buffer == null) buffer = perm;
        else buffer += "\n" + perm;
    });
    document.getElementById("input-perms").value = buffer;
}

window.onload = function () {
    sendRequestUserList(adminOnly).then(a => {
        var obj = JSON.parse(a.msg);

        obj.forEach(b => {
            // noinspection EqualityComparisonWithCoercionJS
            if (b.id == uid) {
                this.data = b;
                display();
            }
        })
    });

    sendRequestCheckPerm("admin.account.grant").then(grant => {
        if(!grant){
            document.getElementById("input-perms").disabled = true;
        }
    })

    sendRequestCheckPerm("admin.account.password").then(grant => {
        if(!grant){
            document.getElementById("input-password").disabled = true;
        }
    })
};