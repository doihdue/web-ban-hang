// Toggle sidebar visibility on small screens and keep aria attributes in sync
(function(){
    function isSmallScreen() {
        return window.innerWidth <= 992;
    }

    function setAriaExpanded(button, expanded) {
        try {
            if (!button) return;
            button.setAttribute('aria-expanded', expanded ? 'true' : 'false');
        } catch(e) { /* silent */ }
    }

    // compute and set backdrop top based on navbar height (for fixed mobile navbar)
    function adjustBackdropPosition(backdrop){
        if(!backdrop) return;
        var navbar = document.querySelector('.navbar-mobile');
        if(navbar){
            var rect = navbar.getBoundingClientRect();
            var bottom = Math.ceil(rect.bottom);
            backdrop.style.top = bottom + 'px';
            backdrop.style.bottom = '0';
            backdrop.style.left = '0';
            backdrop.style.right = '0';
        } else {
            // default
            backdrop.style.top = '56px';
        }
    }

    document.addEventListener('DOMContentLoaded', function(){
        var togglers = document.querySelectorAll('[data-sidebar-toggle]');
        var sidebar = document.getElementById('adminSidebar');
        var backdrop = document.getElementById('adminSidebarBackdrop');

        if(!sidebar) return;

        function showSidebar(btn){
            sidebar.classList.add('show');
            if(backdrop){
                adjustBackdropPosition(backdrop);
                backdrop.classList.add('visible');
                backdrop.setAttribute('aria-hidden','false');
                backdrop.setAttribute('tabindex','-1');
            }
            document.body.style.overflow = 'hidden';
            if(btn) setAriaExpanded(btn, true);
        }
        function hideSidebar(){
            sidebar.classList.remove('show');
            if(backdrop){
                backdrop.classList.remove('visible');
                backdrop.setAttribute('aria-hidden','true');
            }
            document.body.style.overflow = '';
            togglers.forEach(function(b){ setAriaExpanded(b, false); });
        }

        togglers.forEach(function(btn){
            btn.addEventListener('click', function(ev){
                 ev.preventDefault();
                 if(!isSmallScreen()) return;
                 if(sidebar.classList.contains('show')){
                     hideSidebar();
                 } else {
                     showSidebar(btn);
                     if(backdrop) backdrop.focus();
                 }
             });
         });

        // Click on backdrop closes sidebar
        if(backdrop){
            backdrop.addEventListener('click', function(){ hideSidebar(); });
        }

        // Close sidebar when clicking a nav-link inside the sidebar or the mobile navbar area
        document.addEventListener('click', function(e){
            var target = e.target;
            if(isSmallScreen() && sidebar.classList.contains('show')){
                // If clicking inside sidebar nav links -> close
                if(target.closest('.sidebar .nav-link')){
                    hideSidebar();
                    return;
                }
                // If clicking logout or a real nav action inside the mobile navbar actions, do NOT close immediately
                if(target.closest('.navbar-mobile-actions') || (target.closest('.navbar-mobile') && !target.closest('#adminSidebarToggler'))){
                    return;
                }
                 if(!target.closest('.sidebar') && !target.closest('.navbar-mobile')){
                     hideSidebar();
                 }
             }
         });

        // Close sidebar on ESC
        document.addEventListener('keydown', function(e){
            if(e.key === 'Escape' || e.key === 'Esc'){
                if(sidebar.classList.contains('show')){
                    hideSidebar();
                }
            }
        });

        // Ensure sidebar is visible on large screens and aria reset; also adjust backdrop on resize
        window.addEventListener('resize', function(){
            if(!isSmallScreen()){
                hideSidebar();
            }
            if(backdrop && backdrop.classList.contains('visible')){
                adjustBackdropPosition(backdrop);
            }
        });

        // initial adjustment if needed
        if(backdrop){ adjustBackdropPosition(backdrop); }
    });
})();
