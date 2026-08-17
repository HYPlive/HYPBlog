package top.hyp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hyp.constant.BlogStatusConstants;
import top.hyp.entity.Blog;
import top.hyp.entity.Category;
import top.hyp.entity.Tag;
import top.hyp.entity.User;
import top.hyp.exception.NotFoundException;
import top.hyp.exception.PersistenceException;
import top.hyp.mapper.BlogDraftMapper;
import top.hyp.service.BlogDraftService;
import top.hyp.service.CategoryService;
import top.hyp.service.TagService;
import top.hyp.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class BlogDraftServiceImpl implements BlogDraftService {
	@Autowired
	BlogDraftMapper blogDraftMapper;
	@Autowired
	CategoryService categoryService;
	@Autowired
	TagService tagService;

	@Override
	public List<Blog> getListByTitleAndCategoryId(String title, Integer categoryId) {
		return blogDraftMapper.getListByTitleAndCategoryId(title, categoryId);
	}

	@Override
	public Blog getDraftById(Long id) {
		Blog blog = blogDraftMapper.getDraftById(id);
		if (blog == null) {
			throw new NotFoundException("草稿不存在");
		}
		blog.setViews(0);
		return blog;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveDraft(top.hyp.model.dto.Blog blog) {
		prepareDraftBlog(blog);
		Date date = new Date();
		blog.setCreateTime(date);
		blog.setUpdateTime(date);
		User user = new User();
		user.setId(1L);
		blog.setUser(user);
		if (blogDraftMapper.saveDraft(blog) != 1) {
			throw new PersistenceException("保存草稿失败");
		}
		saveDraftTags(blog);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateDraft(top.hyp.model.dto.Blog blog) {
		prepareDraftBlog(blog);
		blog.setUpdateTime(new Date());
		if (blogDraftMapper.updateDraft(blog) != 1) {
			throw new PersistenceException("更新草稿失败");
		}
		blogDraftMapper.deleteDraftTagByDraftId(blog.getId());
		saveDraftTags(blog);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteDraftById(Long id) {
		blogDraftMapper.deleteDraftTagByDraftId(id);
		if (blogDraftMapper.deleteDraftById(id) != 1) {
			throw new NotFoundException("草稿不存在");
		}
	}

	private void prepareDraftBlog(top.hyp.model.dto.Blog blog) {
		if (StringUtils.isEmpty(blog.getTitle())) {
			blog.setTitle("未命名草稿");
		}
		if (blog.getFirstPicture() == null) {
			blog.setFirstPicture("");
		}
		if (blog.getContent() == null) {
			blog.setContent("");
		}
		if (blog.getDescription() == null) {
			blog.setDescription("");
		}
		blog.setPublished(false);
		blog.setRecommend(false);
		blog.setAppreciation(false);
		blog.setCommentEnabled(false);
		blog.setTop(false);
		blog.setPassword("");
		blog.setViews(0);
		int words = blog.getWords() == null || blog.getWords() < 0 ? countWords(blog.getContent()) : blog.getWords();
		blog.setWords(words);
		blog.setReadTime(blog.getReadTime() == null || blog.getReadTime() < 0 ? (int) Math.round(words / 200.0) : blog.getReadTime());
		blog.setCategory(resolveDraftCategory(blog.getCate()));
		blog.setStatus(BlogStatusConstants.DRAFT);
	}

	private Category resolveDraftCategory(Object cate) {
		if (cate instanceof Integer) {
			Category category = categoryService.getCategoryById(((Integer) cate).longValue());
			if (category != null) {
				return category;
			}
		} else if (cate instanceof String && !StringUtils.isEmpty((String) cate)) {
			Category category = categoryService.getCategoryByName((String) cate);
			if (category != null) {
				return category;
			}
			Category c = new Category();
			c.setName((String) cate);
			categoryService.saveCategory(c);
			return c;
		}
		Category uncategorized = categoryService.getCategoryByName("未分类");
		if (uncategorized != null) {
			return uncategorized;
		}
		Category c = new Category();
		c.setName("未分类");
		categoryService.saveCategory(c);
		return c;
	}

	private void saveDraftTags(top.hyp.model.dto.Blog blog) {
		List<Object> tagList = blog.getTagList();
		if (tagList == null || tagList.isEmpty()) {
			return;
		}
		for (Object t : tagList) {
			Tag tag = null;
			if (t instanceof Integer) {
				tag = tagService.getTagById(((Integer) t).longValue());
			} else if (t instanceof String && !StringUtils.isEmpty((String) t)) {
				tag = tagService.getTagByName((String) t);
				if (tag == null) {
					tag = new Tag();
					tag.setName((String) t);
					tagService.saveTag(tag);
				}
			}
			if (tag != null) {
				blogDraftMapper.saveDraftTag(blog.getId(), tag.getId());
			}
		}
	}

	private int countWords(String content) {
		return content == null ? 0 : content.replaceAll("\\s+", "").length();
	}
}
