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

  function selectedNameFromTables(idStr){
    // spróbuj najpierw PG, potem Neo – bierzemy data-first/data-last
    let tr = q(`#pg-table tbody tr[data-id="${idStr}"]`);
    if (!tr) tr = q(`#neo-table tbody tr[data-id="${idStr}"]`);
    if (!tr) return null;
    const first = tr.getAttribute('data-first') || '';
    const last  = tr.getAttribute('data-last') || '';
    const name  = `${first} ${last}`.trim();
    return name || null;
  }

  function markSelectedRows(employeeId) {
    const idStr = String(employeeId);
    ['#pg-table', '#neo-table'].forEach(sel => {
      const table = q(sel);
      if (!table) return;
      let target = null;
      qa('tbody tr', table).forEach(tr => {
        const trId = (tr.getAttribute('data-id') || '').trim();
        if (trId === idStr) { tr.classList.add('selected'); target = tr; }
        else tr.classList.remove('selected');
      });
      if (target) {
        const wrap = table.closest('.table-wrap');
        target.scrollIntoView({block: 'center'});
        if (wrap) wrap.scrollLeft = Math.max(0, target.offsetLeft - 40);
      }
    });
    const hidden = q('#selected-id'); if (hidden) hidden.value = idStr;
    const name = selectedNameFromTables(idStr);
    const hiddenName = q('#selected-name'); if (hiddenName) hiddenName.value = name || '';
  }

  // === API helper: zawsze zwraca obiekt {pg, neo} (może być null/null) ===
  async function fetchEmployee(id) {
    try {
      const res = await fetch(`/api/employee/${id}`, {headers: {'Accept': 'application/json'}});
      if (!res.ok) return { pg: null, neo: null };
      const data = await res.json();
      return (data && typeof data === 'object') ? data : { pg: null, neo: null };
    } catch {
      return { pg: null, neo: null };
    }
  }

  function fillEditModal(data) {
    const pg  = data.pg || {};
    const neo = data.neo || {};
    q('#edit-employeeId') && (q('#edit-employeeId').value  = pg.employeeId || neo.employeeId || q('#selected-id').value || '');
    q('#edit-firstName')  && (q('#edit-firstName').value   = pg.firstName || neo.firstName || '');
    q('#edit-lastName')   && (q('#edit-lastName').value    = pg.lastName  || neo.lastName  || '');
    q('#edit-email')      && (q('#edit-email').value       = pg.email     || neo.email     || '');
    q('#edit-phone')      && (q('#edit-phone').value       = pg.phone     || neo.phone     || '');
    q('#edit-hireDate')   && (q('#edit-hireDate').value    = (pg.hireDate || neo.hireDate || '') + '');
    if (q('#edit-departmentId') && pg.departmentId) q('#edit-departmentId').value = pg.departmentId;

    q('#edit-title')          && (q('#edit-title').value          = neo.title || '');
    q('#edit-minSalary')      && (q('#edit-minSalary').value      = neo.minSalary || '');
    q('#edit-maxSalary')      && (q('#edit-maxSalary').value      = neo.maxSalary || '');
    q('#edit-departmentName') && (q('#edit-departmentName').value = neo.departmentName || pg.departmentName || '');
    q('#edit-location')       && (q('#edit-location').value       = neo.location || '');
    q('#edit-amount')         && (q('#edit-amount').value         = neo.amount || '');
    q('#edit-fromDate')       && (q('#edit-fromDate').value       = (neo.fromDate || '') + '');
  }

  function validateMinMax(form) {
    const min = form.querySelector('input[name="minSalary"]');
    const max = form.querySelector('input[name="maxSalary"]');
    const vmin = min && min.value !== '' ? Number(min.value) : null;
    const vmax = max && max.value !== '' ? Number(max.value) : null;
    if (vmin != null && !Number.isNaN(vmin) && vmax != null && !Number.isNaN(vmax) && vmin >= vmax) {
      max.setCustomValidity("Min salary musi być mniejsze niż Max salary.");
      max.reportValidity();
      return false;
    }
    if (max) max.setCustomValidity("");
    return true;
  }
  function validatePatterns(form) {
    let ok = true;
    qa('input[pattern]', form).forEach(i => { if (!i.checkValidity()) { i.reportValidity(); ok = false; }});
    return ok;
  }

  document.addEventListener('DOMContentLoaded', () => {
    // Klik wiersza w obu tabelach → cross-highlight + zapamiętanie nazwy
    ['#pg-table', '#neo-table'].forEach(sel => {
      qa(sel + ' tbody tr').forEach(tr => {
        tr.addEventListener('click', () => {
          const id = tr.getAttribute('data-id');
          if (!id) return;
          markSelectedRows(id);
        });
      });
    });

    // Modale (Add/Edit/Delete)
    qa('[data-modal]').forEach(btn => {
      btn.addEventListener('click', async (e) => {
        e.preventDefault();
        const name = btn.dataset.modal;
        const id = q('#selected-id').value;

        if ((name === 'edit' || name === 'delete') && !id) {
          alert('Najpierw kliknij wiersz, żeby wybrać employee ID.');
          return;
        }

        if (name === 'edit') {
          const data = await fetchEmployee(id);
          fillEditModal(data);
        } else if (name === 'delete') {
          q('#delete-employeeId').value = id;
          // pobierz nazwę z ukrytego inputu lub z tabeli
          let full = q('#selected-name').value || selectedNameFromTables(String(id)) || `ID ${id}`;
          q('#delete-fullname').textContent = full;
        }
        openModal(name);
      });
    });

    // Zamykacze + klik poza kartą
    qa('dialog .modal__close, dialog [data-dismiss]').forEach(el => {
      el.addEventListener('click', (ev) => { ev.preventDefault(); closeModal(el.closest('dialog')); });
    });
    qa('dialog').forEach(dlg => {
      dlg.addEventListener('click', (ev) => {
        const card = q('.modal__card', dlg); if (!card) return;
        const r = card.getBoundingClientRect();
        const inside = ev.clientX >= r.left && ev.clientX <= r.right && ev.clientY >= r.top && ev.clientY <= r.bottom;
        if (!inside) closeModal(dlg);
      });
      dlg.addEventListener('cancel', (ev) => { ev.preventDefault(); closeModal(dlg); });
    });

    // Walidacje submitów
    const addForm  = q('#modal-add form');
    const editForm = q('#modal-edit form');
    if (addForm)  addForm.addEventListener('submit',  (e)=>{ if(!validatePatterns(addForm) || !validateMinMax(addForm)) e.preventDefault();});
    if (editForm) editForm.addEventListener('submit', (e)=>{ if(!validatePatterns(editForm)|| !validateMinMax(editForm)) e.preventDefault();});
  });
})();
