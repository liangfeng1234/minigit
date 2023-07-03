package com.minigit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jcraft.jsch.SftpException;
import com.minigit.common.R;
import com.minigit.entity.User;
import com.minigit.entityService.UserService;
import com.minigit.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/{userName}/{repoName}")
public class FileController {
    @Autowired
    private UploadService uploadService;
    @Autowired
    private UserService userService;


    @GetMapping("/blob/{branchName}/**")
    public R<String> getFilePath(@PathVariable String userName, @PathVariable String repoName, @PathVariable String branchName,
             HttpServletRequest request) throws SftpException {
        String requestURI = request.getRequestURI();
        String filepath = requestURI.replaceFirst("/blob/", "/");
        String content = uploadService.readFile(uploadService.REMOTE_REPO_PATH +  filepath);
        return R.success(content);
    }

    @GetMapping("/tree/{branchName}/**")
    public R<Map<String, String>> getDirPath(@PathVariable String userName, @PathVariable String repoName, @PathVariable String branchName,
                                             HttpServletRequest request, HttpSession session) throws SftpException {
        Long userId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<User> queryWrapper8 = new LambdaQueryWrapper<>();
        queryWrapper8.eq(User::getAccountName, userName);
        User user = userService.getOne(queryWrapper8);
        if(user.getId() == userId){
            // 是仓库的主人
            String requestURI = request.getRequestURI();
            String filepath = requestURI.replaceFirst("/tree/", "/");
            System.out.println(filepath);
            Map<String, String> map = uploadService.readDir(uploadService.REMOTE_REPO_PATH + filepath);
            return R.success(map);
        }else{
            /*// 不是仓库的主人
            LambdaQueryWrapper<User> queryWrapper7 = new LambdaQueryWrapper<>();
            queryWrapper7.eq(User::getId, user.getId());
            User user1 = userService.getOne(queryWrapper7);
            String filepath = "/" + user1.getAccountName() + "/" + repoName + "/" + branchName + "/";
            System.out.println(filepath);
            Map<String, String> map = uploadService.readDir(uploadService.REMOTE_REPO_PATH + filepath);
            return R.success(map);*/
            return R.success(null);
        }

    }
}
