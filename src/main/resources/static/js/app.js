(() => {
    const root = document.documentElement;
    const saved = localStorage.getItem('ds-theme');
    if (saved) root.dataset.theme = saved;

    const toggle = document.getElementById('themeToggle');
    if (toggle) {
        toggle.addEventListener('click', () => {
            const next = root.dataset.theme === 'dark' ? 'light' : 'dark';
            root.dataset.theme = next;
            localStorage.setItem('ds-theme', next);
        });
    }

    document.querySelectorAll('.menu-trigger').forEach(button => {
        button.addEventListener('click', event => {
            event.stopPropagation();
            const current = button.closest('.menu-wrap');
            document.querySelectorAll('.menu-wrap.open').forEach(menu => {
                if (menu !== current) menu.classList.remove('open');
            });
            current.classList.toggle('open');
        });
    });
    document.addEventListener('click', () => {
        document.querySelectorAll('.menu-wrap.open').forEach(menu => menu.classList.remove('open'));
    });

    const range = document.getElementById('progressRange');
    const output = document.getElementById('progressOutput');
    if (range && output) {
        range.addEventListener('input', () => output.textContent = `${range.value}%`);
    }

    const toast = document.querySelector('.toast');
    if (toast) {
        setTimeout(() => {
            toast.style.transition = 'opacity .3s ease';
            toast.style.opacity = '0';
            setTimeout(() => toast.remove(), 350);
        }, 3200);
    }
})();
