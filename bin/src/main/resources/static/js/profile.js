// Tab Navigation
document.addEventListener('DOMContentLoaded', function() {
    const navItems = document.querySelectorAll('.profile-nav-item');
    const tabContents = document.querySelectorAll('.profile-tab-content');
    
    // Check URL params for active tab
    const urlParams = new URLSearchParams(window.location.search);
    const activeTab = urlParams.get('tab') || 'info';
    
    // Set active tab
    setActiveTab(activeTab);
    
    navItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const tab = this.getAttribute('data-tab');
            setActiveTab(tab);
            
            // Update URL without page reload
            const newUrl = new URL(window.location);
            newUrl.searchParams.set('tab', tab);
            window.history.pushState({}, '', newUrl);
        });
    });
    
    function setActiveTab(tab) {
        // Remove active classes
        navItems.forEach(item => item.classList.remove('active'));
        tabContents.forEach(content => content.classList.remove('active'));
        
        // Add active classes
        const activeNavItem = document.querySelector(`[data-tab="${tab}"]`);
        const activeTabContent = document.getElementById(`${tab}-tab`);
        
        if (activeNavItem) activeNavItem.classList.add('active');
        if (activeTabContent) activeTabContent.classList.add('active');
    }
});

// Toggle Password Visibility
function togglePassword(fieldId) {
    const field = document.getElementById(fieldId);
    const button = field.nextElementSibling.querySelector('i');
    
    if (field.type === 'password') {
        field.type = 'text';
        button.classList.remove('fa-eye');
        button.classList.add('fa-eye-slash');
    } else {
        field.type = 'password';
        button.classList.remove('fa-eye-slash');
        button.classList.add('fa-eye');
    }
}

// Password Strength Checker
document.addEventListener('DOMContentLoaded', () => {
    const newPasswordField = document.getElementById('newPassword');
    const confirmPasswordField = document.getElementById('confirmPassword');
    
    if (newPasswordField) {
        newPasswordField.addEventListener('input', function() {
            const password = this.value;
            const strengthBar = document.getElementById('passwordStrength');
            const strengthText = document.getElementById('passwordStrengthText');
            
            let strength = 0;
            let text = 'Mật khẩu rất yếu';
            let color = 'bg-danger';
            
            if (password.length >= 6) strength += 25;
            if (password.match(/[a-z]/)) strength += 25;
            if (password.match(/[A-Z]/)) strength += 25;
            if (password.match(/[0-9]/)) strength += 25;
            
            if (strength >= 75) {
                text = 'Mật khẩu mạnh';
                color = 'bg-success';
            } else if (strength >= 50) {
                text = 'Mật khẩu trung bình';
                color = 'bg-warning';
            } else if (strength >= 25) {
                text = 'Mật khẩu yếu';
                color = 'bg-warning';
            }
            
            strengthBar.className = `progress-bar ${color}`;
            strengthBar.style.width = strength + '%';
            strengthText.textContent = text;
        });
    }

    // Password Confirmation Checker
    if (confirmPasswordField) {
        confirmPasswordField.addEventListener('input', function() {
            const newPassword = newPasswordField.value;
            const confirmPassword = this.value;
            
            if (newPassword !== confirmPassword) {
                this.setCustomValidity('Mật khẩu xác nhận không khớp!');
            } else {
                this.setCustomValidity('');
            }
        });
    }
});
