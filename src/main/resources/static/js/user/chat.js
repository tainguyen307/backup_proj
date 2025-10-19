function startChat(btn) {
    const vendorId = btn.dataset.vendorId;
    const currentUserId = document.getElementById('currentUserId')?.value;
	
    if (!currentUserId || currentUserId.trim() === "anonymousUser") {
        window.location.href = `/auth/login`;
        return;
    }

    window.location.href = `/user/chat?vendorId=${vendorId}`;
}