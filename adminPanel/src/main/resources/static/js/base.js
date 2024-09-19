function toggleMenu() {
    const navList = document.querySelector('.nav-list');
    navList.classList.toggle('active');
}

document.addEventListener("DOMContentLoaded", function () {
    const notificationIcon = document.getElementById("notificationIcon");
    const notificationBadge = document.getElementById("notificationBadge");
    const notificationPanel = document.getElementById("notificationPanel");
    const notificationList = document.getElementById("notificationList");
    const noNotifications = document.getElementById("noNotifications");

    // Check if elements exist
    if (!notificationIcon || !notificationBadge || !notificationPanel || !notificationList || !noNotifications) {
        console.error('One or more notification elements are missing.');
        return; // Exit the function to avoid further errors
    }

    function fetchNotifications() {
        fetch('/api/notifications/unread')
            .then(response => response.json())
            .then(data => {
                notificationBadge.textContent = data.length;
                if (data.length > 0) {
                    notificationBadge.style.display = 'block';
                    notificationList.innerHTML = '';
                    data.forEach(notification => {
                        const li = document.createElement('li');
                        li.textContent = notification.message;
                        notificationList.appendChild(li);
                    });
                    noNotifications.style.display = 'none';
                } else {
                    notificationBadge.style.display = 'none';
                    noNotifications.style.display = 'block';
                }
                notificationPanel.style.display = 'block'; // Always show the panel
            })
            .catch(error => {
                console.error('Error fetching notifications:', error);
            });
    }

    notificationIcon.addEventListener('click', function () {
        if (notificationPanel.style.display === 'block' || notificationPanel.style.display === '') {
            notificationPanel.style.display = 'none';
        } else {
            notificationPanel.style.display = 'block';
            fetchNotifications(); // Ensure notifications are fetched when the panel is opened
        }
    });

    // Fetch notifications on page load
    fetchNotifications();

    // Optionally, set up an interval to refresh notifications periodically
    setInterval(fetchNotifications, 60000); // Refresh every 60 seconds
});