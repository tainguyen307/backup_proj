document.addEventListener("DOMContentLoaded", function () {
    const quill = new Quill("#editor", {
        theme: "snow",
        placeholder: "Nhập nội dung bài viết...",
        modules: {
            toolbar: [
                [{ header: [1, 2, false] }],
                ["bold", "italic", "underline"],
                ["link"],
                [{ list: "ordered" }, { list: "bullet" }],
                ["clean"]
            ]
        }
    });

    const hiddenInput = document.querySelector("#content");

    // Load nội dung cũ (edit post)
    if (hiddenInput.value?.trim()) {
        quill.root.innerHTML = hiddenInput.value;
    }

    const form = document.querySelector("form");
    form.addEventListener("submit", function (e) {
        const html = quill.root.innerHTML.trim();

        // Quill để trống = "<p><br></p>"
        if (html === "" || html === "<p><br></p>") {
            e.preventDefault();
            alert("Nội dung không được để trống.");
            return;
        }

        hiddenInput.value = html;
    });

    // Ngăn paste/drop file (nếu bạn muốn cấm)
    quill.root.addEventListener("paste", e => {
        if (e.clipboardData.files.length > 0) e.preventDefault();
    });

    quill.root.addEventListener("drop", e => {
        if (e.dataTransfer.files.length > 0) e.preventDefault();
    });
});