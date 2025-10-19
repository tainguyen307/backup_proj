// JavaScript chỉ cho trang Dashboard
document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Dashboard loaded');
    
    // Chỉ xử lý các phần tử trong dashboard-container
    const dashboardContainer = document.querySelector('.dashboard-container');
    if (!dashboardContainer) return;
    
    // Inventory alert counter animation - chỉ trong dashboard
    function animateDashboardCounter(element, target) {
        let current = 0;
        const increment = target / 50;
        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                current = target;
                clearInterval(timer);
            }
            element.textContent = Math.floor(current);
        }, 30);
    }
    
    // Animate stat counters on page load - chỉ trong dashboard
    const statValues = dashboardContainer.querySelectorAll('.stat-value');
    statValues.forEach(stat => {
        const target = parseInt(stat.textContent);
        if (!isNaN(target) && target > 0) {
            animateDashboardCounter(stat, target);
        }
    });
    
    // Real-time inventory updates (simulated) - chỉ cho dashboard
    function simulateDashboardRealTimeUpdates() {
        setInterval(() => {
            const lowStockBadge = dashboardContainer.querySelector('.badge.bg-warning');
            if (lowStockBadge) {
                const currentCount = parseInt(lowStockBadge.textContent);
                const newCount = Math.max(0, currentCount + (Math.random() > 0.7 ? 1 : -1));
                if (newCount !== currentCount) {
                    lowStockBadge.textContent = newCount + ' sản phẩm';
                    lowStockBadge.classList.add('dashboard-pulse-animation');
                    setTimeout(() => {
                        lowStockBadge.classList.remove('dashboard-pulse-animation');
                    }, 1000);
                }
            }
        }, 10000);
    }
    
    // Start real-time updates
    simulateDashboardRealTimeUpdates();
    
    // Add pulse animation CSS chỉ cho dashboard
    const style = document.createElement('style');
    style.textContent = `
        .dashboard-pulse-animation {
            animation: dashboardPulse 1s ease-in-out;
        }
        
        @keyframes dashboardPulse {
            0% { transform: scale(1); }
            50% { transform: scale(1.1); }
            100% { transform: scale(1); }
        }
    `;
    document.head.appendChild(style);
});

// Utility functions chỉ cho dashboard
const DashboardUtils = {
    // Format numbers với style dashboard
    formatDashboardNumber: function(num) {
        return new Intl.NumberFormat('vi-VN').format(num);
    },
    
    // Show notification với style dashboard
    showDashboardNotification: function(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} alert-dismissible fade show`;
        notification.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        // Thêm notification vào dashboard container nếu có
        const dashboardContainer = document.querySelector('.dashboard-container');
        if (dashboardContainer) {
            dashboardContainer.prepend(notification);
        } else {
            document.body.prepend(notification);
        }
        
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 5000);
    }
};

// Make DashboardUtils available globally
window.DashboardUtils = DashboardUtils;