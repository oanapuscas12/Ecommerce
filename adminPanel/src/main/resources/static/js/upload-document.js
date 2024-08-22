    document.addEventListener("DOMContentLoaded", function() {
        const errorMessageElement = document.getElementById('error-message');
        const successMessageElement = document.getElementById('success-message');
        const userId = document.getElementById('userId');
        const fileInput = document.getElementById('fileInput');
        const submitButton = document.getElementById('submitButton');
        const isAdmin = document.querySelector('form').hasAttribute('data-is-admin'); // Determine if admin

        function hideMessages() {
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
        }

        function checkFormValidity() {
            let isValid;

            if (isAdmin) {
                isValid = userId.value !== "" && fileInput.files.length > 0;
            } else {
                isValid = fileInput.files.length > 0;
            }

            const hasError = errorMessageElement && errorMessageElement.style.display !== 'none';
            submitButton.disabled = !isValid || hasError;
            submitButton.style.pointerEvents = submitButton.disabled ? 'none' : 'auto';
        }

        checkFormValidity();

        if (userId) {
            userId.addEventListener('change', checkFormValidity);
        }
        fileInput.addEventListener('change', checkFormValidity);

        hideMessages();
    });