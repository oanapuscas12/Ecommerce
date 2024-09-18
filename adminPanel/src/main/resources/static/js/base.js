function toggleMenu() {
    const navList = document.querySelector('.nav-list');
    navList.classList.toggle('active');
}

document.addEventListener("DOMContentLoaded", function () {
    const notificationIcon = document.getElementById("notificationIcon");
    const notificationBadge = document.getElementById("notificationBadge");

    function fetchNotifications() {
        fetch('/api/notifications/unread') // Endpoint to fetch unread notifications
            .then(response => response.json())
            .then(data => {
                notificationBadge.textContent = data.length;
                if (data.length > 0) {
                    notificationBadge.style.display = 'block';
                } else {
                    notificationBadge.style.display = 'none';
                }
            });
    }

    notificationIcon.addEventListener('click', function () {
        // Toggle visibility of notification panel
        // You can use a modal or dropdown here
    });

    fetchNotifications();
});