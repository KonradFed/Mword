// Modale + walidacja (patterny + min<max)
(function () {
  function q(sel, root)  { return (root || document).querySelector(sel); }
  function qa(sel, root) { return Array.from((root || document).querySelectorAll(sel)); }

  function openModal(name) {
    const dlg = q('#modal-' + name);
    if (!dlg) return;
    if (dlg.showModal) dlg.showModal(); else dlg.setAttribute('open','');
  }
  function closeModal(dlg) {
    if (!dlg) return;
    if (dlg.close) dlg.close(); else dlg.removeAttribute('open');
  }

  function validateMinMax(form) {
    const min = form.querySelector('input[name="minSalary"]');
    const max = form.querySelector('input[name="maxSalary"]');
    if (!min || !max) return true;
    const vmin = min.value ? Number(min.value) : null;
    const vmax = max.value ? Number(max.value) : null;
    if (vmin != null && !Number.isNaN(vmin) && vmax != null && !Number.isNaN(vmax)) {
      if (vmin >= vmax) {
        max.setCustomValidity("Min salary musi być mniejsze niż Max salary.");
        max.reportValidity();
        return false;
      }
    }
    max.setCustomValidity("");
    return true;
  }

  function validatePatterns(form) {
    let ok = true;
    qa('input[pattern]', form).forEach(inp => {
      if (!inp.checkValidity()) {
        inp.reportValidity();
        ok = false;
      }
    });
    return ok;
  }

  document.addEventListener('DOMContentLoaded', () => {
    // bind buttons
    qa('[data-modal]').forEach(btn => {
      btn.addEventListener('click', e => { e.preventDefault(); openModal(btn.dataset.modal); });
    });

    // close actions
    qa('dialog .modal__close, dialog [data-dismiss]').forEach(el => {
      el.addEventListener('click', e => { e.preventDefault(); closeModal(el.closest('dialog')); });
    });

    // backdrop click
    qa('dialog').forEach(dlg => {
      dlg.addEventListener('click', (e) => {
        const card = q('.modal__card', dlg);
        if (!card) return;
        const r = card.getBoundingClientRect();
        const inside = e.clientX >= r.left && e.clientX <= r.right && e.clientY >= r.top && e.clientY <= r.bottom;
        if (!inside) closeModal(dlg);
      });
      dlg.addEventListener('cancel', e => { e.preventDefault(); closeModal(dlg); });
    });

    // form validation
    const addForm = q('#modal-add form');
    if (addForm) {
      addForm.addEventListener('submit', (e) => {
        if (!validatePatterns(addForm) || !validateMinMax(addForm)) {
          e.preventDefault();
        }
      });
    }
  });
})();
