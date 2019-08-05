document.querySelector("#form_login_submit").onclick = (e) => {
    e.preventDefault();
    sendRequestLogin(
        document.querySelector("#form_login_username").value,
        document.querySelector("#form_login_password").value,
    ).then(async data => {
        if (data.msg === "AUTH/SUCCESS" || data.msg === "AUTH/AUTHENTICATED") {
            if (await sendRequestCheckPerm("admin") === false) {
                alert("You do not have permission to access admin panel!");
                document.location.href = "./";
            }
            else window.location.href = "dashboard.html";
        }
        else {
            document.getElementsByClassName("error")[0].hidden = false;
            document.getElementsByClassName("error")[0].innerHTML = "Authentication failed";
        }
    }).catch(data => {
        document.getElementsByClassName("error")[0].hidden = false;
        document.getElementsByClassName("error")[0].innerHTML = data.toString();
    });
}