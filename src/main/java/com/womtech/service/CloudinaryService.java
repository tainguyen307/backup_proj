package com.womtech.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
		String uploadImage(MultipartFile file);
		boolean deleteImage(String imageUrl);
		String extractPublicIdFromUrl(String imageUrl);
		String generateThumbnailUrl(String imageUrl, int width, int height);
		String uploadAvatar(MultipartFile file);
}
