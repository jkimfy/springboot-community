package com.foo.community.controller;

import com.foo.community.annotation.LoginRequired;
import com.foo.community.entity.User;
import com.foo.community.service.FollowService;
import com.foo.community.service.LikeService;
import com.foo.community.service.UserService;
import com.foo.community.util.CommunityConstant;
import com.foo.community.util.CommunityUtil;
import com.foo.community.util.HostHolder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath = "upload/";

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(){
        return "/site/setting";
    }

    //上传文件是指上传到服务器，这时候前端还没有更新，所以还需要一个查询方法
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error","请选择图片");
            return "/site/setting";
        }
        //用户原始文件名,1.png
        String filename = headerImage.getOriginalFilename();
        //获取文件后缀名
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        //根据后缀起一个新文件名
        filename = CommunityUtil.generateUUID() + suffix;

        File dest = new File(uploadPath+ "/" + filename);

        try {
            //存储文件
//            headerImage.transferTo(dest);
            FileUtils.copyInputStreamToFile(headerImage.getInputStream(), dest);
        } catch (IOException e) {
            logger.error("上传失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常",e);
        }
        //上传成功，更新用户头像路径(web访问路径)
        //http://localhost:8081/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + "/" + contextPath + "/user/header/" + filename;
        userService.updateheader(user.getId(),headerUrl);
        //更新成功，重定向到首页
        return "redirect:/index";
    }

    //访问头像
    @GetMapping("/header/{filename}")
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        filename = uploadPath + "/" + filename;
        String suffix = filename.substring(filename.lastIndexOf("."));

        //响应图片
        response.setContentType("image/" + suffix);

        try(OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(filename);
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }
    }

    //个人主页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId,Model model) {
        User user = userService.findUserByUserId(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        //用户
        model.addAttribute("user",user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注
        boolean hashFollowed = false;
        if (hostHolder.getUser()!=null) {
            hashFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hashFollowed);

        return "/site/profile";
    }

}