function auth() {
    let email = document.getElementById("email").value
    let password = document.getElementById("password").value

    const xhr = new XMLHttpRequest()

    xhr.open('POST', 'login/send')
    xhr.setRequestHeader('Content-Type', 'application/json')

    xhr.onreadystatechange = function () {
        if (this.readyState !== 4) return

        if (this.status === 200) {
            window.location.replace("/")
        } else {
            alert("Incorrect email or password!")
        }
    };


    let loginJson = JSON.stringify({
        email: email,
        password: password
    })

    xhr.send(loginJson)
}

function getEvent() {
    let name = document.getElementById("name").value
    let description = document.getElementById("description").value
    let location = document.getElementById("location").value
    let dateStart = document.getElementById("date-start").value
    let timeStart = document.getElementById("time-start").value
    let dateEnd = document.getElementById("date-end").value
    let timeEnd = document.getElementById("time-end").value
    let groups = getCheckedCheckBoxes('groups[]')


    let xhr = new XMLHttpRequest();
    xhr.open("POST", "new_event", true)

    xhr.setRequestHeader("Accept", "application/json")
    xhr.setRequestHeader("Content-Type", "application/json")

    xhr.onreadystatechange = function () {
        if (this.readyState !== 4) return

        if (this.status === 200) {
            window.location.replace("/")
        } else {
            alert("Incorrect event data!")
        }
    };

    xhr.onload = () => console.log(xhr.responseText)

    let eventJson = `{
        "name": "${name}",
        "description": "${description}",
        "location": "${location}",
        "startDate": "${dateStart}",
        "startTime": "${timeStart}",
        "endDate": "${dateEnd}",
        "endTime": "${timeEnd}",
        "groups": ${JSON.stringify(groups)}
    }`

    xhr.send(eventJson)
}

function getCheckedCheckBoxes(name) {
    let checkboxes = document.getElementsByName(name)
    let checkboxesChecked = []
    for (let index = 0; index < checkboxes.length; index++) {
        if (checkboxes[index].checked) {
            checkboxesChecked.push(checkboxes[index].value)
        }
    }
    return checkboxesChecked
}
