document.addEventListener("DOMContentLoaded", function () {
    const favoriteBtn = document.querySelector(".btn-outline-danger");
    if (!favoriteBtn) return; // Nếu không có button thì out

    const productId = document.querySelector("input[name='productID']")?.value;
    if (!productId) return;

    let favorites = JSON.parse(localStorage.getItem("favorites")) || [];
    const icon = favoriteBtn.querySelector("i");

    // --- Cập nhật icon tim khi load trang ---
    function updateIcon() {
        if (favorites.includes(productId)) {
            icon.classList.remove("fa-regular");
            icon.classList.add("fa-solid", "text-danger");
        } else {
            icon.classList.remove("fa-solid", "text-danger");
            icon.classList.add("fa-regular");
        }
    }

    updateIcon();

    // --- Click toggle favorite ---
    favoriteBtn.addEventListener("click", () => {
        const index = favorites.indexOf(productId);
        if (index >= 0) {
            favorites.splice(index, 1);
            showToast("Đã xóa khỏi yêu thích 💔");
        } else {
            favorites.push(productId);
            showToast("Đã thêm vào yêu thích ❤️");
        }

        localStorage.setItem("favorites", JSON.stringify(favorites));
        updateIcon();

        // --- Sync với tab/list khác ---
        window.dispatchEvent(new CustomEvent("favoritesChanged", { detail: { favorites } }));

        // --- Nếu referrer là favorite list page, redirect với ids mới ---
        const referrer = document.referrer;
        if (referrer.includes("/products") && referrer.includes("sort=favorite")) {
            const ids = favorites.join(",");
            // Redirect để server render lại favorite list đúng
            window.location.href = `/products?page=0&sort=favorite&category=&ids=${ids}`;
        }
    });

    // --- Lắng nghe event sync từ list page hoặc tab khác ---
    window.addEventListener("favoritesChanged", (e) => {
        favorites = e.detail.favorites;
        updateIcon();
    });

    // --- Sync giữa các tab bằng storage event ---
    window.addEventListener("storage", (e) => {
        if (e.key === "favorites") {
            favorites = JSON.parse(e.newValue) || [];
            updateIcon();
        }
    });

    // --- Pageshow để khi back từ list page vẫn update icon ---
    window.addEventListener("pageshow", () => {
        favorites = JSON.parse(localStorage.getItem("favorites")) || [];
        updateIcon();
    });

    // --- Toast ---
    function showToast(message) {
        const toast = document.createElement("div");
        toast.textContent = message;
        toast.style.position = "fixed";
        toast.style.bottom = "20px";
        toast.style.right = "20px";
        toast.style.background = "#333";
        toast.style.color = "#fff";
        toast.style.padding = "10px 20px";
        toast.style.borderRadius = "20px";
        toast.style.boxShadow = "0 2px 10px rgba(0,0,0,0.3)";
        toast.style.opacity = "0";
        toast.style.transition = "opacity 0.3s ease";
        toast.style.zIndex = "9999";
        document.body.appendChild(toast);
        setTimeout(() => (toast.style.opacity = "1"), 50);
        setTimeout(() => {
            toast.style.opacity = "0";
            setTimeout(() => toast.remove(), 400);
        }, 1800);
    }
});
