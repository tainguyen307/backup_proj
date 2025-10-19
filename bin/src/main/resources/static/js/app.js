// WomTech.com - Main JavaScript for Spring Boot Application

// Global variables
let isLoading = false;

// DOM Content Loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

// Initialize application
function initializeApp() {
    // Initialize tooltips
    initializeTooltips();
    
    // Initialize smooth scroll
    initializeSmoothScroll();
    
    // Initialize form validation
    initializeFormValidation();
    
    // Initialize cart functionality
    if (isUserLoggedIn()) {
        updateCartBadge();
    }
    
    // Initialize search functionality
    initializeSearch();
    
    console.log('WomTech.com - Application initialized successfully!');
}

// Initialize Bootstrap tooltips
function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Initialize smooth scroll for anchor links
function initializeSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// Initialize form validation
function initializeFormValidation() {
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });
}

// Initialize search functionality
function initializeSearch() {
    const searchInput = document.querySelector('input[name="search"]');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                // Auto-suggest can be implemented here
            }, 300);
        });
    }
}

// Check if user is logged in
function isUserLoggedIn() {
    return document.querySelector('#cartBadge') !== null;
}

// Cart functionality
function updateCartBadge() {
    if (!isUserLoggedIn()) return;
    
    fetch('/cart/count', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        const badge = document.getElementById('cartBadge');
        if (badge) {
            if (data.totalItems > 0) {
                badge.textContent = data.totalItems;
                badge.style.display = 'flex';
            } else {
                badge.style.display = 'none';
            }
        }
    })
    .catch(error => console.log('Error updating cart badge:', error));
}

// Add product to cart
function addToCart(productId, quantity = 1) {
    if (!isUserLoggedIn()) {
        showAlert('warning', 'Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!');
        return;
    }
    
    if (isLoading) return;
    
    setLoading(true);
    
    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `productId=${productId}&quantity=${quantity}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert('success', data.message);
            updateCartBadge();
        } else {
            showAlert('danger', data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', 'Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng!');
    })
    .finally(() => {
        setLoading(false);
    });
}

// Remove product from cart
function removeFromCart(productId) {
    if (!isUserLoggedIn()) return;
    
    if (isLoading) return;
    
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?')) {
        return;
    }
    
    setLoading(true);
    
    fetch('/cart/remove', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `productId=${productId}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert('success', data.message);
            updateCartBadge();
            // Reload page to update cart display
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert('danger', data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', 'Có lỗi xảy ra khi xóa sản phẩm!');
    })
    .finally(() => {
        setLoading(false);
    });
}

// Update cart item quantity
function updateCartQuantity(productId, quantity) {
    if (!isUserLoggedIn() || quantity < 1) return;
    
    if (isLoading) return;
    
    setLoading(true);
    
    fetch('/cart/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `productId=${productId}&quantity=${quantity}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            updateCartBadge();
            // Update price displays if on cart page
            updateCartTotals();
        } else {
            showAlert('danger', data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', 'Có lỗi xảy ra khi cập nhật giỏ hàng!');
    })
    .finally(() => {
        setLoading(false);
    });
}

// Update cart totals (for cart page)
function updateCartTotals() {
    const totalElements = document.querySelectorAll('.cart-total');
    if (totalElements.length > 0) {
        // Reload cart page to get updated totals
        setTimeout(() => {
            window.location.reload();
        }, 500);
    }
}

// Show alert message
function showAlert(type, message, duration = 5000) {
    // Remove existing alerts
    const existingAlerts = document.querySelectorAll('.alert-auto-dismiss');
    existingAlerts.forEach(alert => alert.remove());
    
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show alert-auto-dismiss`;
    alertDiv.style.position = 'fixed';
    alertDiv.style.top = '20px';
    alertDiv.style.right = '20px';
    alertDiv.style.zIndex = '9999';
    alertDiv.style.minWidth = '300px';
    alertDiv.innerHTML = `
        <i class="bi bi-${getAlertIcon(type)} me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alertDiv);
    
    // Auto dismiss
    setTimeout(() => {
        if (alertDiv && alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, duration);
}

// Get icon for alert type
function getAlertIcon(type) {
    const icons = {
        'success': 'check-circle',
        'danger': 'exclamation-triangle',
        'warning': 'exclamation-triangle',
        'info': 'info-circle'
    };
    return icons[type] || 'info-circle';
}

// Set loading state
function setLoading(loading) {
    isLoading = loading;
    const buttons = document.querySelectorAll('.btn');
    
    if (loading) {
        buttons.forEach(btn => {
            if (!btn.disabled) {
                btn.dataset.originalText = btn.innerHTML;
                btn.innerHTML = '<span class="spinner"></span> Loading...';
                btn.disabled = true;
            }
        });
    } else {
        buttons.forEach(btn => {
            if (btn.dataset.originalText) {
                btn.innerHTML = btn.dataset.originalText;
                btn.disabled = false;
                delete btn.dataset.originalText;
            }
        });
    }
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Debounce function
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Enhanced search with debounce
const debouncedSearch = debounce(function(query) {
    if (query.length > 2) {
        // Implement search suggestions here
        console.log('Searching for:', query);
    }
}, 300);

// Product quantity controls
function increaseQuantity(inputId) {
    const input = document.getElementById(inputId);
    if (input) {
        const currentValue = parseInt(input.value) || 1;
        input.value = currentValue + 1;
        
        // If on cart page, update cart
        if (input.dataset.productId) {
            updateCartQuantity(input.dataset.productId, input.value);
        }
    }
}

function decreaseQuantity(inputId) {
    const input = document.getElementById(inputId);
    if (input) {
        const currentValue = parseInt(input.value) || 1;
        if (currentValue > 1) {
            input.value = currentValue - 1;
            
            // If on cart page, update cart
            if (input.dataset.productId) {
                updateCartQuantity(input.dataset.productId, input.value);
            }
        }
    }
}

// Image lazy loading
function initializeLazyLoading() {
    const images = document.querySelectorAll('img[data-src]');
    
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy');
                observer.unobserve(img);
            }
        });
    });
    
    images.forEach(img => imageObserver.observe(img));
}

// Initialize on page load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeLazyLoading);
} else {
    initializeLazyLoading();
}

// Global error handler
window.addEventListener('error', function(e) {
    console.error('Global error:', e.error);
    // You can send error reports to server here
});

// Service Worker registration (for future PWA features)
if ('serviceWorker' in navigator) {
    window.addEventListener('load', function() {
        // navigator.serviceWorker.register('/sw.js').then(...);
        // Uncomment when you want to add PWA features
    });
}