package com.school.dismissal.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务。业务层只依赖此接口，不关心文件最终落在本地磁盘还是 OSS。
 * 后续接入阿里云 OSS 时新增 OssStorageServiceImpl 即可，Controller / Service 无需改动。
 */
public interface StorageService {

    /**
     * 上传图片文件，返回可直接访问的完整 URL。
     *
     * @param file      上传的文件
     * @param dirPrefix 业务目录前缀，如 "avatar"、"student-face"
     * @return 完整访问 URL
     */
    String uploadImage(MultipartFile file, String dirPrefix);

    /**
     * 根据完整 URL 删除文件（删除失败仅记录日志，不抛异常）。
     */
    void deleteByUrl(String url);
}
