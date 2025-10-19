document.addEventListener('DOMContentLoaded', function() {
    console.log('Inventory page loaded');
    
    // Initialize modal functionality
    initRestockModal();
    
    // Add smooth animations to table rows
    initTableAnimations();
});

function initRestockModal() {
    console.log('Initializing restock modal...');
    
    const restockButtons = document.querySelectorAll('.restock-btn');
    const restockModal = document.getElementById('restockModal');
    const restockForm = document.getElementById('restockForm');
    const restockModalBody = document.getElementById('restockModalBody');
    
    console.log('Found buttons:', restockButtons.length);
    console.log('Found modal:', restockModal);
    
    let currentInventoryId = null;
    
    // Initialize Bootstrap modal
    const bsModal = new bootstrap.Modal(restockModal);
    
    // Add click event to all restock buttons
    restockButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('Restock button clicked');
            
            // Get data from button attributes
            currentInventoryId = this.getAttribute('data-inventory-id');
            const productName = this.getAttribute('data-product-name');
            const currentQuantity = this.getAttribute('data-current-quantity');
            
            console.log('Product:', productName, 'Current quantity:', currentQuantity, 'ID:', currentInventoryId);
            
            // Update modal content
            restockModalBody.innerHTML = `
                <div class="mb-3">
                    <label class="form-label fw-bold">Sản phẩm:</label>
                    <p class="text-primary fw-bold">${productName}</p>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-bold">Tồn kho hiện tại:</label>
                    <p class="text-primary fs-4 fw-bold">${currentQuantity}</p>
                </div>
                <div class="mb-3">
                    <label for="restockQuantity" class="form-label fw-bold">
                        Số lượng nhập *
                    </label>
                    <input type="number" 
                           class="form-control form-control-lg" 
                           id="restockQuantity" 
                           name="quantity" 
                           min="1" 
                           value="10" 
                           required
                           placeholder="Nhập số lượng">
                    <div class="form-text">Số lượng tối thiểu: 1</div>
                </div>
                <input type="hidden" name="inventoryId" value="${currentInventoryId}">
            `;
            
            // Update form action
            restockForm.action = `/admin/inventory/restock/${currentInventoryId}`;
            
            console.log('Form action updated to:', restockForm.action);
        });
    });
    
    // Handle form submission
    restockForm.addEventListener('submit', function(e) {
        e.preventDefault();
        console.log('Form submitted');
        
        const quantityInput = document.getElementById('restockQuantity');
        const quantity = parseInt(quantityInput.value);
        
        if (quantity < 1) {
            showToast('Lỗi', 'Số lượng nhập phải lớn hơn 0!', 'danger');
            return;
        }
        
        // Show loading state
        const submitBtn = restockForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';
        submitBtn.disabled = true;
        
        console.log('Submitting form with quantity:', quantity);
        
        // Submit the form after a short delay to show loading state
        setTimeout(() => {
            this.submit();
        }, 500);
    });
    
    // Reset form when modal is hidden
    restockModal.addEventListener('hidden.bs.modal', function() {
        console.log('Modal hidden');
        const submitBtn = restockForm.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-check me-2"></i>Nhập Hàng';
    });
    
    // Clean up when modal is shown
    restockModal.addEventListener('show.bs.modal', function() {
        console.log('Modal shown');
    });
}

function initTableAnimations() {
    const tableRows = document.querySelectorAll('#inventoryTable tbody tr');
    
    tableRows.forEach((row, index) => {
        // Add staggered animation
        row.style.animationDelay = `${index * 0.1}s`;
        row.classList.add('fade-in-row');
    });
}

function showToast(title, message, type = 'info') {
    // Create toast container if it doesn't exist
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }
    
    const toastId = 'toast-' + Date.now();
    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center text-bg-${type} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <strong>${title}</strong><br>${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;
    
    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement);
    toast.show();
    
    // Remove toast from DOM after it's hidden
    toastElement.addEventListener('hidden.bs.toast', function() {
        this.remove();
    });
}

// Add debug styles
const debugStyles = `
    <style>
        .fade-in-row {
            animation: fadeInUp 0.5s ease-out forwards;
            opacity: 0;
        }
        
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .fa-spinner {
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        
        /* Debug border for testing */
        .restock-btn {
            /* border: 2px solid red !important; */
        }
    </style>
`;

document.head.insertAdjacentHTML('beforeend', debugStyles);

// Debug: Log when script is fully loaded
console.log('Inventory JavaScript fully loaded');