let nav = 0;
let clicked = null;
let events = localStorage.getItem('events') ? JSON.parse(localStorage.getItem('events')) : [];

const bigCalendarBody = document.getElementById('bigCalendarBody');
const newEventModal = document.getElementById('newEventModal');
const eventModal = document.getElementById('eventModal');
const backDrop = document.getElementById('modalBackDrop');
const bigCalendarWeekdays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

const eventTitleInput = document.getElementById('eventTitleInput');
const eventLocation = document.getElementById('eventPlace')
const description = document.getElementById('description');
const eventTimeStart = document.getElementById('timeStart')
const eventTimeEnd = document.getElementById('timeEnd')

// clicked = currentDateOfEvent


function openModal(date) {
    const curDay = new Date();
    if (((date.split('/')[1] >= curDay.getDate()) && (nav === 0)) || (nav > 0)) {
        const d = JSON.parse(localStorage.getItem('events'));
        clicked = date;
        let ev = '';

        const eventForDay = events.find(e => e.startDate === clicked);

        if (eventForDay) {
            for (let j = 0; j < d.length; j++) {
                if (d[j].startDate === date) {
                    ev += 'title: ' + d[j].name + ' location: ' + d[j].location + '\n';
                }
            }
        } else {
            ev = 'There are no events yet';
        }
        document.getElementById('eventText').innerText = ev;
        eventModal.style.display = 'block';
        backDrop.style.display = 'block';
    }
}

function openNewModal() {
    newEventModal.style.display = 'block';
    backDrop.style.display = 'block';
}

function load() {
    // events = []
    // localStorage.clear()
    const dt = new Date();

    if (nav !== 0) {
        dt.setMonth(new Date().getMonth() + nav);
    }

    const day = dt.getDate();
    const month = dt.getMonth();
    const year = dt.getFullYear();

    const firstDayOfMonth = new Date(year, month, 1);
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const dateString = firstDayOfMonth.toLocaleDateString('en-us', {
        weekday: 'long',
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
    });
    const paddingDays = bigCalendarWeekdays.indexOf(dateString.split(', ')[0]);

    document.getElementById('monthDisplay').innerText =
        `${dt.toLocaleDateString('en-us', {month: 'long'})} ${year}`;

    bigCalendarBody.innerHTML = '';

    for (let i = 1; i <= paddingDays + daysInMonth; i++) {
        const daySquare = document.createElement('div');
        daySquare.classList.add('day');

        const dayString = `${month + 1}/${i - paddingDays}/${year}`;
        const stringDate = `${month + 1}/${i - paddingDays}/${year}`;

        if (i > paddingDays) {
            daySquare.innerText = (i - paddingDays) + "";

            const eventForDay = events.find(e => e.startDate === dayString);

            if (i - paddingDays === day && nav === 0) {
                daySquare.id = 'currentDay';
            }

            if ((i - paddingDays < day && nav === 0) || (nav < 0)) {
                daySquare.id = 'lastDay';
            }

            if (eventForDay) {
                const d = JSON.parse(localStorage.getItem('events'));
                let k = 0;
                const eventDiv = document.createElement('div');
                eventDiv.classList.add('event');
                eventDiv.innerText = eventForDay.name;
                daySquare.appendChild(eventDiv);
                for (let j = 0; j < d.length; j++) {
                    if (d[j].startDate === stringDate) {
                        k++;
                    }
                }
                if (k > 1) {
                    const eventDiv1 = document.createElement('div');
                    eventDiv1.classList.add('event');
                    eventDiv1.innerText = 'Show more';
                    daySquare.appendChild(eventDiv1);
                }
            }

            if (i - paddingDays === day && nav === 0) {
                daySquare.id = 'currentDay';
            }

            daySquare.addEventListener('click', () => openModal(dayString));
        } else {
            daySquare.classList.add('padding');
        }

        bigCalendarBody.appendChild(daySquare);
    }
}

function closeModal() {
    eventTitleInput.classList.remove('error');
    newEventModal.style.display = 'none';
    eventModal.style.display = 'none';
    backDrop.style.display = 'none';
    eventTitleInput.value = '';
    eventLocation.value = '';
    description.value = '';
    clicked = null;
    load();
}

function saveEvent() {
    if (eventTitleInput.value) {
        eventTitleInput.classList.remove('error');

        let eventJson = {
            name: eventTitleInput.value,
            description: description.value,
            location: eventLocation.value,
            startDate: clicked,
            startTime: eventTimeStart.value,
            endDate: clicked,
            endTime: eventTimeEnd.value,
            groups: getCheckedCheckBoxes('groups[]')
        }

        events.push(eventJson);

        let xhr = new XMLHttpRequest();
        xhr.open("POST", "save_event", true)

        xhr.setRequestHeader("Accept", "application/json")
        xhr.setRequestHeader("Content-Type", "application/json")

        xhr.onreadystatechange = function () {
            if (this.readyState !== 4) return

            if (this.status === 200) {
                localStorage.setItem('events', JSON.stringify(events));
                closeModal();
                alert("OK!")
            } else {
                eventTitleInput.classList.add('error');
                alert("Something went wrong!")
            }
        };

        xhr.send(JSON.stringify(eventJson))
    } else {
        eventTitleInput.classList.add('error');
    }
}

function initButtons() {
    document.getElementById('nextButton').addEventListener('click', () => {
        nav++;
        load();
    });
    document.getElementById('backButton').addEventListener('click', () => {
        nav--;
        load();
    });
    document.getElementById('saveButton').addEventListener('click', saveEvent);
    document.getElementById('cancelButton').addEventListener('click', closeModal);
    document.getElementById('closeButton').addEventListener('click', closeModal);
    document.getElementById('createButton').addEventListener('click', () => {
        eventModal.style.display = 'none';
        openNewModal();
    });
}

function getCheckedCheckBoxes(name) {
    let checkboxes = document.getElementsByName(name)
    let checkboxesChecked = []
    for (let index = checkboxes.length - 1; index >= 0; index--) {
        if (checkboxes[index].checked) {
            checkboxesChecked.push(checkboxes[index].value)
        }

    }
    return checkboxesChecked
}

(function ($) {
    function setChecked(target) {
        const checked = $(target).find("input[type='checkbox']:checked").length;
        if (checked) {
            $(target).find('select option:first').html('Selected: ' + checked);
        } else {
            $(target).find('select option:first').html('Choose from list');
        }
    }

    $.fn.checkselect = function () {
        this.wrapInner('<div class="checkselect-popup"></div>');
        this.prepend(
            '<div class="checkselect-control">' +
            '<select class="form-control"><option></option></select>' +
            '<div class="checkselect-over"></div>' +
            '</div>'
        );

        this.each(function () {
            setChecked(this);
        });
        this.find('input[type="checkbox"]').click(function () {
            setChecked($(this).parents('.checkselect'));
        });

        this.parent().find('.checkselect-control').on('click', function () {
            const $popup = $(this).next();
            $('.checkselect-popup').not($popup).css('display', 'none');
            if ($popup.is(':hidden')) {
                $popup.css('display', 'block');
                $(this).find('select').focus();
            } else {
                $popup.css('display', 'none');
            }
        });

        $('html, body').on('click', function (e) {
            if ($(e.target).closest('.checkselect').length === 0) {
                $('.checkselect-popup').css('display', 'none');
            }
        });
    };
})(jQuery);

$('.checkselect').checkselect();

initButtons();
load();