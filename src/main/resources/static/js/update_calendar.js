let nav = 0;                    /* A variable for tracking the month we are currently in */
let clicked = null;              /* A variable that will later contain the date where we create the event */
let events = localStorage.getItem('events') ? JSON.parse(localStorage.getItem('events')) : [];            /* With the help of a variable, we will get values from the local storage or put new data into it */

const bigCalendarBody = document.getElementById('bigCalendarBody');          /* Link to the calendar */
const newEventModal = document.getElementById('newEventModal');
const deleteEventModal = document.getElementById('deleteEventModal');
const backDrop = document.getElementById('modalBackDrop');
const eventTitleInput = document.getElementById('eventTitleInput');
const bigCalendarWeekdays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];   /* Variable for storing days of the week */

function openModal(date) {
    clicked = date;
    const eventForDay = events.find(e => e.date === clicked);
    newEventModal.style.display = 'block';
    backDrop.style.display = 'block';
}

function load() {                     /* Function for displaying the calendar */
  const dt = new Date();

  if (nav !== 0) {
    dt.setMonth(new Date().getMonth() + nav);
  }

  const day = dt.getDate();
  const month = dt.getMonth();
  const year = dt.getFullYear();

  const firstDayOfMonth = new Date(year, month, 1);         /* We receive information about the first day of the current month */
  const daysInMonth = new Date(year, month + 1, 0).getDate();     /* 0 refers to the last day of the previous month, so the month is incremented by 1 */
  
  const dateString = firstDayOfMonth.toLocaleDateString('en-us', {   /* Convert the information about the first day of the month into a string */
    weekday: 'long',                                      /* Displays the day of the week */
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
  });
  const paddingDays = bigCalendarWeekdays.indexOf(dateString.split(', ')[0]);  /* Removes a comma from the string, getting an array of two elements, and takes 0 element */
  
  document.getElementById('monthDisplay').innerText = 
    `${dt.toLocaleDateString('en-us', { month: 'long' })} ${year}`;
  
  bigCalendarBody.innerHTML = '';         /* Erases the previous calendar, freeing up space for a new one when flipping through the months */

  for(let i = 1; i <= paddingDays + daysInMonth; i++) {                   /* Displaying blocks of days in the calendar */
    const daySquare = document.createElement('div');
    daySquare.classList.add('day');                           /* Adding the day element, which will represent the blocks */

    const dayString = `${month + 1}/${i - paddingDays}/${year}`;
    const stringDate = `${month+1}/${i-paddingDays}/${year}`;

    if (i > paddingDays) {
      daySquare.innerText = i - paddingDays;

      const eventForDay = events.find(e => e.date === dayString);

      if (i - paddingDays === day && nav === 0) {
        daySquare.id = 'currentDay';
      }


      
    
      
      if (eventForDay) {  
        const d = JSON.parse(localStorage.getItem('events'));
        for (let j = 0; j < d.length; j++) {
          if(d[j].date === stringDate) {
            const eventDiv = document.createElement('div');
            eventDiv.classList.add('event');
            eventDiv.innerText = d[j].title + "  " + d[j].place + "  " + d[j].group;
            daySquare.appendChild(eventDiv);
          }
        }
       
    }
     
      
      if (i - paddingDays === day && nav === 0) {
        daySquare.id = 'currentDay';
      }

      daySquare.addEventListener('click', () => openModal(dayString));
    } else {
      daySquare.classList.add('padding');
    }

    bigCalendarBody.appendChild(daySquare);           /* Adding the child element Day Square to calendar */
  }
}




function closeModal() {
    eventTitleInput.classList.remove('error');
    newEventModal.style.display = 'none';
    backDrop.style.display = 'none';
    eventTitleInput.value = '';
    eventPlace.value = '';
    clicked = null;
    load();
}

function saveEvent() {
    if (eventTitleInput.value) {
        eventTitleInput.classList.remove('error');

        events.push({
            date: clicked,
            title: eventTitleInput.value,
            place: eventPlace.value,

            group: select.value
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
        load();
    });
    document.getElementById('backButton').addEventListener('click', () => {
        nav--;
        load();
    });
    document.getElementById('saveButton').addEventListener('click', saveEvent);
    document.getElementById('cancelButton').addEventListener('click', closeModal);
}


initButtons();
load();