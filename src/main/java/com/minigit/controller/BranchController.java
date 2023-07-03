package com.minigit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jcraft.jsch.SftpException;
import com.minigit.common.R;
import com.minigit.entity.*;
import com.minigit.entityService.*;
import com.minigit.service.UploadService;
import com.minigit.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/{userName}/{repoName}")
public class BranchController {
    @Autowired
    private BranchService branchService;
    @Autowired
    private RepoService repoService;
    @Autowired
    private CommitService commitService;
    @Autowired
    private UploadService uploadService;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepoRelationService userRepoRelationService;

    @PostMapping("/add")
    public R<Branch> addBranch(@PathVariable String userName, @PathVariable String repoName,@RequestParam String branchName,
                               @RequestBody Branch sourceBranch, HttpSession session) throws Exception {
        Long authorId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<Repo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Repo::getId,sourceBranch.getRepoId());
        Repo repo = repoService.getOne(queryWrapper);
        String repoPath = repo.getPath();
        Branch branch = new Branch();
        branch.setName(branchName);
        branch.setRepoId(sourceBranch.getRepoId());
        branch.setAuthorId(authorId);
        String commitHash = FileUtils.getCurrentCommitHash(repoPath);
        branch.setCommitHash(commitHash);
        branchService.save(branch);
        String remoteRepoPath = uploadService.REMOTE_REPO_PATH+ "/" + userName + "/" + repoName;
        uploadService.copyDirectory(repoPath, remoteRepoPath + "/" + sourceBranch.getName(),
                remoteRepoPath + "/" + branchName);
        FileUtils.writeFile(repoPath + File.separator + ".minigit" + File.separator + "refs" +
                File.separator + "heads" + File.separator + branchName, commitHash);
        return R.success(branch);
    }

    @GetMapping("/branches")
    public R<List<Branch>> getAllBranches(@PathVariable String userName, @PathVariable String repoName,HttpSession session){
        Long userId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<User> queryWrapper8 = new LambdaQueryWrapper<>();
        queryWrapper8.eq(User::getAccountName, userName);
        User user = userService.getOne(queryWrapper8);
        if(user.getId() !=userId){
            LambdaQueryWrapper<UserRepoRelation> queryWrapper3 = new LambdaQueryWrapper<>();
            queryWrapper3.eq(UserRepoRelation::getUserId, user.getId()).eq(UserRepoRelation::getIsAccept, 1);
            List<UserRepoRelation> list = userRepoRelationService.list(queryWrapper3);
            for (UserRepoRelation userRepoRelation : list) {
                LambdaQueryWrapper<Repo> queryWrapper5 = new LambdaQueryWrapper<>();
                queryWrapper5.eq(Repo::getName, repoName);
                Repo repo = repoService.getOne(queryWrapper5);
                if(repo != null){
                    LambdaQueryWrapper<Branch> queryWrapper7 = new LambdaQueryWrapper<>();
                    queryWrapper7.eq(Branch::getRepoId,repo.getId());
                    List<Branch> list1 = branchService.list(queryWrapper7);
                    return R.success(list1);
                }
            }
        }else{
            Long authorId = (Long) session.getAttribute("user");
            LambdaQueryWrapper<Repo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Repo::getAuthorId, authorId).eq(Repo::getName, repoName);
            Repo repo = repoService.getOne(queryWrapper);

            LambdaQueryWrapper<Branch> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Branch::getRepoId,repo.getId());
            List<Branch> list = branchService.list(queryWrapper1);
            return R.success(list);
        }
        return R.success(null);
    }

    @GetMapping("/{branchName}/commits")
    public R<List<Commit>> getAllCommits(@PathVariable String userName, @PathVariable String repoName,
                                         @PathVariable String branchName){

        List<Commit> list = commitService.getAllCommits(userName, repoName, branchName);
        return R.success(list);
    }

    @DeleteMapping("/{branchName}")
    public R<String> deleteBranch(@PathVariable String userName, @PathVariable String repoName,
                                  @PathVariable String branchName, HttpSession session) {
        Long authorId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<Repo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Repo::getAuthorId, authorId).eq(Repo::getName, repoName);
        Repo repo = repoService.getOne(queryWrapper);

        LambdaQueryWrapper<Branch> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Branch::getRepoId,repo.getId()).eq(Branch::getName, branchName);
        branchService.remove(queryWrapper1);
        try {
            uploadService.deleteDirectory(uploadService.REMOTE_REPO_PATH + "/" + userName + "/" + repoName + "/" + branchName,
                    uploadService.getSFTPClient());
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
        return R.success("删除成功！");
    }


}
