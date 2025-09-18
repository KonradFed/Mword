// app.js
(() => {
    /**
     * ModalManager – lekki menedżer <dialog> z eventami.
     * Eventy:
     *   document.addEventListener('modal:opened', e => e.detail.id)
     *   document.addEventListener('modal:closed', e => e.detail.id)
     */
    class ModalManager {
      constructor() {
        this.modals = {};
        document.querySelectorAll('dialog.modal').forEach(dlg => {
          const id = dlg.id.replace(/^modal-/, '');
          this.modals[id] = dlg;
          this._wireDialog(dlg, id);
        });
  
        // Delegacja – otwieranie po [data-modal]
        document.addEventListener('click', (e) => {
          const opener = e.target.closest('[data-modal]');
          if (!opener) return;
          const id = opener.getAttribute('data-modal');
          const dlg = this.modals[id];
          if (!dlg) return;
          e.preventDefault(); // progressive enhancement – zamiast przeładowania
          this.open(id);
        });
      }
  
      open(id) {
        const dlg = this.modals[id];
        if (!dlg || typeof dlg.showModal !== 'function') return;
        dlg.showModal();
        const firstFocusable = dlg.querySelector('input,select,textarea,button');
        if (firstFocusable) setTimeout(() => firstFocusable.focus(), 30);
        document.dispatchEvent(new CustomEvent('modal:opened', { detail: { id } }));
      }
  
      close(id) {
        const dlg = this.modals[id];
        if (!dlg) return;
        dlg.close();
        document.dispatchEvent(new CustomEvent('modal:closed', { detail: { id } }));
      }
  
      _wireDialog(dlg, id) {
        // Klik poza kartą = zamknij
        dlg.addEventListener('click', (e) => {
          const card = dlg.querySelector('.modal__card');
          if (!card) return;
          const r = card.getBoundingClientRect();
          const inside = e.clientX >= r.left && e.clientX <= r.right && e.clientY >= r.top && e.clientY <= r.bottom;
          if (!inside) this.close(id);
        });
  
        // [x] oraz [data-dismiss]
        dlg.querySelectorAll('.modal__close,[data-dismiss]').forEach(btn => {
          btn.addEventListener('click', () => this.close(id));
        });
  
        // ESC
        dlg.addEventListener('cancel', (e) => {
          e.preventDefault();
          this.close(id);
        });
      }
    }
  
    // Inicjalizacja (skrypt jest wczytywany z defer)
    window.HRMS = window.HRMS || {};
    window.HRMS.Modal = new ModalManager();
  
    /* ===== (Opcjonalnie) Auto-uzupełnianie w "Edit" po wpisaniu ID =====
       Wymaga endpointu GET /neo/find/{id} zwracającego JSON:
       { firstName, lastName, email, phone, hireDate, jobId, departmentId }
    */
    /*
    (function setupEditAutofill(){
      const dlg = document.getElementById('modal-edit');
      if (!dlg) return;
      const idInput = dlg.querySelector('input[name="employeeId"]');
      const fields = {
        firstName: dlg.querySelector('input[name="firstName"]'),
        lastName: dlg.querySelector('input[name="lastName"]'),
        email: dlg.querySelector('input[name="email"]'),
        phone: dlg.querySelector('input[name="phone"]'),
        hireDate: dlg.querySelector('input[name="hireDate"]'),
        jobId: dlg.querySelector('input[name="jobId"]'),
        departmentId: dlg.querySelector('input[name="departmentId"]')
      };
  
      let ctrl;
      idInput.addEventListener('input', () => {
        const id = idInput.value.trim();
        if (!id) return;
        if (ctrl) ctrl.abort();
        ctrl = new AbortController();
        fetch(`/neo/find/${encodeURIComponent(id)}`, { signal: ctrl.signal, headers: { 'Accept': 'application/json' } })
          .then(r => r.ok ? r.json() : null)
          .then(data => {
            if (!data) return;
            Object.entries(fields).forEach(([k, el]) => { if (el && data[k] != null) el.value = data[k]; });
          })
          .catch(() => {});
      });
    })();
    */
  })();
  