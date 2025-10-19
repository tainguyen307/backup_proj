(function() {
	const group = document.getElementById('otpGroup');
	const inputs = Array.from(group.querySelectorAll('.otp-input'));
	const hidden = document.getElementById('otpHidden');
	const submitBtn = document.getElementById('submitBtn');
	const resendBtn = document.getElementById('resendBtn');
	const countdownEl = document.getElementById('countdown');

	// ---- Helpers
	const codeString = () => inputs.map(i => i.value.trim()).join('');
	const updateHiddenAndButton = () => {
		const code = codeString();
		hidden.value = code;
		const ok = /^\d{6}$/.test(code);
		submitBtn.disabled = !ok;
		return ok;
	};
	const moveFocus = (i, dir) => { const n = inputs[i + dir]; if (n) n.focus(); };

	// ---- Behavior cho từng ô
	inputs.forEach((input, idx) => {
		// Chặn nhập chữ: chỉ cho 0-9
		input.addEventListener('keypress', (e) => {
			if (!/[0-9]/.test(e.key)) e.preventDefault();
		});

		// Khi thay đổi giá trị
		input.addEventListener('input', (e) => {
			// Lọc ký tự không phải số (phòng trường hợp paste vào 1 ô)
			const v = e.target.value.replace(/\D/g, '');
			e.target.value = v.slice(-1);           // chỉ 1 số
			e.target.classList.toggle('filled', e.target.value !== '');

			// tự nhảy qua phải
			if (e.target.value && idx < inputs.length - 1) moveFocus(idx, +1);

			// auto submit (tuỳ chọn)
			if (updateHiddenAndButton()) {
				// document.getElementById('otpForm').submit(); // nếu muốn tự submit
			}
		});

		// Điều hướng mũi tên + backspace lùi
		input.addEventListener('keydown', (e) => {
			if (e.key === 'Backspace' && !e.target.value && idx > 0) {
				moveFocus(idx, -1);
			} else if (e.key === 'ArrowLeft') {
				e.preventDefault(); moveFocus(idx, -1);
			} else if (e.key === 'ArrowRight') {
				e.preventDefault(); moveFocus(idx, +1);
			}
		});

		// Chọn sẵn nội dung khi focus (dễ sửa)
		input.addEventListener('focus', (e) => e.target.select());

		// Dán 6 số vào bất kỳ ô nào
		input.addEventListener('paste', (e) => {
			e.preventDefault();
			const text = (e.clipboardData || window.clipboardData).getData('text') || '';
			const digits = (text.match(/\d/g) || []).slice(0, inputs.length);
			inputs.forEach((inp, i) => {
				inp.value = digits[i] || '';
				inp.classList.toggle('filled', inp.value !== '');
			});
			updateHiddenAndButton();
			const nextEmpty = inputs.findIndex(inp => !inp.value);
			(nextEmpty === -1 ? inputs[inputs.length - 1] : inputs[nextEmpty]).focus();
		});
	});

	// Autofocus ô đầu tiên
	inputs[0].focus();

	// ---- Resend countdown + POST kèm CSRF
	if (resendBtn && countdownEl) {
		const startSeconds = Number(resendBtn.dataset.remaining || 60);
		let left = isFinite(startSeconds) && startSeconds > 0 ? startSeconds : 60;

		const setResendState = (enabled) => {
			resendBtn.disabled = !enabled;
			if (enabled) {
				resendBtn.textContent = 'Gửi lại';
			} else {
				resendBtn.innerHTML = `Gửi lại (<span id="countdown">${left}</span>s)`;
			}
		};

		setResendState(false);
		const timer = setInterval(() => {
			left -= 1;
			const counter = document.getElementById('countdown');
			if (counter) counter.textContent = left;
			if (left <= 0) { clearInterval(timer); setResendState(true); }
		}, 1000);

		resendBtn.addEventListener('click', () => {
			if (resendBtn.disabled) return;
			// Tạo form POST kèm CSRF
			const token = document.querySelector('meta[name="_csrf"]')?.content;
			const headerName = document.querySelector('meta[name="_csrf_header"]')?.content;

			const form = document.createElement('form');
			form.method = 'post';
			form.action = '/auth/resend-otp';

			if (token) {
				const input = document.createElement('input');
				input.type = 'hidden';
				input.name = '_csrf';
				input.value = token;
				form.appendChild(input);
			}

			document.body.appendChild(form);
			form.submit();
		});
	}

	// ---- Submit: chắc chắn đã gom 6 số
	document.getElementById('otpForm').addEventListener('submit', (e) => {
		if (!updateHiddenAndButton()) e.preventDefault();
	});
})();
