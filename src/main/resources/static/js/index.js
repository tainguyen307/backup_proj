// JavaScript để clear cookies nếu không có authentication hợp lệ
document.addEventListener('DOMContentLoaded', function() {
    // Kiểm tra xem có authentication hợp lệ không
    fetch('/auth/me')
        .then(response => response.json())
        .then(data => {
            // Nếu không có authentication hoặc token đã hết hạn
            if (!data.ok || data.message === 'No auth') {
                // Clear tất cả cookies
                const cookiesToDelete = ['AT', 'RT', 'WOM_REMEMBER', 'JSESSIONID'];
                
                cookiesToDelete.forEach(function(cookieName) {
                    document.cookie = cookieName + '=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict';
                    document.cookie = cookieName + '=; Path=/; Max-Age=0; Domain=localhost; HttpOnly';
                    document.cookie = cookieName + '=; Path=/; Max-Age=0; Domain=.localhost; HttpOnly';
                    document.cookie = cookieName + '=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT; HttpOnly';
                });
                
                console.log('Cleared invalid authentication cookies');
            }
        })
        .catch(error => {
            console.log('Error checking authentication:', error);
            // Nếu có lỗi, cũng clear cookies để đảm bảo
            const cookiesToDelete = ['AT', 'RT', 'WOM_REMEMBER', 'JSESSIONID'];
            
            cookiesToDelete.forEach(function(cookieName) {
                document.cookie = cookieName + '=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict';
                document.cookie = cookieName + '=; Path=/; Max-Age=0; Domain=localhost; HttpOnly';
                document.cookie = cookieName + '=; Path=/; Max-Age=0; Domain=.localhost; HttpOnly';
                document.cookie = cookieName + '=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT; HttpOnly';
            });
        });
});