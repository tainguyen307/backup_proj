document.addEventListener('DOMContentLoaded', function() {
    const navItems = document.querySelectorAll('.profile-nav-item');
    const tabContents = document.querySelectorAll('.profile-tab-content');

    const urlParams = new URLSearchParams(window.location.search);
    const defaultTab = urlParams.get('tab') || 'info';
    setActiveTab(defaultTab);

    navItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const tab = this.getAttribute('data-tab');
            setActiveTab(tab);

            const newUrl = new URL(window.location);
            newUrl.searchParams.set('tab', tab);
            window.history.pushState({}, '', newUrl);
        });
    });

    function setActiveTab(tab) {
        navItems.forEach(i => i.classList.remove('active'));
        tabContents.forEach(c => c.classList.remove('active'));

        const navItem = document.querySelector(`.profile-nav-item[data-tab="${tab}"]`);
        const tabContent = document.getElementById(`${tab}-tab`);
        if(navItem) navItem.classList.add('active');
        if(tabContent) {
            tabContent.classList.add('active');
            const header = tabContent.querySelector('.card-header');
            if(header){
                header.classList.add('glow');
                setTimeout(() => header.classList.remove('glow'), 800);
            }
        }
    }

    // Password toggle
    window.togglePassword = function(fieldId){
        const field = document.getElementById(fieldId);
        const button = field.nextElementSibling.querySelector('i');
        if(field.type==='password'){
            field.type='text';
            button.classList.replace('bi-eye','bi-eye-slash');
        }else{
            field.type='password';
            button.classList.replace('bi-eye-slash','bi-eye');
        }
    }

    // Password strength
    const newPassword = document.getElementById('newPassword');
    const confirmPassword = document.getElementById('confirmPassword');
    if(newPassword){
        newPassword.addEventListener('input', function(){
            const val = this.value;
            let strength = 0;
            if(val.length>=6) strength+=25;
            if(/[a-z]/.test(val)) strength+=25;
            if(/[A-Z]/.test(val)) strength+=25;
            if(/[0-9]/.test(val)) strength+=25;

            const bar = document.getElementById('passwordStrength');
            const text = document.getElementById('passwordStrengthText');
            bar.style.width = strength+'%';
            if(strength>=75){ bar.className='progress-bar bg-success'; text.textContent='Mật khẩu mạnh'; }
            else if(strength>=50){ bar.className='progress-bar bg-warning'; text.textContent='Mật khẩu trung bình'; }
            else if(strength>=25){ bar.className='progress-bar bg-warning'; text.textContent='Mật khẩu yếu'; }
            else { bar.className='progress-bar bg-danger'; text.textContent='Mật khẩu rất yếu'; }
        });
    }

    // Confirm password check
    if(confirmPassword && newPassword){
        confirmPassword.addEventListener('input', function(){
            if(this.value!==newPassword.value){
                this.setCustomValidity('Mật khẩu xác nhận không khớp!');
            }else{
                this.setCustomValidity('');
            }
        });
    }
});

function showEditForm(id) {
	document.getElementById('view-' + id).classList.add('d-none');
	document.getElementById('form-' + id).classList.remove('d-none');
}

function cancelEdit(id) {
	document.getElementById('view-' + id).classList.remove('d-none');
	document.getElementById('form-' + id).classList.add('d-none');
}

function toggleAddAddressForm() {
	document.getElementById('add-address-form').classList.toggle('d-none');
}
