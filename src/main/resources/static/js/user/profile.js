// Tab navigation
document.addEventListener('DOMContentLoaded', () => {
    const navItems = document.querySelectorAll('.profile-nav-item');
    const tabContents = document.querySelectorAll('.profile-tab-content');
    const urlParams = new URLSearchParams(window.location.search);
    const defaultTab = urlParams.get('tab') || 'info';

    setActiveTab(defaultTab);

    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const tab = item.dataset.tab;
            setActiveTab(tab);
            updateUrl(tab);
        });
    });

    function setActiveTab(tab) {
        navItems.forEach(i => i.classList.remove('active'));
        tabContents.forEach(c => c.classList.remove('active'));

        const navItem = document.querySelector(`.profile-nav-item[data-tab="${tab}"]`);
        const tabContent = document.getElementById(`${tab}-tab`);
        
        if (navItem) navItem.classList.add('active');
        if (tabContent) {
            tabContent.classList.add('active');
            const header = tabContent.querySelector('.card-header');
            if (header) {
                header.classList.add('glow');
                setTimeout(() => header.classList.remove('glow'), 800);
            }
        }
    }

    function updateUrl(tab) {
        const newUrl = new URL(window.location);
        newUrl.searchParams.set('tab', tab);
        window.history.pushState({}, '', newUrl);
    }
});

// Password handling
function togglePassword(fieldId) {
    const field = document.getElementById(fieldId);
    if (!field) return;
    const button = field.nextElementSibling?.querySelector('i');
    if (!button) return;
    field.type = field.type === 'password' ? 'text' : 'password';
    button.classList.toggle('bi-eye');
    button.classList.toggle('bi-eye-slash');
}

document.addEventListener('DOMContentLoaded', () => {
    const newPassword = document.getElementById('newPassword');
    const confirmPassword = document.getElementById('confirmPassword');

    if (newPassword) {
        newPassword.addEventListener('input', function() {
            const val = this.value;
            let strength = 0;
            if (val.length >= 6) strength += 25;
            if (/[a-z]/.test(val)) strength += 25;
            if (/[A-Z]/.test(val)) strength += 25;
            if (/[0-9]/.test(val)) strength += 25;

            const bar = document.getElementById('passwordStrength');
            const text = document.getElementById('passwordStrengthText');
            if (bar && text) {
                bar.style.width = `${strength}%`;
                if (strength >= 75) {
                    bar.className = 'progress-bar bg-success';
                    text.textContent = 'Mật khẩu mạnh';
                } else if (strength >= 50) {
                    bar.className = 'progress-bar bg-warning';
                    text.textContent = 'Mật khẩu trung bình';
                } else if (strength >= 25) {
                    bar.className = 'progress-bar bg-warning';
                    text.textContent = 'Mật khẩu yếu';
                } else {
                    bar.className = 'progress-bar bg-danger';
                    text.textContent = 'Mật khẩu rất yếu';
                }
            }
        });
    }

    if (confirmPassword && newPassword) {
        confirmPassword.addEventListener('input', function() {
            this.setCustomValidity(this.value !== newPassword.value ? 'Mật khẩu xác nhận không khớp!' : '');
        });
    }
});

// Address form handling
function toggleAddAddressForm() {
    const form = document.getElementById('add-address-form');
    if (!form) return;
    form.classList.toggle('d-none');
    if (!form.classList.contains('d-none')) setTimeout(initAddMap, 100);
}

function showEditForm(id) {
    const view = document.getElementById(`view-${id}`);
    const form = document.getElementById(`form-${id}`);
    if (view && form) {
        view.classList.add('d-none');
        form.classList.remove('d-none');
        setTimeout(() => {
            const latInput = document.getElementById(`latitude-${id}`);
            const lngInput = document.getElementById(`longitude-${id}`);
            const lat = latInput ? parseFloat(latInput.value) || 10.762622 : 10.762622;
            const lng = lngInput ? parseFloat(lngInput.value) || 106.660172 : 106.660172;
            initEditMap(id, lat, lng);
            initAddressDropdowns(id);
        }, 100);
    }
}

function cancelEdit(id) {
    const form = document.getElementById(`form-${id}`);
    const view = document.getElementById(`view-${id}`);
    if (form && view) {
        form.classList.add('d-none');
        view.classList.remove('d-none');
    }
}

