// Confirm before destructive actions
document.addEventListener('DOMContentLoaded', () => {

    // Any form with data-confirm attribute shows a confirmation dialog
    document.querySelectorAll('form[data-confirm]').forEach(form => {
        form.addEventListener('submit', e => {
            const msg = form.getAttribute('data-confirm');
            if (!confirm(msg)) {
                e.preventDefault();
            }
        });
    });

    // Auto-dismiss alerts after 5 seconds
    document.querySelectorAll('.alert[data-auto-dismiss]').forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity .5s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // Highlight active nav link based on current path
    const path = window.location.pathname;
    document.querySelectorAll('.nav-links a').forEach(link => {
        if (path.startsWith(link.getAttribute('href')) && link.getAttribute('href') !== '/') {
            link.classList.add('active');
        } else if (path === '/' && link.getAttribute('href') === '/') {
            link.classList.add('active');
        }
    });
});
