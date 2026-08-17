package top.hyp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hyp.entity.Blog;

import java.util.List;

@Mapper
@Repository
public interface BlogDraftMapper {
	List<Blog> getListByTitleAndCategoryId(@Param("title") String title, @Param("categoryId") Integer categoryId);

	Blog getDraftById(Long id);

	int saveDraft(top.hyp.model.dto.Blog blog);

	int updateDraft(top.hyp.model.dto.Blog blog);

	int deleteDraftById(Long id);

	int deleteDraftTagByDraftId(Long draftId);

	int saveDraftTag(Long draftId, Long tagId);
}
