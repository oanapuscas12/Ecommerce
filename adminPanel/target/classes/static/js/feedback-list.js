document.addEventListener('DOMContentLoaded', function () {
    const tableCells = document.querySelectorAll('.table td');

    tableCells.forEach(cell => {
        const contentSpan = cell.querySelector('.content');
        const moreIndicator = cell.querySelector('.more-indicator');

        if (contentSpan && moreIndicator) {
            if (contentSpan.scrollWidth > contentSpan.offsetWidth || contentSpan.scrollHeight > contentSpan.offsetHeight) {
                moreIndicator.style.display = 'inline';
            }

            cell.addEventListener('click', function () {
                this.classList.toggle('expanded');

                if (this.classList.contains('expanded')) {
                    moreIndicator.style.display = 'none';
                } else {
                    if (contentSpan.scrollWidth > contentSpan.offsetWidth || contentSpan.scrollHeight > contentSpan.offsetHeight) {
                        moreIndicator.style.display = 'inline';
                    }
                }
            });
        }
    });
});
