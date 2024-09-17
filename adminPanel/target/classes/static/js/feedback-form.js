document.addEventListener('input', function () {
    const suggestion = document.getElementById('suggestion').value.trim();
    const problem = document.getElementById('problem').value.trim();
    document.getElementById('submitBtn').disabled = !(suggestion && problem);
});