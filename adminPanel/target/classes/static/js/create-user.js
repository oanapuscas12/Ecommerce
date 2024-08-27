$(document).ready(function() {
    function toggleSubmitButton() {
        var isUsernameError = $('#username-error').text() !== '';
        var isEmailError = $('#email-error').text() !== '';
        var isUsernameFilled = $('#username').val().trim() !== '';
        var isEmailFilled = $('#email').val().trim() !== '';
        var isPasswordFilled = $('#password').val().trim() !== '';

        if (!isUsernameError && !isEmailError && isUsernameFilled && isEmailFilled && isPasswordFilled) {
            $('.submit-btn').prop('disabled', false);
        } else {
            $('.submit-btn').prop('disabled', true);
        }
    }

    function validateUsername() {
        var username = $('#username').val();
        if (username) {
            $.ajax({
                url: '/user/users/validateUsername',
                type: 'GET',
                data: { username: username },
                success: function(data) {
                    if (data) {
                        $('#username-error').text('Username already exists.');
                    } else {
                        $('#username-error').text('');
                    }
                    toggleSubmitButton();
                },
                error: function() {
                    $('#username-error').text('An error occurred while checking the username.');
                    toggleSubmitButton();
                }
            });
        } else {
            $('#username-error').text('');
            toggleSubmitButton();
        }
    }

    function validateEmail() {
        var email = $('#email').val();
        if (email) {
            $.ajax({
                url: '/user/users/validateEmail',
                type: 'GET',
                data: { email: email },
                success: function(data) {
                    if (data) {
                        $('#email-error').text('Email already exists.');
                    } else {
                        $('#email-error').text('');
                    }
                    toggleSubmitButton();
                },
                error: function() {
                    $('#email-error').text('An error occurred while checking the email.');
                    toggleSubmitButton();
                }
            });
        } else {
            $('#email-error').text('');
            toggleSubmitButton();
        }
    }

    $('#username').on('focus input', function() {
        validateUsername();
    });

    $('#email').on('focus input', function() {
        validateEmail();
    });

    $('#password').on('focus input', function() {
        toggleSubmitButton();
    });

    validateUsername();
    validateEmail();
    toggleSubmitButton();
});