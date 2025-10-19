document.addEventListener("DOMContentLoaded", function() {
    const cancelBtn = document.getElementById("cancelBtn");
    const confirmBtn = document.getElementById("confirmCancelBtn");

    if (cancelBtn && confirmBtn) {
        cancelBtn.addEventListener("click", () => {
            confirmBtn.classList.toggle("d-none");
        });
    }
});