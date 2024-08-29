document.addEventListener('DOMContentLoaded', function() {
    const filterUsername = document.getElementById('filterUsername');
    const filterEmail = document.getElementById('filterEmail');
    const filterLegalBusinessName = document.getElementById('filterLegalBusinessName');
    const filterIsActive = document.getElementById('filterIsActive');
    const tableBody = document.getElementById('tableBody');

    if (!filterUsername || !filterEmail || !filterLegalBusinessName || !filterIsActive || !tableBody) {
        console.error('One or more filter elements or table body not found!');
        return;
    }

    const rows = tableBody.getElementsByTagName('tr');

    function filterTable() {
        const usernameValue = filterUsername.value.toLowerCase();
        const emailValue = filterEmail.value.toLowerCase();
        const legalBusinessNameValue = filterLegalBusinessName.value.toLowerCase();
        const isActiveValue = filterIsActive.value;

        for (let i = 0; i < rows.length; i++) {
            const usernameCell = rows[i].getElementsByTagName('td')[0];
            const emailCell = rows[i].getElementsByTagName('td')[1];
            const isActiveCell = rows[i].getElementsByTagName('td')[2];
            const legalBusinessNameCell = rows[i].getElementsByTagName('td')[4];

            const username = usernameCell ? usernameCell.textContent.toLowerCase() : '';
            const email = emailCell ? emailCell.textContent.toLowerCase() : '';
            const isActive = isActiveCell ? isActiveCell.textContent.toLowerCase() : '';
            const legalBusinessName = legalBusinessNameCell ? legalBusinessNameCell.textContent.toLowerCase() : '';

            const matchesUsername = username.includes(usernameValue);
            const matchesEmail = email.includes(emailValue);
            const matchesLegalBusinessName = legalBusinessName.includes(legalBusinessNameValue);
            const matchesIsActive = !isActiveValue || isActive.includes(isActiveValue.toLowerCase());

            if (matchesUsername && matchesEmail && matchesLegalBusinessName && matchesIsActive) {
                rows[i].style.display = '';
            } else {
                rows[i].style.display = 'none';
            }
        }
    }

    filterUsername.addEventListener('input', filterTable);
    filterEmail.addEventListener('input', filterTable);
    filterLegalBusinessName.addEventListener('input', filterTable);
    filterIsActive.addEventListener('change', filterTable);
});