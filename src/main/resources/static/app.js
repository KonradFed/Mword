// === Shared selection state ===
let selectedId = null;

// highlight both tables by data-id
function selectRow(tr) {
  selectedId = tr.getAttribute('data-id');
  document.querySelectorAll('table tbody tr').forEach(r => {
    if (r.getAttribute('data-id') === selectedId) r.classList.add('sel');
    else r.classList.remove('sel');
  });
}

// ===== Buttons =====
const btnCreate = document.getElementById('btnCreate');
const btnRead   = document.getElementById('btnRead');
const btnUpdate = document.getElementById('btnUpdate');
const btnDelete = document.getElementById('btnDelete');

btnCreate?.addEventListener('click', () => openModal('createModal'));

btnRead?.addEventListener('click', () => {
  const u = new URL(window.location.href);
  u.searchParams.set('refresh','1');
  window.location.href = u.toString();
});

btnUpdate?.addEventListener('click', async () => {
  if (!selectedId) {
    alert('Select a record you want to edit.');
    return;
  }
  try {
    const res = await fetch(`/api/employee/${selectedId}`);
    const data = await res.json();

    const pg = data.pg || {};
    const neo = data.neo || {};

    // fill form
    document.getElementById('editId')?.setAttribute('value', pg.employeeId ?? selectedId);
    document.getElementById('editFirst')?.setAttribute('value', pg.firstName  ?? '');
    document.getElementById('editLast')?.setAttribute('value',  pg.lastName   ?? '');
    document.getElementById('editEmail')?.setAttribute('value', pg.email      ?? '');
    document.getElementById('editPhone')?.setAttribute('value', pg.phone      ?? '');
    document.getElementById('editHire')?.setAttribute('value',  (pg.hireDate ?? neo.hireDate) ?? '');

    const editDept = document.getElementById('editDept');
    if (editDept) editDept.value = pg.departmentId ? String(pg.departmentId) : '';

    const editJob = document.getElementById('editJob');
    if (editJob) {
      editJob.disabled = !editDept?.value;
      editJob.innerHTML = '<option value="">— choose job —</option>';
      if (editDept?.value) {
        const jobs = await (await fetch(`/api/jobs?deptId=${editDept.value}`)).json();
        jobs.forEach(j => {
          const opt = document.createElement('option');
          opt.value = j.jobId;
          opt.textContent = j.title ?? (`Job #${j.jobId}`);
          editJob.appendChild(opt);
        });
        if (pg.jobId) editJob.value = String(pg.jobId);
      }
    }

    document.getElementById('editDeptName')?.setAttribute('value', neo.departmentName ?? pg.departmentName ?? '');
    document.getElementById('editLocation')?.setAttribute('value', neo.location ?? '');

    openModal('editModal');
  } catch (e) {
    console.error(e);
    alert('Could not load record for edit.');
  }
});

btnDelete?.addEventListener('click', () => {
  if (!selectedId) {
    alert('Select a record you want to delete.');
    return;
  }
  document.getElementById('deleteId').value = selectedId;

  const row = document.querySelector(`#pgTable tr[data-id="${selectedId}"]`);
  const fn  = row?.children?.[1]?.textContent ?? '';
  const ln  = row?.children?.[2]?.textContent ?? '';
  const txt = (fn || ln) ? `Are you sure you want to delete "${fn} ${ln}" in both databases?`
                         : `Are you sure you want to delete employee #${selectedId} in both databases?`;
  document.getElementById('deleteText').textContent = txt;

  openModal('deleteModal');
});

// ===== Modals helpers =====
function openModal(id) {
  document.getElementById(id)?.classList.add('open');
}
function closeModal(id) {
  document.getElementById(id)?.classList.remove('open');
}

// Expose selectRow for inline onclick in Thymeleaf rows
window.selectRow = selectRow;
window.closeModal = closeModal;
window.openModal = openModal;
