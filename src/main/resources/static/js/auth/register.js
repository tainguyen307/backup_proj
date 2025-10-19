// Reusable toggle function
function setupPasswordToggle(toggleId, passwordId) {
  const toggleBtn = document.getElementById(toggleId);
  if (!toggleBtn) return;

  toggleBtn.addEventListener("click", function () {
    const password = document.getElementById(passwordId);
    const icon = this.querySelector("i");
    if (!password || !icon) return;

    if (password.type === "password") {
      password.type = "text";
      icon.classList.remove("bi-eye");
      icon.classList.add("bi-eye-slash");
    } else {
      password.type = "password";
      icon.classList.remove("bi-eye-slash");
      icon.classList.add("bi-eye");
    }
  });
}

setupPasswordToggle("togglePassword", "password");
setupPasswordToggle("toggleConfirmPassword", "confirmPassword");

// Password strength indicator
const passwordInput = document.getElementById("password");
const strengthBar = document.getElementById("passwordStrength");
if (passwordInput && strengthBar) {
  passwordInput.addEventListener("input", function () {
    const password = this.value;
    let strength = 0;
    if (password.length >= 6) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^a-zA-Z0-9]/.test(password)) strength++;

    strengthBar.className = "password-strength mt-2";
    if (strength <= 2) {
      strengthBar.classList.add("strength-weak");
    } else if (strength <= 3) {
      strengthBar.classList.add("strength-medium");
    } else {
      strengthBar.classList.add("strength-strong");
    }
  });
}

// Confirm password validation
function validatePasswordMatch() {
  const pw = document.getElementById("password")?.value || "";
  const cpw = document.getElementById("confirmPassword")?.value || "";
  const matchDiv = document.getElementById("passwordMatch");
  const confirmInput = document.getElementById("confirmPassword");
  if (!matchDiv || !confirmInput) return true;

  if (cpw && pw !== cpw) {
    matchDiv.textContent = "Mật khẩu xác nhận không khớp";
    confirmInput.classList.add("is-invalid");
    return false;
  } else {
    matchDiv.textContent = "";
    confirmInput.classList.remove("is-invalid");
    return true;
  }
}

document.getElementById("confirmPassword")?.addEventListener("input", validatePasswordMatch);
document.getElementById("password")?.addEventListener("input", validatePasswordMatch);

// Form submit handling
document.getElementById("registerForm")?.addEventListener("submit", function (e) {
  if (!validatePasswordMatch()) {
    e.preventDefault();
    return false;
  }
  const submitBtn = document.getElementById("submitBtn");
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="bi bi-hourglass-split me-2"></i>Đang xử lý...';
  }
});

// Auto dismiss alerts
setTimeout(function () {
  document.querySelectorAll(".alert").forEach((a) => a.remove());
}, 5000);
