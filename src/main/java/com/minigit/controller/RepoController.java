package com.minigit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.minigit.common.R;
import com.minigit.entity.Branch;
import com.minigit.entity.Repo;
import com.minigit.entity.User;
import com.minigit.entity.UserRepoRelation;
import com.minigit.entityService.BranchService;
import com.minigit.entityService.RepoService;
import com.minigit.entityService.UserRepoRelationService;
import com.minigit.entityService.UserService;
import com.minigit.service.GitService;
import com.minigit.service.UploadService;
import com.minigit.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/{userName}")
public class RepoController {
    @Autowired
    private RepoService repoService;
    @Autowired
    private UserService userService;
    @Autowired
    private BranchService branchService;
    @Autowired
    private GitService gitService;
    @Autowired
    private UploadService uploadService;

    @Autowired
    private UserRepoRelationService userRepoRelationService;

    /**
     * @param repo
     * @param session
     * @return
     */
    @PostMapping("/init")
    public R<Repo> init(@PathVariable String userName, @RequestBody Repo repo, HttpSession session) throws SftpException {
        String path = repo.getPath();
        Long authorId = (Long) session.getAttribute("user");
        gitService.init(path, authorId, repo);
        uploadService.createDir(uploadService.REMOTE_REPO_PATH + "/" + userName + "/" + repo.getName());
        Branch branch = new Branch();
        branch.setName("main");
        repoService.save(repo);
        branch.setRepoId(repo.getId());
        branch.setAuthorId(authorId);
        // 还没有提交，commitHash为null
        branch.setCommitHash(null);
        branchService.save(branch);
        uploadService.createDir(uploadService.REMOTE_REPO_PATH + "/" + userName + "/" + repo.getName() + "/" + branch.getName());
        return R.success(repo);
    }

    @GetMapping("/repos")
    public R<List<Repo>> getAllRepo(@PathVariable String userName, HttpSession session){
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccountName, userName);
        User user0 = userService.getOne(queryWrapper);
        Long id = user0.getId();

        LambdaQueryWrapper<Repo> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Repo::getAuthorId, id);
        List<Repo> list = repoService.list(queryWrapper1);

        LambdaQueryWrapper<UserRepoRelation> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(UserRepoRelation::getUserId, user0.getId()).eq(UserRepoRelation::getIsAccept, 1);
        // 得到本user的被邀请且接受的仓库
        List<UserRepoRelation> list1 = userRepoRelationService.list(queryWrapper2);
        for (UserRepoRelation userRepoRelation : list1) {
            // 把每个仓库取出来
            LambdaQueryWrapper<Repo> queryWrapper3 = new LambdaQueryWrapper<>();
            queryWrapper3.eq(Repo::getId, userRepoRelation.getRepoId());
            Repo repo = repoService.getOne(queryWrapper3);
            list.add(repo);
        }

        return R.success(list);
    }

    @DeleteMapping("/{repoName}")
    public R<String> deleteRepo(@PathVariable String userName, @PathVariable String repoName, HttpSession session){
        LambdaQueryWrapper<Repo> queryWrapper  = new LambdaQueryWrapper<>();
        Long authorId = (Long) session.getAttribute("user");
        queryWrapper.eq(Repo::getAuthorId, authorId).eq(Repo::getName, repoName);
        Repo repo1 = repoService.getOne(queryWrapper);
        repoService.remove(queryWrapper);

        try {
            FileUtils.deleteFileOrDirectory(repo1.getPath() + File.separator + ".minigit");
            uploadService.deleteDirectory(uploadService.REMOTE_REPO_PATH + "/" + userName + "/" + repoName, uploadService.getSFTPClient());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
        return R.success("删除成功！");
    }

    @PutMapping
    public R<Repo> updateRepo(@RequestBody Repo repo, HttpSession session){

        return null;
    }

    @PostMapping("/{repoName}/invite/{partnerName}")
    public R<String> invite(@PathVariable String userName, @PathVariable String repoName,@PathVariable String partnerName, HttpSession session){
        LambdaQueryWrapper<Repo> queryWrapper  = new LambdaQueryWrapper<>();
        Long authorId = (Long) session.getAttribute("user");
        queryWrapper.eq(Repo::getAuthorId, authorId).eq(Repo::getName, repoName);
        Repo repo = repoService.getOne(queryWrapper);
        Long repoId = repo.getId();

        // 找到被邀请人的userId
        LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(User::getAccountName, partnerName);
        User user = userService.getOne(queryWrapper1);

        UserRepoRelation userRepoRelation = new UserRepoRelation();
        userRepoRelation.setRepoId(repoId);

        userRepoRelation.setUserId(user.getId());
        userRepoRelationService.save(userRepoRelation);
        return R.success("发送邀请成功！");
    }

    @PostMapping("/{repoName}/invite/accept")
    public R<String> acceptInvitation(@PathVariable String userName, @PathVariable String repoName, HttpSession session){
        Long UserId = (Long) session.getAttribute("user");

        LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(User::getAccountName, userName);
        User user = userService.getOne(queryWrapper1);

        LambdaQueryWrapper<Repo> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Repo::getAuthorId, user.getId()).eq(Repo::getName, repoName);
        Repo repo = repoService.getOne(queryWrapper2);


        LambdaQueryWrapper<UserRepoRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRepoRelation::getUserId, UserId).eq(UserRepoRelation::getRepoId, repo.getId());

        UserRepoRelation userRepoRelation = userRepoRelationService.getOne(queryWrapper);
        userRepoRelation.setIsAccept(true);

        /*userRepoRelationService.remove(queryWrapper);

        UserRepoRelation userRepoRelation = new UserRepoRelation();
        userRepoRelation.setRepoId(repo.getId());

        userRepoRelation.setUserId(UserId);
        userRepoRelation.setIsAccept(true);

        userRepoRelationService.save(userRepoRelation);*/

        return R.success("接受邀请！");
    }

    @PostMapping("/{repoName}/invite/reject")
    public R<String> rejectInvitation(@PathVariable String userName, @PathVariable String repoName, HttpSession session){
        /*LambdaQueryWrapper<UserRepoRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRepoRelation::getUserId, userRepoRelation.getUserId()).eq(UserRepoRelation::getRepoId, userRepoRelation.getRepoId());

        userRepoRelationService.remove(queryWrapper);*/

        return R.success("拒绝邀请！");
    }

    @PostMapping("/{repoName}/invite")
    public R<List<String>> getInvitation(HttpSession session){
        Long userId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<UserRepoRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRepoRelation::getUserId, userId).eq(UserRepoRelation::getIsAccept, 0);

        List<UserRepoRelation> list = userRepoRelationService.list(queryWrapper);

        List<String> result = new ArrayList<>();

        for (UserRepoRelation userRepoRelation : list) {


            LambdaQueryWrapper<Repo> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(Repo::getId, userRepoRelation.getRepoId());
            Repo repo = repoService.getOne(queryWrapper2);

            LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(User::getId, repo.getAuthorId());
            User user = userService.getOne(queryWrapper1);


            result.add(user.getAccountName() + "\t" + repo.getName());
        }

        return R.success(result);
    }

}