// Address dropdowns
async function initAddressDropdowns(id = null) {
    const formId = id ? `form-${id}` : 'add-address-form';
    const provinceSelect = document.getElementById(`provinceSelect${id ? `-${id}` : ''}`);
    const districtSelect = document.getElementById(`districtSelect${id ? `-${id}` : ''}`);
    const wardSelect = document.getElementById(`wardSelect${id ? `-${id}` : ''}`);

    const provinceIdInput = document.querySelector(`#${formId} input[name="cityId"]`);
    const districtIdInput = document.querySelector(`#${formId} input[name="districtId"]`);
    const wardIdInput = document.querySelector(`#${formId} input[name="wardId"]`);
    const provinceNameInput = document.querySelector(`#${formId} input[name="city"]`);
    const districtNameInput = document.querySelector(`#${formId} input[name="district"]`);
    const wardNameInput = document.querySelector(`#${formId} input[name="ward"]`);

    if (!provinceSelect || !districtSelect || !wardSelect || !provinceNameInput || !districtNameInput || !wardNameInput) {
        console.error(`One or more elements not found in form ${formId}`);
        return;
    }

    try {
        // Load provinces
        const provinces = await fetch('/api/ghn/provinces').then(res => res.json());
        provinceSelect.innerHTML = '<option value="">-- Chọn Tỉnh/Thành --</option>';
        provinces.forEach(p => {
            const option = new Option(p.ProvinceName, p.ProvinceID);
            if (provinceIdInput && p.ProvinceID === provinceIdInput.value) option.selected = true;
            provinceSelect.add(option);
        });
        provinceNameInput.value = provinceSelect.selectedOptions[0]?.text || '';

        async function loadDistricts(provinceId, selectedDistrictId = '') {
            districtSelect.innerHTML = '<option value="">-- Chọn Quận/Huyện --</option>';
            wardSelect.innerHTML = '<option value="">-- Chọn Phường/Xã --</option>';
            if (districtIdInput) districtIdInput.value = '';
            if (districtNameInput) districtNameInput.value = '';
            if (wardIdInput) wardIdInput.value = '';
            if (wardNameInput) wardNameInput.value = '';

            if (!provinceId) return;
            const districts = await fetch(`/api/ghn/districts/${provinceId}`).then(res => res.json());
            districts.forEach(d => {
                const option = new Option(d.DistrictName, d.DistrictID);
                if (d.DistrictID === selectedDistrictId) option.selected = true;
                districtSelect.add(option);
            });
            districtNameInput.value = districtSelect.selectedOptions[0]?.text || '';
        }

        async function loadWards(districtId, selectedWardId = '') {
            wardSelect.innerHTML = '<option value="">-- Chọn Phường/Xã --</option>';
            if (wardIdInput) wardIdInput.value = '';
            if (wardNameInput) wardNameInput.value = '';

            if (!districtId) return;
            const wards = await fetch(`/api/ghn/wards/${districtId}`).then(res => res.json());
            wards.forEach(w => {
                const option = new Option(w.WardName, w.WardCode);
                if (w.WardCode === selectedWardId) option.selected = true;
                wardSelect.add(option);
            });
            wardNameInput.value = wardSelect.selectedOptions[0]?.text || '';
        }

        provinceSelect.addEventListener('change', async () => {
            if (provinceIdInput) provinceIdInput.value = provinceSelect.value;
            provinceNameInput.value = provinceSelect.selectedOptions[0]?.text || '';
            await loadDistricts(provinceSelect.value);
            if (!id) goToArea(districtSelect.value, wardSelect.value);
            else goToEditArea(id, districtSelect.value, wardSelect.value);
        });

        districtSelect.addEventListener('change', async () => {
            if (districtIdInput) districtIdInput.value = districtSelect.value;
            districtNameInput.value = districtSelect.selectedOptions[0]?.text || '';
            await loadWards(districtSelect.value);
            if (!id) goToArea(districtSelect.value, wardSelect.value);
            else goToEditArea(id, districtSelect.value, wardSelect.value);
        });

        wardSelect.addEventListener('change', () => {
            if (wardIdInput) wardIdInput.value = wardSelect.value;
            wardNameInput.value = wardSelect.selectedOptions[0]?.text || '';
            if (!id) goToArea(districtSelect.value, wardSelect.value);
            else goToEditArea(id, districtSelect.value, wardSelect.value);
        });

        if (provinceIdInput?.value) {
            await loadDistricts(provinceIdInput.value, districtIdInput?.value);
            if (districtIdInput?.value) await loadWards(districtIdInput.value, wardIdInput?.value);
        }
    } catch (err) {
        console.error('Error initializing address dropdowns:', err);
    }
}

// Map handling
let addMap, addMarker, addBounds = null;
const editMaps = {}, editMarkers = {}, editBounds = {};

