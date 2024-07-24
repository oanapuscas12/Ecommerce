document.addEventListener('DOMContentLoaded', function() {
    const password = document.getElementById('password');
    const confirmPassword = document.getElementById('confirmPassword');
    const errorElement = document.getElementById('passwordError');

    function validatePasswords() {
        if (password.value !== confirmPassword.value) {
            errorElement.textContent = 'Passwords do not match.';
        } else {
            errorElement.textContent = '';
        }
    }

    password.addEventListener('input', validatePasswords);
    confirmPassword.addEventListener('input', validatePasswords);
});
