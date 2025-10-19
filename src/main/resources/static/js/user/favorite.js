document.addEventListener("DOMContentLoaded", function () {
    const favoriteButtons = document.querySelectorAll(".favorite-btn");
    let favorites = JSON.parse(localStorage.getItem("favorites")) || [];
    const urlParams = new URLSearchParams(window.location.search);
    const isFavoritePage = urlParams.get("sort") === "favorite";
    const idsParam = urlParams.get("ids");

    const productGrid = document.querySelector(".row.g-4") || document.querySelector(".product-container");
    const pagination = document.querySelector(".pagination");

    // --- 0️⃣ Nếu sort=favorite mà ids null, redirect kèm ids
    if (isFavoritePage && idsParam === null) {
        if (favorites.length === 0) {
            productGrid.innerHTML = `
                <div class="text-center py-5">
                    <h5>Bạn chưa có sản phẩm yêu thích nào 😢</h5>
                    <a href="/products" class="btn btn-outline-primary mt-3">Quay lại mua sắm</a>
                </div>`;
            if (pagination) pagination.style.display = "none";
            return;
        } else {
            const newUrl = `/products?page=0&sort=favorite&category=&ids=${favorites.join(",")}`;
            window.location.href = newUrl;
            return;
        }
    }

    // --- 1️⃣ Nếu ids rỗng, hiển thị thông báo
    if (isFavoritePage && idsParam !== null && idsParam.trim() === "") {
        productGrid.innerHTML = `
            <div class="text-center py-5">
                <h5>Bạn chưa có sản phẩm yêu thích nào 😢</h5>
                <a href="/products" class="btn btn-outline-primary mt-3">Quay lại mua sắm</a>
            </div>`;
        if (pagination) pagination.style.display = "none";
        return;
    }

    // --- 2️⃣ Cập nhật icon tim
    function updateIcons() {
        favoriteButtons.forEach(btn => {
            const id = btn.dataset.id;
            const icon = btn.querySelector("i");
            if (favorites.includes(id)) {
                icon.classList.remove("fa-regular");
                icon.classList.add("fa-solid", "text-danger");
            } else {
                icon.classList.remove("fa-solid", "text-danger");
                icon.classList.add("fa-regular");
            }
        });
    }

    updateIcons();

    // --- 3️⃣ Click toggle favorite
    favoriteButtons.forEach(btn => {
        const id = btn.dataset.id;

        btn.addEventListener("click", () => {
            const index = favorites.indexOf(id);
            if (index >= 0) {
                favorites.splice(index, 1);
                showToast("Đã xóa khỏi yêu thích 💔");
            } else {
                favorites.push(id);
                showToast("Đã thêm vào yêu thích ❤️");
            }

            localStorage.setItem("favorites", JSON.stringify(favorites));
            updateIcons();

            // --- 4️⃣ Nếu đang ở favorite page, reload page với ids mới
            if (isFavoritePage) {
                const ids = favorites.join(",");
                window.location.href = `/products?page=0&sort=favorite&category=&ids=${ids}`;
            }

            // --- 5️⃣ Sync với các tab/detail khác
            window.dispatchEvent(new CustomEvent("favoritesChanged", { detail: { favorites } }));
        });
    });

    // --- 6️⃣ Sync event từ tab khác / storage
    window.addEventListener("favoritesChanged", (e) => {
        favorites = e.detail.favorites;
        updateIcons();
    });
    window.addEventListener("storage", (e) => {
        if (e.key === "favorites") {
            favorites = JSON.parse(e.newValue) || [];
            updateIcons();
        }
    });

    // --- 7️⃣ Toast
    function showToast(message) {
        let toast = document.createElement("div");
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
        setTimeout(() => toast.style.opacity = "1", 50);
        setTimeout(() => {
            toast.style.opacity = "0";
            setTimeout(() => toast.remove(), 400);
        }, 1800);
    }
});
