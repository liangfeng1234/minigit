package com.minigit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minigit.common.R;
import com.minigit.entity.Repo;
import com.minigit.entity.User;
import com.minigit.entityService.RepoService;
import com.minigit.entityService.UserService;
import com.minigit.util.GitUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/{user}")
public class RepoController {
    @Autowired
    private RepoService repoService;
    @Autowired
    private UserService userService;

    /**
     *
     * @param repo
     * @param session
     * @return
     */
    @PostMapping("/init")
    public R<Repo> init(@RequestBody Repo repo, HttpSession session){
        String path = repo.getPath();
        String repoName = repo.getName();
        boolean isPublic = repo.getIsPublic();
        GitUtils.init(path);
        Long authorId = (Long) session.getAttribute("user");
        repo.setAuthorId(authorId);
        repoService.save(repo);
        return R.success(repo);
    }

    @GetMapping()
    public R<List<Repo>> getAllRepo(@PathVariable String user, HttpSession session){
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccountName, user);
        User user0 = userService.getOne(queryWrapper);
        Long id = user0.getId();

        LambdaQueryWrapper<Repo> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Repo::getAuthorId, id);
        List<Repo> list = repoService.list(queryWrapper1);
        return R.success(list);
    }

    @PostMapping("/add")
    public R<Repo> addRepo(@PathVariable String user, @RequestBody Repo repo, HttpSession session){
        Long authorId = (Long) session.getAttribute("user");
        repo.setAuthorId(authorId);
        repoService.save(repo);
        return R.success(repo);
    }

    @DeleteMapping
    public R<String> deleteRepo(HttpSession session){

        return null;
    }

    @PutMapping
    public R<Repo> updateRepo(@RequestBody Repo repo, HttpSession session){

        return null;
    }




}
