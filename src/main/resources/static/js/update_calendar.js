let nav = 0;
let clicked = null;
let events = localStorage.getItem('events') ? JSON.parse(localStorage.getItem('events')) : [];

const bigCalendarBody = document.getElementById('bigCalendarBody');
const newEventModal = document.getElementById('newEventModal');
const eventModal = document.getElementById('eventModal');
const backDrop = document.getElementById('modalBackDrop');
const eventTitleInput = document.getElementById('eventTitleInput');
const bigCalendarWeekdays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
const eventPlace = document.getElementById('eventPlace')
const description = document.getElementById('description');

function openModal(date) {
    const curDay = new Date();
    if (((date.split('/')[1] >= curDay.getDate()) && (nav === 0)) || (nav > 0)) {
        const d = JSON.parse(localStorage.getItem('events'));
        clicked = date;
        let ev = '';

        const eventForDay = events.find(e => e.date === clicked);

        if (eventForDay) {
            for (let j = 0; j < d.length; j++) {
                if (d[j].date === date) {
                    ev += 'title: ' + d[j].title + ' location: ' + d[j].place + '\n';
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

function load(isClear) {
    if (isClear) {
        localStorage.clear()
        events = []
    }

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

            const eventForDay = events.find(e => e.date === dayString);

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
                eventDiv.innerText = eventForDay.title;
                daySquare.appendChild(eventDiv);
                for (let j = 0; j < d.length; j++) {
                    if (d[j].date === stringDate) {
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
    eventPlace.value = '';
    description.value = '';
    clicked = null;
    load(false);
}

function saveEvent() {
    if (eventTitleInput.value) {
        eventTitleInput.classList.remove('error');

        events.push({
            date: clicked,
            title: eventTitleInput.value,
            place: eventPlace.value,
        });

        localStorage.setItem('events', JSON.stringify(events));
        closeModal();
    } else {
        eventTitleInput.classList.add('error');
    }
}

function initButtons() {
    document.getElementById('nextButton').addEventListener('click', () => {
        nav++;
        load(false);
    });
    document.getElementById('backButton').addEventListener('click', () => {
        nav--;
        load(false);
    });
    document.getElementById('saveButton').addEventListener('click', saveEvent);
    document.getElementById('cancelButton').addEventListener('click', closeModal);
    document.getElementById('closeButton').addEventListener('click', closeModal);
    document.getElementById('createButton').addEventListener('click', () => {
        eventModal.style.display = 'none';
        openNewModal();
    });
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
            $popup = $(this).next();
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
load(true);