package com.example.blog.controller.admin;

import com.example.blog.dto.CommentDto;
import com.example.blog.model.Blog;
import com.example.blog.model.Comment;
import com.example.blog.model.User;
import com.example.blog.service.BlogService;
import com.example.blog.service.CommentService;
import com.example.blog.service.StorageService;
import com.example.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Controller
@RequestMapping("admin/blogs")
public class BlogAdminController {
    private final BlogService blogService;
    private final CommentService commentService;
    private final UserService userService;
    private final StorageService storageService;

    public BlogAdminController(BlogService blogService, CommentService commentService, UserService userService, StorageService storageService) {
        this.blogService = blogService;
        this.commentService = commentService;
        this.userService = userService;
        this.storageService = storageService;
    }

    // Hiển thị chi tiết một bài blog
    @GetMapping("view/{blogId}")
    public String view(@PathVariable("blogId") Long blogId, Model model) {
        // Tìm blog dựa trên blogId
        Optional<Blog> existed = blogService.findById(blogId);
        Blog entity = existed.get();

        // Thêm blog vào model để truyền dữ liệu sang giao diện
        model.addAttribute("blog", entity);
        return "userContent"; // Trả về giao diện hiển thị nội dung blog
    }

    // Xóa một bài blog
    @GetMapping("delete/{blogId}")
    public String delete(@PathVariable("blogId") Long blogId, Model model) throws IOException {
        // Tìm blog dựa trên blogId
        Optional<Blog> blogOptional = blogService.findById(blogId);
        Blog entity = blogOptional.get();

        // Lấy danh sách người dùng đã thích bài blog
        List<User> userList = new ArrayList<>(entity.getLikedUser());
        String title = entity.getTitle();

        // Xóa liên kết giữa người dùng và bài blog
        for (User user : userList) {
            user.removeLikedBlog(entity);
            userService.save(user); // Lưu người dùng sau khi cập nhật
        }
        // Nếu blog có hình ảnh, xóa hình ảnh khỏi bộ lưu trữ
        if (entity.getImageTitle() != null) {
            storageService.delete(entity.getImageTitle());
        }
        // Xóa bài blog khỏi cơ sở dữ liệu
        blogService.delete(entity);
        // Thêm thông báo xóa blog vào model
        model.addAttribute("deleteMess", "Truyện " + title + " đã bị xóa");
        return "forward:/admin/blogs"; // Chuyển tiếp đến danh sách blog
    }

    // Xóa một bình luận trong blog
    @GetMapping("deleteComment/{commentId}")
    public String deleteComment(@PathVariable("commentId") Long commentId) {
        // Tìm bình luận dựa trên commentId
        Optional<Comment> existed = commentService.findById(commentId);
        Comment entity = existed.get();

        // Lấy blogId liên quan đến bình luận
        Long blogId = entity.getBlogComment().getBlogId();
        // Xóa bình luận khỏi cơ sở dữ liệu
        commentService.delete(entity);
        // Chuyển hướng về trang chi tiết blog
        return "redirect:/admin/blogs/view/" + blogId;
    }

    // Danh sách blog với phân trang
    @GetMapping("")
    public String list(@RequestParam("page") Optional<Integer> page,
                       @RequestParam("size") Optional<Integer> size,
                       Model model) {
        int currentPage = page.orElse(1); // Trang hiện tại, mặc định là 1
        int pageSize = size.orElse(5); // Số bài blog trên mỗi trang, mặc định là 5
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        // Lấy danh sách blog với phân trang
        Page<Blog> resultPage = blogService.findAll(pageable);
        // Thêm thông tin phân trang vào model
        paginateBlog(model, currentPage, resultPage);
        return "adminBlogList"; // Trả về giao diện danh sách blog
    }

    // Tìm kiếm blog theo từ khóa với phân trang
    @GetMapping("search")
    public String search(@RequestParam(value = "searchWord", required = false) String name,
                         @RequestParam("page") Optional<Integer> page,
                         @RequestParam("size") Optional<Integer> size,
                         Model model) {
        int currentPage = page.orElse(1); // Trang hiện tại, mặc định là 1
        int pageSize = size.orElse(5); // Số bài blog trên mỗi trang, mặc định là 5
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        if (StringUtils.hasText(name)) { // Kiểm tra từ khóa tìm kiếm
            // Tìm kiếm blog theo từ khóa
            Page<Blog> resultPage = blogService.findByNameContaining(name.trim(), pageable);
            paginateBlog(model, currentPage, resultPage);
        } else {
            // Nếu không có từ khóa, lấy tất cả blog
            Page<Blog> resultPage = blogService.findAll(pageable);
            paginateBlog(model, currentPage, resultPage);
        }
        model.addAttribute("keyword", name); // Thêm từ khóa tìm kiếm vào model
        return "adminBlogSearch"; // Trả về giao diện kết quả tìm kiếm
    }

    // Hàm hỗ trợ phân trang cho danh sách blog
    private void paginateBlog(Model model, int currentPage, Page<Blog> resultPage) {
        int totalPages = resultPage.getTotalPages(); // Tổng số trang
        if (totalPages > 0) {
            int start = Math.max(1, currentPage - 2); // Trang bắt đầu
            int end = Math.min(currentPage + 2, totalPages); // Trang kết thúc
            if (totalPages >= 5) {
                if (end == totalPages) start = end - 4; // Điều chỉnh nếu ở cuối danh sách
                else if (start == 1) end = start + 4; // Điều chỉnh nếu ở đầu danh sách
            }
            // Tạo danh sách số trang
            List<Integer> pageNumbers = IntStream.rangeClosed(start, end)
                    .boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers); // Thêm danh sách số trang vào model
        }
        model.addAttribute("blogs", resultPage); // Thêm danh sách blog vào model
    }
}
