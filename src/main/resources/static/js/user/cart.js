document.addEventListener('DOMContentLoaded', function() {
    // Xử lý nút tăng giảm
	
    document.querySelectorAll('.btn-decrease').forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.closest('.input-group').querySelector('input[name="quantity"]');
            let value = parseInt(input.value) || 1;
            if (value > 1) input.value = value - 1;
        });
    });

    document.querySelectorAll('.btn-increase').forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.closest('.input-group').querySelector('input[name="quantity"]');
            let value = parseInt(input.value) || 1;
            input.value = value + 1;
        });
    });
});
