/*
  Charty documentation: theme toggle, search, copy buttons, on-page contents highlighting and the
  mobile sidebar.

  Every page is a real HTML document, so navigation is the browser's own — back, forward, reload and
  opening in a new tab all behave, and none of it is reimplemented here. What this file adds is the
  behaviour a static page cannot have on its own.
*/
(function () {
    'use strict';

    var root = document.currentScript.dataset.root || '';

    /* ---------- theme ---------- */

    var toggle = document.querySelector('.theme-toggle');

    function paintToggle() {
        if (!toggle) {
            return;
        }
        var dark = document.documentElement.dataset.theme === 'dark';
        toggle.textContent = dark ? '☀' : '☾';
        toggle.setAttribute('aria-label', dark ? 'Switch to light theme' : 'Switch to dark theme');
    }

    if (toggle) {
        toggle.addEventListener('click', function () {
            var dark = document.documentElement.dataset.theme === 'dark';
            document.documentElement.dataset.theme = dark ? 'light' : 'dark';
            try {
                localStorage.setItem('charty-theme', dark ? 'light' : 'dark');
            } catch (e) {
                /* Private browsing refuses writes; the theme still applies for this page. */
            }
            paintToggle();
        });
        paintToggle();
    }

    /* ---------- copy buttons ---------- */

    document.querySelectorAll('.code-block .copy').forEach(function (button) {
        button.addEventListener('click', function () {
            var code = button.parentElement.querySelector('code');
            if (!code) {
                return;
            }
            navigator.clipboard.writeText(code.innerText).then(function () {
                button.textContent = 'Copied';
                setTimeout(function () { button.textContent = 'Copy'; }, 1400);
            }, function () {
                button.textContent = 'Press ⌘C';
                setTimeout(function () { button.textContent = 'Copy'; }, 1800);
            });
        });
    });

    /* ---------- mobile sidebar ---------- */

    var menu = document.querySelector('.menu-toggle');
    var sidebar = document.getElementById('sidebar');

    if (menu && sidebar) {
        menu.addEventListener('click', function () {
            var open = sidebar.classList.toggle('is-open');
            menu.setAttribute('aria-expanded', String(open));
        });
    }

    /* ---------- on-page contents ---------- */

    var tocLinks = Array.prototype.slice.call(document.querySelectorAll('.toc a'));

    if (tocLinks.length && 'IntersectionObserver' in window) {
        var headings = tocLinks
            .map(function (link) { return document.getElementById(link.hash.slice(1)); })
            .filter(Boolean);

        // The top margin keeps a heading from counting as "current" while it is still hidden behind
        // the sticky bar; the bottom margin stops the last section winning the whole way down.
        var observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (!entry.isIntersecting) {
                    return;
                }
                tocLinks.forEach(function (link) {
                    link.classList.toggle('is-active', link.hash === '#' + entry.target.id);
                });
            });
        }, { rootMargin: '-80px 0px -70% 0px' });

        headings.forEach(function (heading) { observer.observe(heading); });
    }

    /* ---------- reveal on scroll ---------- */

    /*
      Sections below the fold rise into place as they are reached.

      Driven by a scroll sweep rather than an IntersectionObserver, because the observer gets this
      wrong in a way that loses content: jump straight to the bottom of the page — an anchor link, a
      restored scroll position, End — and the sections you flew past never intersected anything, so
      they stay hidden forever. A sweep asks the only question that matters, "is this above the fold
      now", and answers it correctly however the reader got there.

      Elements are marked hidden here rather than in the stylesheet, and only if they start below the
      fold, so a reader who never scrolls still sees a complete page and a broken script degrades to
      no animation instead of no content.
    */
    var revealables = Array.prototype.slice.call(document.querySelectorAll('.reveal'));

    if (revealables.length && !matchMedia('(prefers-reduced-motion: reduce)').matches) {
        var pending = revealables.filter(function (element) {
            return element.getBoundingClientRect().top > window.innerHeight * 0.9;
        });
        pending.forEach(function (element) { element.classList.add('is-pending'); });

        var ticking = false;

        function sweep() {
            ticking = false;
            var limit = window.innerHeight * 0.9;
            pending = pending.filter(function (element) {
                if (element.getBoundingClientRect().top > limit) {
                    return true;
                }
                element.classList.remove('is-pending');
                return false;
            });
            if (!pending.length) {
                window.removeEventListener('scroll', onScroll);
                window.removeEventListener('resize', onScroll);
            }
        }

        function onScroll() {
            if (!ticking) {
                ticking = true;
                requestAnimationFrame(sweep);
            }
        }

        if (pending.length) {
            window.addEventListener('scroll', onScroll, { passive: true });
            window.addEventListener('resize', onScroll);
        }
    }

    /* ---------- search ---------- */

    var input = document.getElementById('search-input');
    var results = document.getElementById('search-results');
    var index = null;
    var loading = false;
    var cursor = -1;

    function loadIndex() {
        if (index || loading) {
            return Promise.resolve();
        }
        loading = true;
        return fetch(root + 'assets/search-index.json')
            .then(function (response) { return response.json(); })
            .then(function (data) { index = data; })
            .catch(function () { index = []; });
    }

    // Scores a page against the query. Every term must appear somewhere, so a two-word query
    // narrows rather than widens; a title match outranks a body match, because someone typing
    // "streaming" wants the streaming page and not the twelve pages that mention it.
    function score(entry, terms) {
        var title = entry.t.toLowerCase();
        var section = entry.s.toLowerCase();
        var body = entry.b.toLowerCase();
        var total = 0;

        for (var i = 0; i < terms.length; i++) {
            var term = terms[i];
            if (title.indexOf(term) === 0) {
                total += 100;
            } else if (title.indexOf(term) !== -1) {
                total += 60;
            } else if (section.indexOf(term) !== -1) {
                total += 20;
            } else if (body.indexOf(term) !== -1) {
                total += 6;
            } else {
                return 0;
            }
        }
        return total;
    }

    function render(matches) {
        if (!matches.length) {
            results.innerHTML = '<div class="r-empty">No matches.</div>';
            results.hidden = false;
            return;
        }
        results.innerHTML = matches.map(function (entry, i) {
            var active = i === cursor ? ' class="is-active"' : '';
            return '<a href="' + root + entry.u + '"' + active + '>' +
                '<span class="r-section">' + entry.s + '</span>' + entry.t + '</a>';
        }).join('');
        results.hidden = false;
    }

    function search() {
        var query = input.value.trim().toLowerCase();
        if (query.length < 2) {
            results.hidden = true;
            return;
        }
        var terms = query.split(/\s+/);
        var matches = (index || [])
            .map(function (entry) { return { entry: entry, score: score(entry, terms) }; })
            .filter(function (row) { return row.score > 0; })
            .sort(function (a, b) { return b.score - a.score; })
            .slice(0, 8)
            .map(function (row) { return row.entry; });
        cursor = -1;
        render(matches);
    }

    if (input && results) {
        input.addEventListener('focus', loadIndex);
        input.addEventListener('input', function () {
            loadIndex().then(search);
        });

        input.addEventListener('keydown', function (event) {
            var links = results.querySelectorAll('a');
            if (event.key === 'Escape') {
                results.hidden = true;
                input.blur();
                return;
            }
            if (!links.length || results.hidden) {
                return;
            }
            if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
                event.preventDefault();
                cursor += event.key === 'ArrowDown' ? 1 : -1;
                if (cursor < 0) {
                    cursor = links.length - 1;
                }
                if (cursor >= links.length) {
                    cursor = 0;
                }
                links.forEach(function (link, i) {
                    link.classList.toggle('is-active', i === cursor);
                });
                links[cursor].scrollIntoView({ block: 'nearest' });
            }
            if (event.key === 'Enter' && cursor >= 0) {
                event.preventDefault();
                window.location.href = links[cursor].href;
            }
        });

        document.addEventListener('click', function (event) {
            if (!event.target.closest('.search')) {
                results.hidden = true;
            }
        });

        // "/" focuses search, the shortcut every docs site has and readers reach for without
        // thinking. Ignored while typing, so it can still be typed into a field.
        document.addEventListener('keydown', function (event) {
            var typing = /^(INPUT|TEXTAREA|SELECT)$/.test(document.activeElement.tagName);
            if (event.key === '/' && !typing) {
                event.preventDefault();
                input.focus();
            }
        });
    }
})();
