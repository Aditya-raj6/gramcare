// GramCare shared client-side helpers (vanilla JS, no frameworks).

document.addEventListener('DOMContentLoaded', function () {
  // Confirmation prompts for destructive/irreversible actions.
  document.querySelectorAll('[data-confirm]').forEach(function (el) {
    el.addEventListener('submit', function (evt) {
      var message = el.getAttribute('data-confirm') || 'Are you sure?';
      if (!window.confirm(message)) {
        evt.preventDefault();
      }
    });
  });


  document.querySelectorAll('form.validated-form').forEach(function (form) {
    form.addEventListener('submit', function (evt) {
      var missing = [];
      form.querySelectorAll('[required]').forEach(function (field) {
        if (!field.value || !field.value.trim()) {
          missing.push(field.name || field.id || 'field');
          field.classList.add('input-error');
        } else {
          field.classList.remove('input-error');
        }
      });
      if (missing.length > 0) {
        evt.preventDefault();
        var banner = form.querySelector('.form-validation-banner');
        if (banner) {
          banner.textContent = 'Please fill in: ' + missing.join(', ');
          banner.style.display = 'block';
        }
      }
    });
  });

  var toggle = document.getElementById('sidebarToggle');
  var sidebar = document.querySelector('.sidebar');
  if (toggle && sidebar) {
    toggle.addEventListener('click', function () {
      sidebar.classList.toggle('open');
    });
  }
});
