(() => {
  const q  = (s, r) => (r||document).querySelector(s);
  const qa = (s, r) => Array.from((r||document).querySelectorAll(s));

  /* ---------- dialog helpers ---------- */
  const openModal  = name => { const d=q('#modal-'+name); if(!d) return; if(d.showModal) d.showModal(); else d.setAttribute('open',''); };
  const closeModal = dlg  => { if(!dlg) return; if(dlg.close) dlg.close(); else dlg.removeAttribute('open'); };

  /* ---------- utils ---------- */
  function toIsoDate(d){
    if(!d) return '';
    if(/^\d{4}-\d{2}-\d{2}$/.test(d)) return d;
    const m = String(d||'').match(/^(\d{1,2})[.\-/ ](\d{1,2})[.\-/ ](\d{4})$/);
    if(m) return `${m[3]}-${m[2].padStart(2,'0')}-${m[1].padStart(2,'0')}`;
    const dt = new Date(d);
    if(!isNaN(dt)){
      const mm = String(dt.getMonth()+1).padStart(2,'0');
      const dd = String(dt.getDate()).padStart(2,'0');
      return `${dt.getFullYear()}-${mm}-${dd}`;
    }
    return '';
  }

  function textTrim(el){ return (el?.textContent||'').trim(); }

  function markSelectedRows(employeeId){
    const idStr = String(employeeId||'').trim();
    ['#pg-table','#neo-table'].forEach(sel=>{
      const table=q(sel); if(!table) return;
      qa('tbody tr', table).forEach(tr=>{
        const trId=(tr.getAttribute('data-id')||'').trim();
        if(trId===idStr){ tr.classList.add('selected'); } else tr.classList.remove('selected');
      });
    });
    // ukryte inputy do komunikatów/akcji
    const hidden = q('#selected-id'); if(hidden) hidden.value=idStr;

    const trPG  = q(`#pg-table  tr[data-id="${idStr}"]`);
    const trNeo = q(`#neo-table tr[data-id="${idStr}"]`);
    const first = trPG?.getAttribute('data-first') || trNeo?.getAttribute('data-first') || '';
    const last  = trPG?.getAttribute('data-last')  || trNeo?.getAttribute('data-last')  || '';
    const hName = q('#selected-name'); if(hName) hName.value = `${first} ${last}`.trim();
  }

  /* ---------- job helpers ---------- */
  /**
   * Ustawia value selecta jobId po jobId lub – jeśli brak – po tytule.
   */
  function setJobSelect(select, jobId, jobTitle){
    if(!select) return;
    // 1) preferuj value == jobId
    if(jobId != null){
      const val = String(jobId);
      const opt = qa('option', select).find(o => o.value === val);
      if(opt){ select.value = val; return; }
    }
    // 2) fallback po tytule (porównanie tekstu lub data-title)
    if(jobTitle){
      const titleNorm = String(jobTitle).trim().toLowerCase();
      const opt2 = qa('option', select).find(o => {
        const t = (o.dataset.title || o.textContent || '').trim().toLowerCase();
        return t === titleNorm;
      });
      if(opt2){ select.value = opt2.value; return; }
    }
    // 3) nic nie dopasowano – wyczyść wybór (zostaw placeholder)
    if(select.querySelector('option[disabled]')) {
      select.value = '';
    }
  }

  /**
   * Gdy użytkownik wybiera job w selekcie, przepisz tytuł do pola tekstowego (jeśli istnieje).
   */
  function mirrorJobTitleOnChange(selectId, titleInputId){
    const sel = q(selectId);
    const titleInput = q(titleInputId);
    if(!sel || !titleInput) return;
    sel.addEventListener('change', ()=>{
      const opt = sel.options[sel.selectedIndex];
      if(!opt) return;
      const t = (opt.dataset.title || opt.textContent || '').trim();
      titleInput.value = t;
    });
  }

  /* ---------- fill Edit modal from PG row ---------- */
  function fillEditFromPgRow(idStr){
    const tr = q(`#pg-table tr[data-id="${idStr}"]`);
    if(!tr) return false;

    // pola podstawowe
    q('#edit-employeeId').value = idStr;
    q('#edit-firstName').value  = tr.getAttribute('data-first') || '';
    q('#edit-lastName').value   = tr.getAttribute('data-last')  || '';
    q('#edit-email').value      = tr.getAttribute('data-email') || '';
    q('#edit-phone').value      = tr.getAttribute('data-phone') || '';
    q('#edit-hireDate').value   = toIsoDate(tr.getAttribute('data-hire') || '');

    // department select
    const depSel = q('#edit-departmentId');
    const depId  = tr.getAttribute('data-dept-id');
    if(depSel){
      if(depId) depSel.value = String(depId);
      else if(depSel.querySelector('option[disabled]')) depSel.value = '';
    }

    // job select + tytuł
    const jobSel = q('#edit-jobId');
    const jobId  = tr.getAttribute('data-job-id');
    const jobT   = tr.getAttribute('data-job') || ''; // alias jobTitle
    setJobSelect(jobSel, jobId, jobT);

    // pole tytułu (jeśli zostawiamy widoczne)
    const titleInput = q('#edit-title');
    if(titleInput){
      if(jobSel && jobSel.value){
        const opt = jobSel.options[jobSel.selectedIndex];
        titleInput.value = (opt?.dataset.title || opt?.textContent || jobT || '').trim();
      } else {
        titleInput.value = jobT;
      }
    }

    // lokalizacja (z wiersza PG)
    const loc = tr.getAttribute('data-loc') || '';
    const locInput = q('#edit-location'); if(locInput) locInput.value = loc;

    return true;
  }

  /* ---------- attach row click selection ---------- */
  function attachTableRowSelection(){
    [['#pg-table'],['#neo-table']].forEach(([sel])=>{
      const tbody = q(sel+' tbody'); if(!tbody) return;
      tbody.addEventListener('click', (ev)=>{
        const a = ev.target.closest('a[href^="mailto:"]'); if(a){ ev.preventDefault(); }
        const tr = ev.target.closest('tr[data-id]'); if(!tr) return;
        const id = tr.getAttribute('data-id'); if(!id) return;
        markSelectedRows(id);
      });
    });
  }

  /* ---------- attach modal buttons ---------- */
  function attachModalButtons(){
    qa('[data-modal]').forEach(btn=>{
      btn.addEventListener('click', (e)=>{
        e.preventDefault();
        const name = btn.dataset.modal;
        const id = (q('#selected-id').value||'').trim();

        if((name==='edit' || name==='delete') && !id){
          alert(name==='edit' ? 'Najpierw wybierz rekord, który chcesz edytować.'
                              : 'Najpierw wybierz rekord, który chcesz usunąć.');
          return;
        }

        if(name==='add'){
          // wyczyść pola w add (opcjonalnie)
          const form = q('#modal-add form');
          if(form){
            form.reset?.();
            // placeholdery selectów
            const dep = form.querySelector('select[name="departmentId"]');
            if(dep && dep.querySelector('option[disabled]')) dep.value = '';
            const job = form.querySelector('select[name="jobId"]');
            if(job && job.querySelector('option[disabled]')) job.value = '';
          }
        }

        if(name==='edit'){
          // wypełnij modal danymi z wiersza PG
          const ok = fillEditFromPgRow(id);
          if(!ok){ alert('Nie znaleziono danych w tabeli PG dla wybranego rekordu.'); return; }
        }

        if(name==='delete'){
          q('#delete-employeeId').value = id;
          q('#delete-fullname').textContent = q('#selected-name').value || 'wybrany rekord';
        }

        openModal(name);
      });
    });
  }

  /* ---------- close modals (X, Anuluj, klik poza kartę, Esc) ---------- */
  function attachModalClosing(){
    qa('dialog .modal__close, dialog [data-dismiss]').forEach(el=>{
      el.addEventListener('click',(ev)=>{ ev.preventDefault(); closeModal(el.closest('dialog')); });
    });
    qa('dialog').forEach(dlg=>{
      // klik w tło
      dlg.addEventListener('click', (ev)=>{
        const card = q('.modal__card', dlg); if(!card) return;
        const r = card.getBoundingClientRect();
        const inside = ev.clientX>=r.left && ev.clientX<=r.right && ev.clientY>=r.top && ev.clientY<=r.bottom;
        if(!inside) closeModal(dlg);
      });
      // Esc
      dlg.addEventListener('cancel', (ev)=>{ ev.preventDefault(); closeModal(dlg); });
    });
  }

  /* ---------- mirror job title on change (Create/Update) ---------- */
  function attachJobMirroring(){
    mirrorJobTitleOnChange('#modal-add select[name="jobId"]',  '#modal-add input[name="title"]');
    mirrorJobTitleOnChange('#modal-edit select[name="jobId"]', '#modal-edit input[name="title"]');
  }

  /* ---------- bootstrap ---------- */
  document.addEventListener('DOMContentLoaded', ()=>{
    attachTableRowSelection();
    attachModalButtons();
    attachModalClosing();
    attachJobMirroring();

    // Jeżeli po wejściu na stronę w tabeli jest cokolwiek, automatycznie wybierz pierwszy wiersz (opcjonalnie)
    const firstRow = q('#pg-table tbody tr[data-id]') || q('#neo-table tbody tr[data-id]');
    if(firstRow){
      markSelectedRows(firstRow.getAttribute('data-id'));
    }
  });
})();
