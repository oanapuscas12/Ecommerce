document.addEventListener("DOMContentLoaded", function() {
    const errorMessageElement = document.getElementById('error-message');
    const successMessageElement = document.getElementById('success-message');

    if (errorMessageElement) {
        setTimeout(() => {
            errorMessageElement.style.display = 'none';
        }, 3000);
    }

    if (successMessageElement) {
        setTimeout(() => {
            successMessageElement.style.display = 'none';
        }, 3000);
    }
});