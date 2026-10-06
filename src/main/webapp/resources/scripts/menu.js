(function () {
    "use strict";

    if (window.galenosMenuReady) {
        return;
    }
    window.galenosMenuReady = true;

    function setMenuOpen(button, open) {
        if (!button) {
            return;
        }
        var navigation = document.getElementById(button.getAttribute("aria-controls"));
        if (!navigation) {
            return;
        }
        var restoreFocus = !open && navigation.contains(document.activeElement);
        navigation.hidden = !open;
        navigation.closest(".layout-body").classList.toggle("is-menu-open", open);
        button.setAttribute("aria-expanded", String(open));
        if (restoreFocus) {
            button.focus();
        }
    }

    // La delegación mantiene el botón funcional tras las actualizaciones AJAX de Faces.
    document.addEventListener("click", function (event) {
        var button = event.target instanceof Element && event.target.closest("#menuToggle");
        if (button) {
            setMenuOpen(button, button.getAttribute("aria-expanded") !== "true");
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key !== "Escape") {
            return;
        }
        var button = document.getElementById("menuToggle");
        var navigation = document.getElementById("layoutNavigation");
        if (button && navigation && !navigation.hidden
                && (navigation.contains(event.target) || button.contains(event.target))) {
            event.preventDefault();
            setMenuOpen(button, false);
        }
    });

    // Cada carga o regreso a la página empieza con el menú cerrado.
    window.addEventListener("pageshow", function () {
        setMenuOpen(document.getElementById("menuToggle"), false);
    });
}());
