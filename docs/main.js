document.addEventListener('DOMContentLoaded', () => {
    // Elements
    const headings = document.querySelectorAll('.content h2, .content h3');
    const tocList = document.getElementById('toc-list');
    const mobileMenuToggle = document.getElementById('mobile-toggle');
    const sidebarLeft = document.getElementById('sidebar-left');
    const searchInput = document.getElementById('doc-search');
    const navLinks = document.querySelectorAll('.nav-item a');
    const headerLogo = document.getElementById('header-logo');

    // 1. Dynamic TOC Generation with Smooth Scroll Nesting
    const tocFragment = document.createDocumentFragment();
    headings.forEach(heading => {
        // Ensure ID exists
        if (!heading.id) {
            heading.id = heading.textContent.trim().toLowerCase()
                .replace(/[^\w\s-]/g, '')
                .replace(/\s+/g, '-');
        }

        // Add Anchor Symbol Link (if not already present in HTML)
        if (!heading.querySelector('.anchor-link')) {
            const anchor = document.createElement('span');
            anchor.className = 'anchor-link';
            anchor.textContent = '#';
            anchor.title = 'Copy link';
            heading.appendChild(anchor);
        }
        
        // Create TOC entry
        const li = document.createElement('li');
        const a = document.createElement('a');
        a.href = `#${heading.id}`;
        a.textContent = heading.textContent.replace(' #', '').trim();
        
        if (heading.tagName === 'H3') {
            a.style.paddingLeft = '1.2rem';
            a.style.fontSize = '0.85rem';
            a.style.opacity = '0.8';
        }
        
        li.appendChild(a);
        tocFragment.appendChild(li);
    });
    tocList.appendChild(tocFragment);

    // 2. Performance-Optimized Scroll-Spy
    const observerOptions = {
        root: null,
        rootMargin: '-10% 0px -70% 0px',
        threshold: 0
    };

    const updateActiveStates = (id) => {
        if (!id) return;

        // Update Left Sidebar
        navLinks.forEach(link => {
            const navItem = link.closest('.nav-item');
            if (link.getAttribute('href') === `#${id}`) {
                navLinks.forEach(l => l.closest('.nav-item').classList.remove('active'));
                navItem.classList.add('active');
                // Scroll sidebar to keep active item in view if needed
                navItem.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }
        });

        // Update Right TOC
        document.querySelectorAll('.toc-list a').forEach(link => {
            if (link.getAttribute('href') === `#${id}`) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    };

    const scrollObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                updateActiveStates(entry.target.id);
            }
        });
    }, observerOptions);

    // All sections and cards are observers
    document.querySelectorAll('section[id], .feature-card[id]').forEach(el => {
        scrollObserver.observe(el);
    });

    // 3. Advanced Search (Instant Result Filtering)
    searchInput.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase().trim();
        const sections = document.querySelectorAll('.content section');
        
        if (query.length === 0) {
            sections.forEach(s => s.style.display = 'block');
            return;
        }

        sections.forEach(section => {
            const text = section.innerText.toLowerCase();
            const isMatch = text.includes(query);
            section.style.display = isMatch ? 'block' : 'none';
        });
    });

    // 4. Interaction Enhancements
    // Logo scroll to top
    headerLogo.addEventListener('click', () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
        history.pushState(null, null, ' '); // Clear hash
    });

    // Keyboard shortcut for search (/)
    window.addEventListener('keydown', (e) => {
        if (e.key === '/' && document.activeElement !== searchInput) {
            e.preventDefault();
            searchInput.focus();
        }
    });

    // Mobile Menu
    if (mobileMenuToggle) {
        mobileMenuToggle.addEventListener('click', () => {
            sidebarLeft.classList.toggle('open');
            mobileMenuToggle.classList.toggle('active');
            document.body.style.overflow = sidebarLeft.classList.contains('open') ? 'hidden' : '';
        });
    }

    // Close sidebar on link click (mobile)
    sidebarLeft.addEventListener('click', (e) => {
        if (e.target.closest('a')) {
            sidebarLeft.classList.remove('open');
            mobileMenuToggle.classList.remove('active');
            document.body.style.overflow = '';
        }
    });

    // 5. Copy Link to Clipboard
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('anchor-link')) {
            const id = e.target.parentElement.id;
            const url = `${window.location.origin}${window.location.pathname}#${id}`;
            
            navigator.clipboard.writeText(url).then(() => {
                const originalText = e.target.textContent;
                e.target.textContent = '✓';
                e.target.style.color = 'var(--primary)';
                setTimeout(() => {
                    e.target.textContent = originalText;
                    e.target.style.color = '';
                }, 2000);
            });
        }
    });

    // 6. Intersection Observer for Fade-In animations
    const fadeObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
            }
        });
    }, { threshold: 0.1 });

    document.querySelectorAll('section, .feature-card, .info-box').forEach(el => {
        fadeObserver.observe(el);
    });
});
