document.addEventListener('DOMContentLoaded', function() {
    const filterUsername = document.getElementById('filterUsername');
    const filterEmail = document.getElementById('filterEmail');
    const filterLegalBusinessName = document.getElementById('filterLegalBusinessName');
    const filterIsActive = document.getElementById('filterIsActive');
    const tableBody = document.getElementById('tableBody');

    if (!filterUsername || !filterEmail || !filterIsActive || !tableBody) {
        console.error('One or more filter elements or table body not found!');
        return;
    }

    const rows = tableBody.getElementsByTagName('tr');
    let legalBusinessNameColumnIndex = -1;

    // Determine if the Legal Business Name column exists
    if (filterLegalBusinessName) {
        // Assuming Legal Business Name is the fourth column if present
        legalBusinessNameColumnIndex = 3;
    }

    function filterTable() {
        const usernameValue = filterUsername.value.toLowerCase();
        const emailValue = filterEmail.value.toLowerCase();
        const legalBusinessNameValue = filterLegalBusinessName ? filterLegalBusinessName.value.toLowerCase() : '';
        const isActiveValue = filterIsActive.value.toLowerCase();

        for (let i = 0; i < rows.length; i++) {
            const cells = rows[i].getElementsByTagName('td');
            const username = cells[0] ? cells[0].textContent.toLowerCase() : '';
            const email = cells[1] ? cells[1].textContent.toLowerCase() : '';
            const isActive = cells[2] ? cells[2].textContent.toLowerCase() : '';
            const legalBusinessName = legalBusinessNameColumnIndex !== -1 && cells[legalBusinessNameColumnIndex]
                ? cells[legalBusinessNameColumnIndex].textContent.toLowerCase()
                : '';

            const matchesUsername = username.includes(usernameValue);
            const matchesEmail = email.includes(emailValue);
            const matchesLegalBusinessName = legalBusinessNameColumnIndex === -1 || legalBusinessName.includes(legalBusinessNameValue);
            const matchesIsActive = !isActiveValue || isActive.includes(isActiveValue);

            if (matchesUsername && matchesEmail && matchesLegalBusinessName && matchesIsActive) {
                rows[i].style.display = '';
            } else {
                rows[i].style.display = 'none';
            }
        }
    }

    filterUsername.addEventListener('input', filterTable);
    filterEmail.addEventListener('input', filterTable);
    if (filterLegalBusinessName) {
        filterLegalBusinessName.addEventListener('input', filterTable);
    }
    filterIsActive.addEventListener('change', filterTable);
});