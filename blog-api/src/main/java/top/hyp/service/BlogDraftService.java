package top.hyp.service;

import top.hyp.entity.Blog;

import java.util.List;

public interface BlogDraftService {
	List<Blog> getListByTitleAndCategoryId(String title, Integer categoryId);

	Blog getDraftById(Long id);

	void saveDraft(top.hyp.model.dto.Blog blog);

	void updateDraft(top.hyp.model.dto.Blog blog);

	void deleteDraftById(Long id);
}
