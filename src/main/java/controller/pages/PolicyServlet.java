package controller.pages;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
        "/privacy-policy",
        "/terms",
        "/shipping-policy",
        "/return-policy",
        "/buying-guide",
        "/support"
})
public class PolicyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        PolicyContent content = contentFor(path);
        request.setAttribute("policyTitle", content.title());
        request.setAttribute("policyLead", content.lead());
        request.setAttribute("policySections", content.sections());
        request.getRequestDispatcher("/pages/main/policy.jsp").forward(request, response);
    }

    private PolicyContent contentFor(String path) {
        switch (path) {
            case "/privacy-policy":
                return new PolicyContent(
                        "Chính sách bảo mật",
                        "PetShop chỉ thu thập thông tin cần thiết để xử lý tài khoản, giao hàng và hỗ trợ sau mua.",
                        List.of(
                                "Thông tin lưu trữ: họ tên, email, số điện thoại, địa chỉ giao hàng, lịch sử đơn hàng và trạng thái thanh toán.",
                                "Mật khẩu được lưu bằng BCrypt; mã OTP, token xác thực và token quên mật khẩu có thời hạn sử dụng.",
                                "Thông tin cá nhân không được bán hoặc chia sẻ cho bên thứ ba ngoài mục đích giao hàng, thanh toán và hỗ trợ khách hàng.",
                                "Người dùng có thể cập nhật thông tin cá nhân và địa chỉ trong trang Tài khoản của tôi."
                        )
                );
            case "/terms":
                return new PolicyContent(
                        "Điều khoản sử dụng",
                        "Khi sử dụng PetShop, khách hàng đồng ý cung cấp thông tin chính xác và tuân thủ quy trình mua hàng.",
                        List.of(
                                "Không sử dụng hệ thống để spam đơn hàng, spam đánh giá hoặc thử truy cập trái phép khu vực quản trị.",
                                "Đơn hàng có thể bị hủy nếu thông tin giao hàng không hợp lệ, sản phẩm hết hàng hoặc thanh toán không được xác nhận.",
                                "Giá, khuyến mãi và tồn kho có thể thay đổi; hệ thống sẽ kiểm tra lại ở bước checkout trước khi tạo đơn.",
                                "Tài khoản vi phạm có thể bị khóa tạm thời hoặc vô hiệu hóa để bảo vệ hệ thống."
                        )
                );
            case "/shipping-policy":
                return new PolicyContent(
                        "Chính sách vận chuyển",
                        "PetShop hỗ trợ giao hàng toàn quốc và tính phí dựa trên địa chỉ, trọng lượng và giá trị đơn hàng.",
                        List.of(
                                "Đơn từ 500.000đ có thể được miễn phí vận chuyển theo cấu hình hiện tại.",
                                "Nếu dịch vụ tính phí vận chuyển tạm thời gián đoạn, hệ thống sẽ dùng mức phí dự phòng và hiển thị cảnh báo ở checkout.",
                                "Khách hàng cần kiểm tra kỹ số điện thoại và địa chỉ trước khi đặt hàng.",
                                "Trạng thái đơn hàng có thể theo dõi trong mục Đơn hàng của tôi."
                        )
                );
            case "/return-policy":
                return new PolicyContent(
                        "Chính sách đổi trả",
                        "PetShop hỗ trợ đổi trả khi sản phẩm giao sai, lỗi, hư hỏng hoặc không đúng mô tả.",
                        List.of(
                                "Yêu cầu đổi trả nên được gửi trong vòng 48 giờ sau khi nhận hàng.",
                                "Sản phẩm cần còn bao bì, hóa đơn hoặc thông tin đơn hàng để đối chiếu.",
                                "Sản phẩm thức ăn, pate, sữa hoặc hàng có hạn dùng chỉ đổi trả khi lỗi phát sinh từ shop hoặc vận chuyển.",
                                "Đơn đã thanh toán chuyển khoản sẽ được admin đối soát trước khi xử lý hoàn tiền."
                        )
                );
            case "/buying-guide":
                return new PolicyContent(
                        "Hướng dẫn mua hàng",
                        "Quy trình mua hàng gồm chọn sản phẩm, kiểm tra giỏ, nhập địa chỉ, chọn thanh toán và theo dõi đơn.",
                        List.of(
                                "Thêm sản phẩm vào giỏ và kiểm tra lại số lượng trước khi thanh toán.",
                                "Cập nhật họ tên, số điện thoại và địa chỉ giao hàng mặc định.",
                                "Chọn COD, MoMo demo hoặc chuyển khoản ngân hàng theo QR được tạo sau khi đặt đơn.",
                                "Theo dõi trạng thái xử lý, giao hàng và thanh toán trong Đơn hàng của tôi."
                        )
                );
            case "/support":
            default:
                return new PolicyContent(
                        "Tư vấn khách hàng",
                        "PetShop hỗ trợ khách hàng chọn sản phẩm phù hợp cho chó, mèo và nhu cầu chăm sóc hằng ngày.",
                        List.of(
                                "Hotline: 1900 123 456.",
                                "Email: support@petshop.vn.",
                                "Giờ hỗ trợ: 8:00 - 21:00 tất cả các ngày trong tuần.",
                                "Khi cần hỗ trợ đơn hàng, vui lòng cung cấp mã đơn và số điện thoại nhận hàng."
                        )
                );
        }
    }

    private record PolicyContent(String title, String lead, List<String> sections) {}
}
