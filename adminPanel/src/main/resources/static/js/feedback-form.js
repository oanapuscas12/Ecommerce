document.addEventListener('DOMContentLoaded', function () {
    const maxLength = 255;

    function handleInput(event) {
        const textarea = event.target;
        const currentLength = textarea.value.length;
        const errorDiv = document.getElementById(`${textarea.id}-error`);

        if (errorDiv) {
            if (currentLength >= maxLength) {
                errorDiv.style.display = 'block';
                textarea.value = textarea.value.substring(0, maxLength);
            } else {
                errorDiv.style.display = 'none';
            }
        } else {
            console.error(`Error div not found for ${textarea.id}`);
        }

        validateSubmitButton();
    }

    function validateSubmitButton() {
        const suggestion = document.getElementById('suggestion');
        const problem = document.getElementById('problem');
        const submitBtn = document.getElementById('submitBtn');

        if (suggestion && problem && submitBtn) {
            const suggestionValue = suggestion.value.trim();
            const problemValue = problem.value.trim();
            submitBtn.disabled = !(suggestionValue && problemValue);
        } else {
            console.error('One or more form elements not found.');
        }
    }

    const suggestionTextarea = document.getElementById('suggestion');
    const problemTextarea = document.getElementById('problem');

    if (suggestionTextarea) {
        suggestionTextarea.addEventListener('input', handleInput);
    } else {
        console.error('Suggestion textarea not found.');
    }

    if (problemTextarea) {
        problemTextarea.addEventListener('input', handleInput);
    } else {
        console.error('Problem textarea not found.');
    }
});