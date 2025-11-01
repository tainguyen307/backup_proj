document.addEventListener("DOMContentLoaded", function () {
    const editorEl = document.querySelector("#editor");
    const uploadBtn = document.getElementById("uploadImageBtn");
    const commentInput = document.getElementById("commentInput");

    if (!editorEl || typeof Quill === "undefined") {
        console.error("Quill chưa được load hoặc #editor không tồn tại!");
        return;
    }

    // --- Custom Blot cho hình ---
    const BlockEmbed = Quill.import('blots/block/embed');

    class ReviewImageBlot extends BlockEmbed {
        static create(src) {
            const node = super.create();
            const img = document.createElement('img');
            img.src = src;
            img.style.cssText = `
                width:120px; height:120px; object-fit:cover; border-radius:8px;
                margin-bottom:8px; margin-left:8px; cursor:pointer;
            `;
            img.addEventListener('click', () => {
                const quill = Quill.find(editorEl);
                const blot = Quill.find(node);
                const index = quill.getIndex(blot);
                quill.deleteText(index, 1, Quill.sources.USER);
                updateHiddenInput();
            });
            node.appendChild(img);
            node.setAttribute('contenteditable', 'false');
            node.style.display = 'inline-block';
            return node;
        }
        static value(node) {
            const img = node.querySelector('img');
            return img ? img.src : '';
        }
    }

    ReviewImageBlot.blotName = 'reviewImage';
    ReviewImageBlot.tagName = 'div';
    ReviewImageBlot.className = 'review-image-block';
    Quill.register(ReviewImageBlot);

    // --- Khởi tạo Quill ---
    const quill = new Quill(editorEl, {
        theme: 'snow',
        placeholder: 'Viết cảm nhận của bạn (tối thiểu 50 ký tự)...',
        modules: { toolbar: false }
    });

    function updateHiddenInput() {
        if (commentInput) commentInput.value = quill.root.innerHTML.trim();
    }
    quill.on('text-change', updateHiddenInput);

    // --- Upload hình ---
    if (uploadBtn) {
        uploadBtn.addEventListener('click', () => {
            const input = document.createElement('input');
            input.type = 'file';
            input.accept = 'image/*';
            input.click();

            input.onchange = async () => {
                const file = input.files[0];
                if (!file) return;

                const formData = new FormData();
                formData.append("file", file);

                try {
                    const res = await fetch("/reviews/upload-image", {
                        method: "POST",
                        body: formData
                    });
                    const data = await res.json();
                    if (data.success) addImageToEditor(data.url);
                    else alert("Upload hình thất bại: " + data.error);
                } catch (err) {
                    console.error(err);
                    alert("Lỗi upload hình");
                }
            };
        });
    }

    function addImageToEditor(src) {
        quill.insertEmbed(quill.getLength(), 'reviewImage', src, Quill.sources.USER);
        quill.setSelection(quill.getLength(), quill.getLength(), Quill.sources.SILENT);
        updateHiddenInput();
    }

    // --- Chặn Backspace/Delete nhầm và Ctrl+A ---
    quill.root.addEventListener('keydown', e => {
        const range = quill.getSelection();
        if (!range) return;
        const [blotBefore] = quill.scroll.descendant(ReviewImageBlot, range.index - 1);
        const [blotHere] = quill.scroll.descendant(ReviewImageBlot, range.index);
        if ((blotBefore || blotHere) && (e.key === 'Backspace' || e.key === 'Delete')) {
            e.preventDefault();
            quill.setSelection(quill.getLength(), quill.getLength(), Quill.sources.SILENT);
        }

        if (e.ctrlKey && e.key.toLowerCase() === 'a') {
            e.preventDefault();
            let start = 0, end = quill.getLength();
            const [firstBlot] = quill.scroll.descendant(ReviewImageBlot, 0);
            if (firstBlot) start = quill.getIndex(firstBlot) + 1;
            const [lastBlot] = quill.scroll.descendant(ReviewImageBlot, end - 1);
            if (lastBlot) end = quill.getIndex(lastBlot);
            quill.setSelection(start, end - start, Quill.sources.SILENT);
        }
    });

    quill.on('selection-change', range => {
        if (!range) return;
        const [blot] = quill.scroll.descendant(ReviewImageBlot, range.index);
        if (blot) quill.setSelection(quill.getLength(), quill.getLength(), Quill.sources.SILENT);
    });

    // --- Prefill form khi sửa review ---
	window.prefillUpdateForm = function(reviewID, rating, commentHTML) {
	    // Prefill rating
	    for (let i = 1; i <= 5; i++) {
	        const star = document.getElementById('star' + i);
	        if (star) star.checked = (i === rating);
	    }

	    // Xóa nội dung hiện tại
	    quill.setContents([]);

	    // Paste HTML nguyên trạng
	    quill.clipboard.dangerouslyPasteHTML(commentHTML || '');

	    // Scan blot hình để thêm sự kiện xóa
	    quill.scroll.descendants(ReviewImageBlot).forEach(blot => {
	        const img = blot.domNode.querySelector('img');
	        if (img && !img._listenerAdded) {
	            img.addEventListener('click', () => {
	                const index = quill.getIndex(blot);
	                quill.deleteText(index, 1, Quill.sources.USER);
	                updateHiddenInput();
	            });
	            img._listenerAdded = true;
	        }
	    });

	    // Update hidden input
	    updateHiddenInput();

	    // Đặt con trỏ cuối
	    quill.setSelection(quill.getLength(), quill.getLength(), Quill.sources.SILENT);

	    // Đổi action form
	    const form = document.querySelector('form');
	    if (form) {
	        form.action = '/reviews/update/' + reviewID;
	        const reviewIdInput = document.getElementById('reviewID');
	        if (reviewIdInput) reviewIdInput.value = reviewID;
	        form.scrollIntoView({ behavior: 'smooth' });
	    }
    };

    // --- Event listener nút Sửa ---
    document.querySelectorAll('.btn-edit-review').forEach(btn => {
        btn.addEventListener('click', () => {
            const reviewID = btn.dataset.id;
            const rating = parseInt(btn.dataset.rating);
            const comment = btn.dataset.comment; // HTML escape
            prefillUpdateForm(reviewID, rating, comment);
        });
    });
	
	const form = document.querySelector('form');
	if (form) {
	    form.addEventListener('submit', e => {
	        let html = quill.root.innerHTML;

	        // --- Clean toàn bộ nội dung trước khi gửi ---
	        html = html
	            .replace(/<p>(\s|&nbsp;|<br>)*<\/p>/gi, "")
	            .replace(/<div>(\s|&nbsp;|<br>)*<\/div>/gi, "")
	            .replace(/<!--.*?-->/g, "")
	            .replace(/\s*(style|class)="[^"]*"/g, "")
	            .replace(/(&nbsp;|\s){2,}/g, ' ')
	            .replace(/^(<p>)?\s+/g, '$1')
	            .trim();

	        quill.root.innerHTML = html;
	        updateHiddenInput(); // cập nhật commentInput.value = HTML sạch
	    });
	}
});