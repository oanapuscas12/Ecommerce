document.addEventListener('DOMContentLoaded', function () {
    const merchantSelect = document.getElementById('merchantId');
    merchantSelect.addEventListener('change', function () {
        this.form.submit();
    });
});
