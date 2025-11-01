package com.womtech.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.womtech.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Transactional
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;
    
    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn file để upload!");
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ cho phép upload file ảnh!");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "womtech/products", // Tự động tạo folder
                    "resource_type", "image"
            ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload ảnh lên Cloudinary!", e);
        }
    }
    
    @Override
    public boolean deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        
        try {
            // Extract public_id from URL
            // URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
            String publicId = extractPublicIdFromUrl(imageUrl);
            
            if (publicId != null) {
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                return "ok".equals(result.get("result"));
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa ảnh từ Cloudinary: " + e.getMessage());
            return false;
        }
    }
    
    @Override
	public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn file để upload!");
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ cho phép upload file ảnh!");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "womtech/avatars", // Tự động tạo folder
                    "resource_type", "image"
            ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload ảnh lên Cloudinary!", e);
        }
    }
    
    @Override
    public String extractPublicIdFromUrl(String imageUrl) {
        try {
            // Example URL: https://res.cloudinary.com/demo/image/upload/v1234567890/womtech/products/abc-123.jpg
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }
            
            String pathWithVersion = parts[1];
            // Remove version (v1234567890/)
            String pathWithoutVersion = pathWithVersion.replaceFirst("v\\d+/", "");
            // Remove file extension
            String publicId = pathWithoutVersion.replaceFirst("\\.[^.]+$", "");
            
            return publicId;
            
        } catch (Exception e) {
            System.err.println("Không thể extract public_id từ URL: " + imageUrl);
            return null;
        }
    }
    
    /**
     * Generate thumbnail URL from original URL
     * @param imageUrl Original image URL
     * @param width Thumbnail width
     * @param height Thumbnail height
     * @return Thumbnail URL
     */
    @Override
    public String generateThumbnailUrl(String imageUrl, int width, int height) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return imageUrl;
        }
        
        // Insert transformation parameters into URL
        String transformation = "w_" + width + ",h_" + height + ",c_fill,q_auto,f_auto/";
        return imageUrl.replace("/upload/", "/upload/" + transformation);
    }
}
