package com.example.demo.controller;

import com.example.demo.Model.*;
import com.example.demo.Repository.CartRepository;
import com.example.demo.Repository.CheckoutRepository;
import com.example.demo.Repository.OrderDetailRepository;
import com.example.demo.Service.AddressService;
import com.example.demo.Service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@Controller
public class CheckoutController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private com.example.demo.Repository.ProductRepository productRepository;

    @Autowired
    private SettingService settingService;

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/LoginUser";
        }

        Cart cart = cartRepository.findByUserId(user.getId());
        if (cart == null || cart.getOrderDetails() == null || cart.getOrderDetails().isEmpty()) {
            return "redirect:/giohang";
        }

        // Tính tổng tiền
        double totalPrice = 0;
        for (OrderDetail orderDetail : cart.getOrderDetails()) {
            totalPrice += orderDetail.getTotalPrice();
        }

        // Lấy địa chỉ mặc định của người dùng
        Address defaultAddress = null;
        List<Address> addresses = addressService.findAllAddressesByUser(user);
        for (Address address : addresses) {
            if (address.isDefault()) {
                defaultAddress = address;
                break;
            }
        }

        model.addAttribute("cart", cart);
        model.addAttribute("orderDetails", cart.getOrderDetails());
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("user", user);
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("addresses", addresses);
        model.addAttribute("orderCode", generateOrderCode());
        model.addAttribute("paymentMethod", PaymentMethod.COD);

        return "checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam String fullName,
                             @RequestParam String phone,
                             @RequestParam String email,
                             @RequestParam String address,
                             @RequestParam String note,
                             @RequestParam String paymentMethod,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/LoginUser";
        }

        Cart cart = cartRepository.findByUserId(user.getId());
        if (cart == null || cart.getOrderDetails() == null || cart.getOrderDetails().isEmpty()) {
            return "redirect:/giohang";
        }

        // Stock validation: ensure each product has enough stock for requested quantity
        for (OrderDetail od : cart.getOrderDetails()) {
            if (od.getProducts() != null) {
                for (com.example.demo.Model.Product p : od.getProducts()) {
                    // reload fresh product from DB
                    Optional<com.example.demo.Model.Product> prodOpt = productRepository.findById(p.getProductID());
                    if (!prodOpt.isPresent()) {
                        redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại: " + p.getProductName());
                        return "redirect:/giohang";
                    }
                    com.example.demo.Model.Product prod = prodOpt.get();
                    if (prod.getStock() < od.getQuantity()) {
                        redirectAttributes.addFlashAttribute("error", "Sản phẩm '" + prod.getProductName() + "' chỉ còn " + prod.getStock() + " cái. Vui lòng điều chỉnh số lượng.");
                        return "redirect:/giohang";
                    }
                }
            }
        }

        // Tính tổng tiền
        double totalPrice = 0;
        for (OrderDetail orderDetail : cart.getOrderDetails()) {
            totalPrice += orderDetail.getTotalPrice();
        }

        // Tạo mã đơn hàng
        String orderCode = generateOrderCode();

        // Tạo đơn hàng mới
        Checkout checkout = new Checkout();
        checkout.setCustomerName(fullName);
        checkout.setPhoneNumber(phone);
        checkout.setEmail(email);
        checkout.setDeliveryAddress(address);
        checkout.setNotes(note);
        checkout.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
        checkout.setProductPrice(totalPrice);
        checkout.setShippingFee(0); // Miễn phí vận chuyển
        checkout.setOrderTotal(totalPrice);
        checkout.setUser(user);

        // Thêm thông tin mới
        checkout.setOrderCode(orderCode);
        checkout.setOrderDate(new Date());
        checkout.setOrderStatus("Đang xử lý");

        // Xác định trạng thái thanh toán dựa trên phương thức thanh toán
        if (paymentMethod.equals("COD")) {
            checkout.setPaymentStatus("Chưa thanh toán");

            // Lưu đơn hàng và xử lý OrderDetail
            saveOrderAndDetails(checkout, cart);

            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công! Cảm ơn bạn đã mua sắm.");
            redirectAttributes.addFlashAttribute("orderCode", orderCode);
            redirectAttributes.addFlashAttribute("paymentMethod", paymentMethod);
            redirectAttributes.addFlashAttribute("paymentStatus", checkout.getPaymentStatus());

            return "redirect:/order-success";
        } else {
            // Đối với các phương thức thanh toán trực tuyến
            checkout.setPaymentStatus("Chờ thanh toán");

            // Lưu đơn hàng và xử lý OrderDetail
            saveOrderAndDetails(checkout, cart);

            // Chuyển hướng đến trang thanh toán
            return "redirect:/payment/" + orderCode;
        }
    }

    // Tách phần lưu đơn hàng và xử lý OrderDetail thành phương thức riêng
    private void saveOrderAndDetails(Checkout checkout, Cart cart) {
        // Lưu đơn hàng
        checkoutRepository.save(checkout);

        // Chuyển OrderDetail từ Cart sang Checkout
        List<OrderDetail> orderDetails = new ArrayList<>(cart.getOrderDetails());

        // Xóa OrderDetail khỏi Cart trước
        cart.getOrderDetails().clear();
        cartRepository.save(cart);

        // Gán OrderDetail cho Checkout và lưu lại
        for (OrderDetail detail : orderDetails) {
            detail.setCart(null);
            detail.setCheckout(checkout);

            // Lưu tên sản phẩm vào OrderDetail
            if (detail.getProduct() != null && !detail.getProduct().isEmpty()) {
                detail.setProductName(detail.getProduct().get(0).getProductName());
            }

            // Decrement stock for each product in detail
            if (detail.getProduct() != null) {
                for (com.example.demo.Model.Product prod : detail.getProduct()) {
                    Optional<com.example.demo.Model.Product> prodOpt = productRepository.findById(prod.getProductID());
                    if (prodOpt.isPresent()) {
                        com.example.demo.Model.Product realProd = prodOpt.get();
                        int newStock = realProd.getStock() - detail.getQuantity();
                        if (newStock < 0) newStock = 0; // just in case
                        realProd.setStock(newStock);
                        productRepository.save(realProd);
                    }
                }
            }

            orderDetailRepository.save(detail);
        }

        // Thêm OrderDetail vào Checkout
        checkout.setOrderDetails(orderDetails);
        checkoutRepository.save(checkout);
    }

    @GetMapping("/order-success")
    public String orderSuccess(Model model, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công! Cảm ơn bạn đã mua sắm.");
        return "order-success";
    }

    private String generateOrderCode() {
        return "DMD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @GetMapping("/payment/{orderCode}")
    public String showPaymentPage(@PathVariable("orderCode") String orderCode, Model model) {
        Checkout checkout = checkoutRepository.findByOrderCode(orderCode);
        if (checkout == null) {
            return "redirect:/";
        }

        model.addAttribute("checkout", checkout);

        String paymentQrCode = getSettingOrDefault(
                "paymentQrCodeUrl",
                "https://upload.wikimedia.org/wikipedia/commons/d/d0/QR_code_for_mobile_English_Wikipedia.svg"
        );

        // Thêm thông tin thanh toán tùy theo phương thức
        if (checkout.getPaymentMethod() == PaymentMethod.BankTransfer) {
            model.addAttribute("bankInfo", getBankInfo(orderCode, paymentQrCode));
        } else if (checkout.getPaymentMethod() == PaymentMethod.MoMo) {
            model.addAttribute("momoQR", paymentQrCode);
        } else if (checkout.getPaymentMethod() == PaymentMethod.VnPay) {
            model.addAttribute("vnpayQR", paymentQrCode);
        }

        return "payment";
    }

    // Phương thức hỗ trợ
    private Map<String, String> getBankInfo(String orderCode, String paymentQrCode) {
        Map<String, String> bankInfo = new HashMap<>();
        bankInfo.put("bankName", getSettingOrDefault("paymentBankName", "Vietcombank"));
        bankInfo.put("accountNumber", getSettingOrDefault("paymentAccountNumber", "1234567890"));
        bankInfo.put("accountName", getSettingOrDefault("paymentAccountName", "CÔNG TY TNHH DMD"));
        bankInfo.put("transferContent", orderCode);
        bankInfo.put("qrCode", paymentQrCode);
        return bankInfo;
    }

    private String getSettingOrDefault(String key, String defaultValue) {
        String value = settingService.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    @PostMapping("/confirm-payment/{orderCode}")
    public String confirmPayment(@PathVariable("orderCode") String orderCode,
                                 @RequestParam(required = false) String transactionId,
                                 RedirectAttributes redirectAttributes) {
        Checkout checkout = checkoutRepository.findByOrderCode(orderCode);
        if (checkout == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
            return "redirect:/";
        }

        // Cập nhật trạng thái thanh toán
        checkout.setPaymentStatus("Đã thanh toán");
        checkout.setPaymentDate(new Date());

        // Lưu thông tin giao dịch nếu có
        if (transactionId != null && !transactionId.isEmpty()) {
            checkout.setNotes(checkout.getNotes() + " | Mã giao dịch: " + transactionId);
        }

        checkoutRepository.save(checkout);

        redirectAttributes.addFlashAttribute("success", "Thanh toán thành công! Cảm ơn bạn đã mua sắm.");
        redirectAttributes.addFlashAttribute("orderCode", orderCode);
        redirectAttributes.addFlashAttribute("paymentMethod", checkout.getPaymentMethod().toString());
        redirectAttributes.addFlashAttribute("paymentStatus", checkout.getPaymentStatus());

        return "redirect:/order-success";
    }

    @GetMapping("/cancel-payment/{orderCode}")
    public String cancelPayment(@PathVariable("orderCode") String orderCode,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        // Lấy thông tin người dùng từ session
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/LoginUser";
        }

        // Tìm đơn hàng theo mã
        Checkout checkout = checkoutRepository.findByOrderCode(orderCode);
        if (checkout == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
            return "redirect:/giohang";
        }

        // Kiểm tra xem đơn hàng có thuộc về người dùng hiện tại không
        if (checkout.getUser().getId() != user.getId()) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy đơn hàng này!");
            return "redirect:/giohang";
        }

        // Tìm hoặc tạo giỏ hàng cho người dùng
        Cart cart = cartRepository.findByUserId(user.getId());
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }

        // Chuyển các sản phẩm từ đơn hàng về giỏ hàng
        List<OrderDetail> orderDetails = checkout.getOrderDetails();
        for (OrderDetail detail : orderDetails) {
            // Restore stock for each product in this order detail
            if (detail.getProduct() != null) {
                for (com.example.demo.Model.Product prod : detail.getProduct()) {
                    Optional<com.example.demo.Model.Product> prodOpt = productRepository.findById(prod.getProductID());
                    if (prodOpt.isPresent()) {
                        com.example.demo.Model.Product realProd = prodOpt.get();
                        realProd.setStock(realProd.getStock() + detail.getQuantity());
                        productRepository.save(realProd);
                    }
                }
            }

            detail.setCheckout(null);
            detail.setCart(cart);
            orderDetailRepository.save(detail);
        }

        // Cập nhật giỏ hàng
        cart.getOrderDetails().addAll(orderDetails);
        cartRepository.save(cart);

        // Xóa đơn hàng
        checkout.setOrderDetails(new ArrayList<>());
        checkoutRepository.save(checkout);
        checkoutRepository.delete(checkout);

        redirectAttributes.addFlashAttribute("info", "Đơn hàng đã được hủy và sản phẩm đã được đưa trở lại giỏ hàng của bạn.");
        return "redirect:/giohang";
    }
}