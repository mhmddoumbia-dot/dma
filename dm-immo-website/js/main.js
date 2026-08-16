document.addEventListener('DOMContentLoaded', function () {
  var header = document.getElementById('siteHeader');
  var navToggle = document.getElementById('navToggle');
  var backToTop = document.getElementById('backToTop');
  var yearEl = document.getElementById('year');
  var form = document.getElementById('contactForm');
  var formSuccess = document.getElementById('formSuccess');

  if (yearEl) yearEl.textContent = new Date().getFullYear();

  // Header shrink + back-to-top visibility on scroll
  function onScroll() {
    var scrolled = window.scrollY > 40;
    header.classList.toggle('scrolled', scrolled);
    backToTop.classList.toggle('show', window.scrollY > 500);
  }
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();

  // Mobile nav toggle
  if (navToggle) {
    navToggle.addEventListener('click', function () {
      header.classList.toggle('nav-open');
    });
  }

  // Close mobile nav when a link is clicked
  document.querySelectorAll('.nav a').forEach(function (link) {
    link.addEventListener('click', function () {
      header.classList.remove('nav-open');
    });
  });

  // Back to top
  backToTop.addEventListener('click', function () {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  });

  // Contact form — client-side only: opens a prefilled email to the DM IMMO inbox
  if (form) {
    form.addEventListener('submit', function (e) {
      e.preventDefault();

      var name = document.getElementById('fname').value.trim();
      var email = document.getElementById('femail').value.trim();
      var phone = document.getElementById('fphone').value.trim();
      var subject = document.getElementById('fsubject').value;
      var message = document.getElementById('fmessage').value.trim();

      var body = [
        'Nom : ' + name,
        'Email : ' + email,
        'Téléphone : ' + (phone || 'Non renseigné'),
        '',
        message
      ].join('\n');

      var mailto = 'mailto:contact@dmimmo.ci'
        + '?subject=' + encodeURIComponent('[Site DM IMMO] ' + subject)
        + '&body=' + encodeURIComponent(body);

      window.location.href = mailto;

      formSuccess.classList.add('show');
      form.reset();

      setTimeout(function () {
        formSuccess.classList.remove('show');
      }, 8000);
    });
  }
});
