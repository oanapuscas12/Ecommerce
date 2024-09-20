document.addEventListener('DOMContentLoaded', function () {
    const merchantSelect = document.getElementById('merchantId');
    if (merchantSelect) {
        merchantSelect.addEventListener('change', function () {
            this.form.submit();
        });
    }

    $('#previewModal').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget);
        var documentId = button.data('document-id');
        var modal = $(this);
        var previewUrl = '/documents/preview/' + documentId;

        fetch(previewUrl, { method: 'HEAD' })
            .then(response => {
                var contentType = response.headers.get('Content-Type');
                var previewFrame = modal.find('#previewFrame');
                var downloadLink = modal.find('#downloadLink');

                if (contentType && (contentType.startsWith('image/') || contentType === 'application/pdf' || contentType.startsWith('text/'))) {
                    previewFrame.attr('src', previewUrl).show();
                    downloadLink.hide();
                } else {
                    previewFrame.hide();
                    downloadLink.attr('href', previewUrl).show();
                }
            })
            .catch(error => {
                console.error('Error fetching content type:', error);
                modal.find('#previewFrame').hide();
                modal.find('#downloadLink').attr('href', previewUrl).show();
            });
    });
});
