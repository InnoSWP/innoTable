function getLoginPassword() {
    let login = document.getElementById("username").value;
    let password = document.getElementById("password").value;

    let auth = true;  //TODO make proper authentication
    if (auth) {
        window.location.href = "create_event.html"
    }
}

function getEvent() {
    let title = document.getElementById("name").value;
    let description = document.getElementById("description").value;
    let location = document.getElementById("location").value;
    let start_date = document.getElementById("date-start").value;
    let start_time = document.getElementById("time-start").value;
    let end_date = document.getElementById("date-end").value;
    let end_time = document.getElementById("time-end").value;
    let groups = getCheckedCheckBoxes('groups[]');

    for (const groupsKey in groups) {
        alert(groups[groupsKey]);
    }
    window.location.href = "create_event.html"

}

function getCheckedCheckBoxes(name) {
    let checkboxes = document.getElementsByName(name);
    let checkboxesChecked = [];
    for (let index = 0; index < checkboxes.length; index++) {
        if (checkboxes[index].checked) {
            checkboxesChecked.push(checkboxes[index].value);
        }
    }
    return checkboxesChecked;
}
