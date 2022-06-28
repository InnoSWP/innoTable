function auth() {
    let login = document.getElementById("username").value;
    let password = document.getElementById("password").value;

    let auth = true;  //TODO make proper authentication
    if (auth) {
        window.location.href = "create_event.html"
    }
}

function getEvent() {
    let name = document.getElementById("name").value;
    let description = document.getElementById("description").value;
    let location = document.getElementById("location").value;
    let dateStart = document.getElementById("date-start").value;
    let timeStart = document.getElementById("time-start").value;
    let dateEnd = document.getElementById("date-end").value;
    let timeEnd = document.getElementById("time-end").value;
    let groups = getCheckedCheckBoxes('groups[]');


    let xhr = new XMLHttpRequest();
    xhr.open("PUT", "http://localhost:8080/api/create_event", true);

    xhr.setRequestHeader("Accept", "application/json");
    xhr.setRequestHeader("Content-Type", "application/json");

    xhr.onload = () => console.log(xhr.responseText);

    let eventJson = `{
        "name": "${name}",
        "description": "${description}",
        "location": "${location}",
        "startDate": "${dateStart}",
        "startTime": "${timeStart}",
        "endDate": "${dateEnd}",
        "endTime": "${timeEnd}",
        "groups": ${JSON.stringify(groups)}
    }`;

    xhr.send(eventJson);

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