async function updateAddressFromLatLng(lat, lng, id = null) {
    try {
        const prefix = id ? `#form-${id}` : '#add-address-form';
        const streetInput = document.querySelector(`${prefix} input[name="street"]`);
        const wardSelect = document.querySelector(`${prefix} select[name="ward"]`);
        const districtSelect = document.querySelector(`${prefix} select[name="district"]`);
        const provinceSelect = document.querySelector(`${prefix} select[name="province"]`);

        let street = '';

        // Photon reverse geocoding
        const res = await fetch(`https://photon.komoot.io/reverse?lat=${lat}&lon=${lng}`);
        const data = await res.json();
        if (data.features && data.features.length > 0) {
            const feature = data.features[0].properties;
            if (feature.name) {
                street = feature.name;
            }
        }

        // Fallback: nếu ko có name, dùng ward/district/province
        if (!street) {
            const wardName = wardSelect?.selectedOptions[0]?.text || '';
            const districtName = districtSelect?.selectedOptions[0]?.text || '';
            const provinceName = provinceSelect?.selectedOptions[0]?.text || '';
            street = wardName ? `${wardName}, ${districtName}, ${provinceName}` : `${districtName}, ${provinceName}`;
        }

        if (streetInput) streetInput.value = street;

    } catch (err) {
        console.error('Photon geocoding error:', err);
    }
}

async function goToArea(districtId, wardId) {
    if (!districtId || !addMap || !addMarker) return;

    const provinceSelect = document.getElementById('provinceSelect');
    const districtSelect = document.getElementById('districtSelect');
    const wardSelect = document.getElementById('wardSelect');

    const provinceName = provinceSelect?.selectedOptions[0]?.text || '';
    const districtName = districtSelect?.selectedOptions[0]?.text || '';
    const wardName = wardSelect?.selectedOptions[0]?.text || '';

    const queries = [];
    if (wardName) queries.push(`${wardName}, ${districtName}, ${provinceName}`);
    queries.push(`${districtName}, ${provinceName}`);

    for (let q of queries) {
        try {
            const res = await fetch(`https://photon.komoot.io/api/?q=${encodeURIComponent(q)}&limit=1`);
            const data = await res.json();
            if (data.features && data.features.length > 0) {
                const feature = data.features[0];
                const [minLat, minLon, maxLat, maxLon] = feature.bbox || [
                    feature.geometry.coordinates[1],
                    feature.geometry.coordinates[0],
                    feature.geometry.coordinates[1],
                    feature.geometry.coordinates[0]
                ];

                addBounds = L.latLngBounds(
                    [minLat, minLon],
                    [maxLat, maxLon]
                );
                addMap.fitBounds(addBounds);

                const center = addBounds.getCenter();
                addMarker.setLatLng(center);

                document.getElementById('latitude-add').value = center.lat;
                document.getElementById('longitude-add').value = center.lng;

                await updateAddressFromLatLng(center.lat, center.lng);
                return;
            }
        } catch (err) {
            console.error('Photon goToArea error:', err);
        }
    }
}

async function goToEditArea(id, districtId, wardId) {
    if (!districtId || !editMaps[id] || !editMarkers[id]) return;

    const provinceSelect = document.getElementById(`provinceSelect-${id}`);
    const districtSelect = document.getElementById(`districtSelect-${id}`);
    const wardSelect = document.getElementById(`wardSelect-${id}`);

    const provinceName = provinceSelect?.selectedOptions[0]?.text || '';
    const districtName = districtSelect?.selectedOptions[0]?.text || '';
    const wardName = wardSelect?.selectedOptions[0]?.text || '';

    const queries = [];
    if (wardName) queries.push(`${wardName}, ${districtName}, ${provinceName}`);
    queries.push(`${districtName}, ${provinceName}`);

    for (let q of queries) {
        try {
            const res = await fetch(`https://photon.komoot.io/api/?q=${encodeURIComponent(q)}&limit=1`);
            const data = await res.json();
            if (data.features && data.features.length > 0) {
                const feature = data.features[0];
                const [minLat, minLon, maxLat, maxLon] = feature.bbox || [
                    feature.geometry.coordinates[1],
                    feature.geometry.coordinates[0],
                    feature.geometry.coordinates[1],
                    feature.geometry.coordinates[0]
                ];

                editBounds[id] = L.latLngBounds(
                    [minLat, minLon],
                    [maxLat, maxLon]
                );
                editMaps[id].fitBounds(editBounds[id]);

                const center = editBounds[id].getCenter();
                editMarkers[id].setLatLng(center);

                document.getElementById(`latitude-${id}`).value = center.lat;
                document.getElementById(`longitude-${id}`).value = center.lng;

                await updateAddressFromLatLng(center.lat, center.lng, id);
                return;
            }
        } catch (err) {
            console.error(`Photon goToEditArea error for id ${id}:`, err);
        }
    }
}

