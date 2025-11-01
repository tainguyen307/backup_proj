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
    const existingContent = hiddenInput.value;

    // Nếu có nội dung cũ (edit post), load vào editor
    if (existingContent && existingContent.trim() !== "") {
        quill.root.innerHTML = existingContent;
    }

    // Khi submit form → copy HTML vào textarea
    const form = document.querySelector("form");
    form.addEventListener("submit", function () {
        hiddenInput.value = quill.root.innerHTML;
    });
	
	quill.root.addEventListener("paste", e => {
	    if (e.clipboardData.files.length > 0) e.preventDefault();
	});

	quill.root.addEventListener("drop", e => {
	    if (e.dataTransfer.files.length > 0) e.preventDefault();
	});
});