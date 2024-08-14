document.addEventListener('DOMContentLoaded', function() {
    function updateDateTime() {
        const now = new Date();
        const day = String(now.getDate()).padStart(2, '0');
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const year = now.getFullYear();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const seconds = String(now.getSeconds()).padStart(2, '0');

        const dateString = `${day}-${month}-${year}`;
        const timeString = `${hours}:${minutes}:${seconds}`;

        const dateElement = document.querySelector('.date');
        const timeElement = document.querySelector('.time');

        if (dateElement) {
            dateElement.textContent = dateString;
        }
        if (timeElement) {
            timeElement.textContent = timeString;
        }
    }

    setInterval(updateDateTime, 1000);
    updateDateTime();
});