function initAddMap() {
    const container = document.getElementById('map-add');
    if (!container) return;

    if (addMap) { addMap.invalidateSize(); return; }

    const defaultLat = 10.762622, defaultLng = 106.660172;
    addMap = L.map('map-add').setView([defaultLat, defaultLng], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(addMap);
    addMarker = L.marker([defaultLat, defaultLng], { draggable: true }).addTo(addMap);

    addMarker.on('dragend', e => {
        const latLng = e.target.getLatLng();
        document.getElementById('latitude-add').value = latLng.lat;
        document.getElementById('longitude-add').value = latLng.lng;
        updateAddressFromLatLng(latLng.lat, latLng.lng);
    });

    addMap.on('click', e => {
        addMarker.setLatLng(e.latlng);
        document.getElementById('latitude-add').value = e.latlng.lat;
        document.getElementById('longitude-add').value = e.latlng.lng;
        updateAddressFromLatLng(e.latlng.lat, e.latlng.lng);
    });

    const districtSelect = document.getElementById('districtSelect');
    const wardSelect = document.getElementById('wardSelect');

    districtSelect?.addEventListener('change', () => goToArea(districtSelect.value, wardSelect.value));
    wardSelect?.addEventListener('change', () => goToArea(districtSelect.value, wardSelect.value));

    goToArea(districtSelect?.value, wardSelect?.value);
}

function initEditMap(id, lat = 10.762622, lng = 106.660172) {
    const container = document.getElementById(`map-${id}`);
    if (!container) return;

    if (editMaps[id]) { editMaps[id].invalidateSize(); return; }

    const map = L.map(`map-${id}`).setView([lat, lng], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(map);
    const marker = L.marker([lat, lng], { draggable: true }).addTo(map);

    marker.on('dragend', e => {
        const latLng = e.target.getLatLng();
        document.getElementById(`latitude-${id}`).value = latLng.lat;
        document.getElementById(`longitude-${id}`).value = latLng.lng;
        updateAddressFromLatLng(latLng.lat, latLng.lng, id);
    });

    map.on('click', e => {
        marker.setLatLng(e.latlng);
        document.getElementById(`latitude-${id}`).value = e.latlng.lat;
        document.getElementById(`longitude-${id}`).value = e.latlng.lng;
        updateAddressFromLatLng(e.latlng.lat, e.latlng.lng, id);
    });

    const districtSelect = document.getElementById(`districtSelect-${id}`);
    const wardSelect = document.getElementById(`wardSelect-${id}`);

    districtSelect?.addEventListener('change', () => goToEditArea(id, districtSelect.value, wardSelect.value));
    wardSelect?.addEventListener('change', () => goToEditArea(id, districtSelect.value, wardSelect.value));

    editMaps[id] = map;
    editMarkers[id] = marker;

    goToEditArea(id, districtSelect?.value, wardSelect?.value);
}

document.addEventListener('DOMContentLoaded', () => {
    initAddressDropdowns();

    // ======= Regex để kiểm tra địa chỉ hợp lệ =======
    // Cho phép: số, chữ, khoảng trắng, dấu / , . - , và ký tự tiếng Việt
    const streetRegex = /^[0-9A-Za-zÀ-ỹà-ỹ\s\/,.\-]+$/;

    // ======= Validate hàm chung cho cả Add và Edit =======
    function validateStreetInput(input) {
        const value = input.value.trim();
        if (!value) {
            alert('Vui lòng nhập địa chỉ (số nhà, tên đường).');
            input.focus();
            return false;
        }
        if (!streetRegex.test(value)) {
            alert('Địa chỉ chứa ký tự không hợp lệ. Vui lòng chỉ nhập chữ, số và ký tự cơ bản như "/", "-", ",".');
            input.focus();
            return false;
        }
        if (value.length < 3) {
            alert('Địa chỉ quá ngắn. Vui lòng nhập chi tiết hơn (ví dụ: "123 Nguyễn Trãi").');
            input.focus();
            return false;
        }
        return true;
    }

    // ======= Áp dụng cho form thêm địa chỉ =======
    const addForm = document.querySelector('#add-address-form form');
    if (addForm) {
        const addStreetInput = addForm.querySelector('input[name="street"]');
        addForm.addEventListener('submit', (e) => {
            if (!validateStreetInput(addStreetInput)) {
                e.preventDefault();
            }
        });
    }

    // ======= Áp dụng cho tất cả form chỉnh sửa =======
    const editForms = document.querySelectorAll('.edit-form');
    editForms.forEach(form => {
        const editStreetInput = form.querySelector('input[name="street"]');
        form.addEventListener('submit', (e) => {
            if (!validateStreetInput(editStreetInput)) {
                e.preventDefault();
            }
        });
    });
});

document.getElementById("avatarFile").addEventListener("change", function (event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            document.getElementById("avatarPreview").src = e.target.result;
        };
        reader.readAsDataURL(file);
    }
});
