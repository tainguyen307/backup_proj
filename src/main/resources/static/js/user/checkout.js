document.addEventListener("DOMContentLoaded", () => {
  const select = document.getElementById("selectedAddress");

  const fields = {
    fullname: document.getElementById("displayFullname"),
    phone: document.getElementById("displayPhone"),
    street: document.getElementById("displayStreet"),
    ward: document.getElementById("displayWard"),
    district: document.getElementById("displayDistrict"),
    city: document.getElementById("displayCity"),
  };

  const hiddenAddressId = document.getElementById("selectedAddressId");
  const hiddenPayment = document.getElementById("selectedPayment");
  const checkoutForm = document.getElementById("checkoutForm");

  // Hàm hiển thị thông tin địa chỉ
  function showAddressInfo(option) {
    fields.fullname.textContent = option.dataset.fullname || "—";
    fields.phone.textContent = option.dataset.phone || "—";
    fields.street.textContent = option.dataset.street || "—";
    fields.ward.textContent = option.dataset.ward || "—";
    fields.district.textContent = option.dataset.district || "—";
    fields.city.textContent = option.dataset.city || "—";
  }

  // ✅ Khi chọn thay đổi địa chỉ trong dropdown
  select?.addEventListener("change", (e) => {
    const option = e.target.selectedOptions[0];
    if (option && option.value) {
      showAddressInfo(option);
      hiddenAddressId.value = option.value; // Gán addressID mỗi khi chọn
    }
  });

  // Khi load trang: hiển thị địa chỉ mặc định và gán ID vào form
  const selectedOption =
    select?.options[select.selectedIndex] || select?.querySelector("option[value]");
  if (selectedOption && selectedOption.value) {
    showAddressInfo(selectedOption);
    hiddenAddressId.value = selectedOption.value;
  }

  // Khi người dùng chọn phương thức thanh toán
  const paymentRadios = document.querySelectorAll('input[name="payment_method"]');
  paymentRadios.forEach((radio) => {
    radio.addEventListener("change", () => {
      if (radio.checked) {
        hiddenPayment.value = radio.value;
      }
    });
  });

  // Khi load trang: gán phương thức mặc định (radio đầu tiên)
  const defaultChecked = document.querySelector(
    'input[name="payment_method"]:checked'
  );
  if (defaultChecked) {
    hiddenPayment.value = defaultChecked.value;
  }

  // Khi submit form: đảm bảo giá trị được gửi
  checkoutForm?.addEventListener("submit", () => {
    if (select && hiddenAddressId) hiddenAddressId.value = select.value;
    const checkedPayment = document.querySelector(
      'input[name="payment_method"]:checked'
    );
    if (checkedPayment && hiddenPayment) hiddenPayment.value = checkedPayment.value;
  });
});
