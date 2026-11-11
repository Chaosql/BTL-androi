package com.example.myapplication.data.remote.network;

import java.util.Locale;

public final class ErrorMessageMapper {
    private static final String[][] RULES = {
        {"Bạn đang gửi yêu cầu quá nhanh. Vui lòng chờ rồi thử lại.", "Too many requests"},
        {"Không tìm thấy tài khoản với email này.", "User not found with this username:", "User not found with username:"},
        {"Email hoặc mật khẩu không chính xác.", "Invalid credentials", "Bad credentials"},
        {"Đã có lỗi xảy ra, vui lòng thử lại sau", "Something went wrong, please try again later"},
        {"Xin lỗi, bạn cần cung cấp thông tin xác thực để truy cập tài nguyên này", "Sorry, you needs to provide authentication credentials to access this resource", "authentication credentials"},
        {"Xin lỗi, bạn không có quyền truy cập tài nguyên này", "Sorry, you do not have the necessary permissions to access this resource", "necessary permissions"},
        {"Bạn không có quyền cập nhật hoặc xóa tài nguyên này", "You do not have permission to update or delete this resource", "permission to update or delete"},
        {"Trường này không hợp lệ", "This field is invalid"},
        {"Sai định dạng", "Invalid format"},
        {"Trường này là bắt buộc", "This field is required"},
        {"Trường này không được để trống", "This field can't be blank"},
        {"Mật khẩu không đạt yêu cầu (phải bao gồm cả chữ, số và ký tự đặc biệt)", "Unsatisfactory password", "Password must include letters, numbers, and special characters"},
        {"Định dạng email không hợp lệ", "Invalid email format"},
        {"Sai định dạng ngày tháng", "Wrong or Invalid datetime picker"},
        {"Ngày giờ phải theo định dạng yyyy-MM-dd HH:mm:ss", "DateTime must be format yyyy-MM-dd HH:mm:ss"},
        {"Ngày phải lớn hơn ngày hiện tại và theo định dạng yyyy-MM-dd", "Date must format yyyy-MM-dd and greater than current date"},
        {"Ngày sinh phải là ngày trong quá khứ và theo định dạng yyyy-MM-dd", "Date of birth must be in the past and format yyyy-MM-dd"},
        {"Chỉ cho phép hình ảnh PNG, JPG, JPEG, WEBP hoặc GIF", "Only PNG, JPG, JPEG, WEBP or GIF images are allowed"},
        {"Tên đăng nhập không chính xác", "Username is incorrect"},
        {"Mật khẩu không chính xác", "Password is incorrect"},
        {"Mật khẩu cũ không chính xác hoặc mật khẩu mới không khớp", "Old password is incorrect or new passwords do not match"},
        {"Mật khẩu xác nhận không khớp!", "Password confirmation does not match!", "Password confirmation does not match"},
        {"Refresh token không hợp lệ hoặc đã hết hạn", "Invalid refresh token", "Refresh token was expired"},
        {"Email này đã được sử dụng!", "This email is already in use"},
        {"Tài khoản này đã được xác thực trước đó!", "This account has already been verified!", "This account has already been verified"},
        {"Tài khoản chưa được xác thực. Vui lòng kiểm tra email để kích hoạt!", "Account has not been verified. Please check your email to activate it!", "Account has not been verified"},
        {"Chỉ tài khoản đã kích hoạt mới có thể khôi phục mật khẩu. Vui lòng kiểm tra email xác thực trước!", "Only activated accounts can recover passwords. Please check your verification email first!", "Only activated accounts can recover passwords"},
        {"Mã OTP không chính xác hoặc đã hết hạn", "OTP code is incorrect or has expired"},
        {"Bạn đang thao tác quá nhanh, vui lòng đợi 60 giây!", "You are doing this too fast, please wait 60 seconds!", "please wait 60 seconds"},
        {"Hệ thống gửi mail đang gặp sự cố, vui lòng thử lại sau!", "The email sending system is experiencing issues, please try again later!", "email sending system is experiencing issues"},
        {"Phiên đổi mật khẩu đã hết hạn, vui lòng yêu cầu lại OTP", "Password reset session has expired, please request OTP again"},
        {"Phiên đăng ký đã hết hạn hoặc email không tồn tại. Vui lòng đăng ký lại.", "The registration session has expired or the email does not exist. Please register again.", "registration session has expired"},
        {"Lỗi hệ thống khi xử lý dữ liệu. Vui lòng thử lại sau.", "A system error occurred while processing data. Please try again later."},
        {"Token Firebase không hợp lệ hoặc đã hết hạn", "Invalid or expired Firebase token"},
        {"Đăng nhập Google thất bại. Vui lòng thử lại", "Google login failed. Please try again", "Google login failed"},
        {"Vui lòng hoàn thiện thông tin cá nhân trước khi sử dụng", "Please complete your profile before using"},
        {"Hồ sơ đã được hoàn thiện rồi", "Profile has already been completed"},
        {"Tài khoản đăng nhập bằng Google không hỗ trợ thay đổi mật khẩu", "Google login accounts do not support password changes"},
        {"Không tìm thấy cuộc trò chuyện", "Conversation not found"},
        {"Không tìm thấy tin nhắn", "Message not found"},
        {"Không tìm thấy cuộc hội thoại đích", "Target conversation not found"},
        {"Bạn không phải là thành viên của cuộc trò chuyện này", "You are not a member of this conversation"},
        {"Bạn không phải là người gửi tin nhắn này", "You are not the sender of this message"},
        {"Nhóm đã đạt giới hạn số lượng thành viên tối đa", "Group has reached the maximum number of members"},
        {"Bạn đã rời khỏi nhóm này", "You have already left this group"},
        {"Chỉ trưởng nhóm mới được thực hiện hành động này", "Only the group owner can perform this action"},
        {"Không thể giải tán cuộc hội thoại 1-1", "Cannot dissolve a direct conversation"},
        {"Không thể chuyển quyền trưởng nhóm cho chính mình", "Cannot transfer ownership to yourself"},
        {"Người dùng này đã bị chặn", "This user has been blocked"},
        {"Bạn đã chặn người dùng này trước đó", "You have already blocked this user"},
        {"Bạn không thể tự chặn chính mình", "Cannot block yourself"},
        {"Không thể thu hồi tin nhắn sau 30 phút", "Cannot recall message after 30 minutes"},
        {"Bạn không có quyền thu hồi tin nhắn này", "You are not the author of this message"},
        {"Đã quá thời gian cho phép thu hồi tin nhắn", "Message recall time has expired"},
        {"Tin nhắn này đã được thu hồi trước đó", "This message has already been recalled"},
        {"Tin nhắn này đã được ghim rồi", "This message is already pinned"},
        {"Tin nhắn này chưa được ghim", "This message is not pinned"},
        {"Không thể chuyển tiếp tin nhắn đã thu hồi", "Cannot forward a recalled message"},
        {"Không thể trả lời tin nhắn đã thu hồi", "Cannot reply to a recalled message"},
        {"Bạn không thể tự báo cáo chính mình", "You cannot report yourself"},
        {"Không thể tạo cuộc trò chuyện với chính mình", "Cannot create a conversation with yourself"},
        {"Tập tin không hợp lệ", "Invalid file"},
        {"Tập tin vượt quá kích thước cho phép", "File size exceeds the allowed limit"},
        {"Bạn không thể tự gửi lời mời kết bạn cho chính mình", "You cannot send a friend request to yourself"},
        {"Bạn đã là bạn bè với người này rồi", "You are already friends with this user"},
        {"Bạn không phải là bạn bè với người này", "You are not friends with this user"},
        {"Lời mời kết bạn đã được gửi trước đó", "Friend request has already been sent"},
        {"Không tìm thấy lời mời kết bạn", "Friend request not found"},
        {"Bạn không phải là người nhận lời mời kết bạn này", "You are not the receiver of this friend request"},
        {"Không tìm thấy người dùng với số điện thoại này", "No user found with this phone number"},
        {"Không tìm thấy người dùng với từ khóa này", "No user found matching the keyword"},
        {"Hiện tại chỉ hỗ trợ đặt lịch hẹn trong cuộc trò chuyện 1-1", "Booking can only be created in 1-1 conversations for now", "Cannot book in a group chat, direct messages only"},
        {"Bạn đã vượt quá số lượng đặt lịch hẹn tối đa (3) trong cuộc trò chuyện này.", "You have reached the maximum number of active bookings (3) in this conversation.", "maximum number of active bookings (3)"},
        {"Bạn đã đạt giới hạn số lượng lịch hẹn đang hoạt động", "You have reached the maximum number of active bookings"},
        {"Không tìm thấy đối tác trong cuộc trò chuyện", "Could not find partner in conversation"},
        {"Bạn không phải là người tham gia lịch hẹn này", "You are not a participant in this booking"},
        {"Bạn không phải là thành viên của cuộc hội thoại này", "You are not a member of this conversation"},
        {"Không tìm thấy lịch hẹn", "Booking not found"},
        {"Lịch hẹn không ở trạng thái PENDING", "Booking is not in PENDING state"},
        {"Trạng thái lịch hẹn không hợp lệ cho thao tác này", "Invalid booking status for this action"},
        {"Lịch hẹn này chưa có thời gian diễn ra cụ thể", "This booking has not been scheduled yet"},
        {"Thời gian hẹn phải ở trong tương lai", "Scheduled time must be in the future"},
        {"Lịch hẹn quá gấp, vui lòng đặt xa hơn", "Booking must be scheduled further in advance"},
        {"Đã quá trễ để hủy lịch hẹn này", "It is too late to cancel this booking"},
        {"Vui lòng nhập lý do hủy lịch", "Reason is required for cancellation"},
        {"Không thể hoàn thành trước giờ hẹn", "Cannot complete the booking before its scheduled time"},
        {"Bạn chỉ có thể đánh giá đối tác của mình", "You can only rate your partner"},
        {"Bạn đã đánh giá lịch hẹn này rồi", "You have already rated this booking"},
        {"Điểm đánh giá phải từ 1 đến 5 sao", "Rating score must be between 1 and 5"},
        {"Yêu cầu nhập thông tin địa điểm", "Location information is required"},
        {"scheduledAt là bắt buộc", "scheduledAt is required"},
        {"scheduledAt phải ở trong tương lai", "scheduledAt must be in the future"},
        {"locationName là bắt buộc", "locationName is required"},
        {"locationAddress là bắt buộc", "locationAddress is required"},
        {"locationDistrict là bắt buộc", "locationDistrict is required"},
        {"locationCity là bắt buộc", "locationCity is required"},
        {"Lỗi khi chuyển đổi dữ liệu đặt lịch hẹn.", "Failed to serialize booking payload."},
        {"Không thể hẹn với người dùng đã bị chặn", "Cannot book with a blocked user"},
        {"Kích thước file vượt quá giới hạn 10MB", "File size exceeds limit"},
        {"File rỗng", "File is empty"},
        {"Định dạng file không được hỗ trợ", "File type not allowed"},
        {"Tên file không hợp lệ", "Invalid file name"},
        {"Tải lên file thất bại", "Upload failed"},
        {"Trạng thái người dùng không hoạt động", "User status is inactive"},
        {"Bạn cần bật chế độ Sẵn sàng để cập nhật vị trí", "You must enable Buddy Active to update location"},
        {"Bán kính quét không hợp lệ", "Invalid radar radius"},
        {"Tọa độ không hợp lệ", "Invalid GPS coordinates"},
        {"Không tìm thấy thông báo", "Notification not found"},
        {"Bạn không phải là người nhận thông báo này", "You are not the recipient of this notification"},
        {"Danh sách ID thông báo không được rỗng", "Notification ID list must not be empty"},
        {"Chưa có quyền USER trong hệ thống!", "Role USER not found in the system!", "Role USER not found"}
    };
    public static String map(String raw, int code) {
        String text = raw == null ? "" : raw.trim();
        if (text.contains("\n")) {
            java.util.LinkedHashSet<String> lines = new java.util.LinkedHashSet<>();
            for (String line : text.split("\n")) if (!line.trim().isEmpty()) lines.add(map(line, code));
            return String.join("\n", lines);
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String[] rule : RULES)
            for (int i = 1; i < rule.length; i++)
                if (lower.contains(rule[i].toLowerCase(Locale.ROOT))) return rule[0];
        if (!text.isEmpty()) return text;
        switch (code) {
            case 400: case 422: return "Yêu cầu không hợp lệ. Vui lòng kiểm tra thông tin đã nhập.";
            case 401: return "Thông tin xác thực không hợp lệ hoặc phiên đăng nhập đã hết hạn.";
            case 403: return "Bạn không có quyền thực hiện thao tác này.";
            case 404: return "Không tìm thấy dữ liệu yêu cầu.";
            case 409: return "Dữ liệu đã tồn tại hoặc bị xung đột.";
            case 429: return "Bạn đang thao tác quá nhanh. Vui lòng chờ trước khi thử lại.";
            default: return code >= 500 ? "Máy chủ đang gặp sự cố. Vui lòng thử lại sau."
                    : "Không thể thực hiện yêu cầu. Vui lòng thử lại.";
        }
    }
    private ErrorMessageMapper() { }
}
