const URL = "http://localhost:8440";

function sendRequestLogin(user, password) {
    let formData = new FormData();
    formData.append('username', user);
    formData.append('password', password);

    return sendRequest("/auth?action=login", formData);
}

function sendRequestLogout() {
    return sendRequest("/auth?action=logout", undefined);
}

function sendRequestCheckSession() {
    return sendRequest("/auth?action=check", undefined);
}

function sendRequestCheckIn(user, password) {
    let formData = new FormData();
    formData.append('username', user);
    formData.append('password', password);

    return sendRequest("/checkin?action=log", formData);
}

function sendRequestList(type, value) {
    let formData = new FormData();
    formData.append('type', type);
    formData.append('value', value);

    return sendRequest("/admin?action=list", formData);
}

async function sendRequestPoll() {
    return (await sendRequest("/admin?action=poll", undefined)).msg;
}

function sendRequestCheckPerm(node) {
    return new Promise((resolve, reject) => {
        let formData = new FormData();
        formData.append('node', node);

        return sendRequest("/auth?action=checkperm", formData)
            .then((a) => resolve(a.status === 200))
            .catch((b) => reject(b));
    });
}


function sendRequestDeleteLog(id) {
    let formData = new FormData();
    formData.append('id', id);

    return sendRequest("/admin?action=delete", formData);
}

function sendRequestUserUpdate(id, column, data) {
    let formData = new FormData();
    formData.append('id', id);
    formData.append('column', column);
    formData.append('data', data);
    return sendRequest("/admin?action=userupdate", formData);
}

function sendRequestUserList(adminOnly = true) {
    let formData = new FormData();
    formData.append('adminonly', adminOnly);
    return sendRequest("/admin?action=userlist", formData);
}

function sendRequest(argument, body) {
    return new Promise((resolve, reject) => {
        fetch(URL + argument, {
            method: "post",
            cache: 'no-cache',
            mode: 'cors',
            credentials: 'include',
            body: body,
        }).then(a => {

            a.text().then(b => {
                a.msg = b;
                resolve(a)
            });
        }).catch(reason => {
            reject(reason);
        });
    });
}